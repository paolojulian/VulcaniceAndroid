package com.vulcanice.vulcanice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vulcanice.vulcanice.Model.Shop;

public class FindGasActivity extends AppCompatActivity {
    //DATABASE
    protected FirebaseUser user;
    protected DatabaseReference vulcanizeRef;
    //RECYCLER
    private RecyclerView mListGas;
    private RecyclerView.LayoutManager mLayoutManager;
    protected FirebaseRecyclerAdapter<Shop, ListGasViewHolder> firebaseAdapter;
    //LOCATION
    protected FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    protected LocationCallback locationCallback;
    protected LocationRequest mLocationRequest;
    //INTERVAL
    private long UPDATE_INTERVAL = 10 * 1000; /* 10 seconds */
    private long FASTEST_INTERVAL = 5 * 1000; /* 2 seconds */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_nearest_gas);
        checkIfLoggedIn();
        //GET LOCATION
        setLocationRequest();
        setLocation();
        setOnLocationUpdate();
        listGas();
    }

    private void setLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setLocation() {
        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void listGas() {
        vulcanizeRef = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child("Gas Station");
        mListGas = findViewById(R.id.list_gas_station);

        firebaseAdapter = new FirebaseRecyclerAdapter<Shop, ListGasViewHolder>
                (Shop.class, R.layout.listview_shop, ListGasViewHolder.class, vulcanizeRef) {
            @Override
            protected void populateViewHolder(ListGasViewHolder viewHolder, Shop model, int position) {
                Toast.makeText(
                        FindGasActivity.this,
                        "Test",
                        Toast.LENGTH_SHORT
                ).show();
                viewHolder.bindListGas(model);
            }
        };
        mListGas.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mListGas.setLayoutManager(mLayoutManager);
        mListGas.setAdapter(firebaseAdapter);
    }

    private void setOnLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            responseNoLocation();
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            responseNoLocation();
                            return;
                        }
                        mLastLocation = location;
//                        Toast.makeText(
//                                FindGasActivity.this,
//                                "Lat: " + mLastLocation.getLatitude() +
//                                        "\nLon: " + mLastLocation.getLongitude(),
//                                Toast.LENGTH_SHORT
//                        ).show();
                    }
                });
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
//                    Toast.makeText(
//                            FindGasActivity.this,
//                            "Lat: " + mLastLocation.getLatitude() +
//                                    "\nLon: " + mLastLocation.getLongitude(),
//                            Toast.LENGTH_SHORT
//                    ).show();
                }
            }
        };
        startLocationUpdates();
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
//                    @Override
//                    public void onLocationResult(LocationResult locationResult) {
//                        mLastLocation = locationResult.getLastLocation();
//                        if (mLastLocation == null) {
//                            return;
//                        }
//                        Toast.makeText(
//                                FindGasActivity.this,
//                                "Lat: " + mLastLocation.getLatitude() +
//                                        "Lon: " + mLastLocation.getLongitude(),
//                                Toast.LENGTH_SHORT
//                        ).show();
//                    }
//                },
//                Looper.myLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, locationCallback, null
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    protected void checkIfLoggedIn() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(FindGasActivity.this, MainActivity.class));
        }
    }

    protected void responseNoLocation() {
        Toast.makeText(
                FindGasActivity.this,
                "Could not get Location" +
                        "Please try again later\n",
                Toast.LENGTH_SHORT
        ).show();
    }

}
