package com.vulcanice.vulcanice;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.Tracking;
import com.vulcanice.vulcanice.Model.User;

public class ListShops extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{
    DatabaseReference locations, onlineShopsRef, currentUserRef, counterRef;
    FirebaseRecyclerAdapter<User, ListShopViewHolder> adapter;

    protected int online = 1;
    RecyclerView listShop;
    RecyclerView.LayoutManager layoutManager;

    // FOR_TRACKING_LOCATION
    private static final int PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RES_REQUEST = 7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISTANCE = 10;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_shops);

        listShop = (RecyclerView) findViewById(R.id.listShop);
        listShop.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listShop.setLayoutManager(layoutManager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("@string/title");

        locations = FirebaseDatabase.getInstance().getReference("Locations");
        onlineShopsRef = FirebaseDatabase.getInstance()
                .getReference().child(".info/connected");
        counterRef = FirebaseDatabase.getInstance()
                .getReference("lastOnline");
        currentUserRef = FirebaseDatabase.getInstance()
                .getReference("lastOnline")
                .child(FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        .getUid()
                );

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.INTERNET
            },PERMISSION_REQUEST_CODE);

        }
        else
        {
            if (checkPlayServices())
            {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                createLocationRequest();
                buildGoogleApiClient();
                displayLocation();
            }
        }
        setupSystem();
        updateList();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.INTERNET) !=  PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //Update to firebase
                Tracking tracking = new Tracking();
                tracking.setEmail( FirebaseAuth.getInstance().getCurrentUser().getEmail() );
                tracking.setUid( FirebaseAuth.getInstance().getCurrentUser().getUid() );
                tracking.setLatitude( String.valueOf(location.getLatitude()) );
                tracking.setLongitude( String.valueOf(location.getLongitude()) );

                locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(tracking);
            }
        });
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (mLastLocation != null )
//        {
//            //Update to firebase
//            Tracking tracking = new Tracking();
//            tracking.setEmail( FirebaseAuth.getInstance().getCurrentUser().getEmail() );
//            tracking.setUid( FirebaseAuth.getInstance().getCurrentUser().getUid() );
//            tracking.setLatitude( String.valueOf(mLastLocation.getLatitude()) );
//            tracking.setLongitude( String.valueOf(mLastLocation.getLongitude()) );
//
//            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                    .setValue(tracking);
//        }
//        else
//        {
//            Toast.makeText(this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
//        }
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
    }

    private void updateList() {
        adapter = new FirebaseRecyclerAdapter<User, ListShopViewHolder>(
                User.class,
                R.layout.user_layout,
                ListShopViewHolder.class,
                counterRef
        ) {
            @Override
            protected void populateViewHolder(ListShopViewHolder viewHolder, User model, int position) {
                viewHolder.textEmail.setText(model.getEmail());
            }
        };
        adapter.notifyDataSetChanged();
        listShop.setAdapter(adapter);
    }

    private void setupSystem() {
        onlineShopsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().removeValue();
                    User user = new User();
                    user.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    user.setStatus(online);
                    counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    String textStatus;
                    if (user.getStatus() == 1)
                    {
                        textStatus = "@string/online";
                    }
                    else
                    {
                        textStatus = "@string/offline";
                    }
                    Log.d("LOG", "" + user.getEmail() + " is " + textStatus);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionJoin:
                User user = new User();
                user.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                user.setStatus(online);
                counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(user);
                break;
            case R.id.actionLogout:
                currentUserRef.removeValue();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if ( resultCode != ConnectionResult.SUCCESS )
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RES_REQUEST).show();
            }
            else
            {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if ( mGoogleApiClient != null )
        {
            mGoogleApiClient.connect();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
        );
    }

    @Override
    public void onConnectionSuspended(int i) { mGoogleApiClient.connect(); }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Location services connection failed!!!", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onStop() {
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;

        }
    }
}
