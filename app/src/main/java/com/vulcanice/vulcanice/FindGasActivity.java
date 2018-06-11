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
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.Shop;
import com.vulcanice.vulcanice.Model.VCN_User;

import java.util.concurrent.atomic.AtomicInteger;

public class FindGasActivity extends AppCompatActivity {
    //DATABASE
    protected FirebaseUser user;
    protected DatabaseReference vulcanizeRef;
    protected Query vulcanizeQuery;
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
                .child("Gasoline Station");
        mListGas = findViewById(R.id.list_gas_station);

        firebaseAdapter = new FirebaseRecyclerAdapter<Shop, ListGasViewHolder>
                (Shop.class, R.layout.listview_nearest_gas, ListGasViewHolder.class, vulcanizeRef) {
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
                        getClosestGasStation();
                        Toast.makeText(
                                FindGasActivity.this,
                                "Lat: " + mLastLocation.getLatitude() +
                                        "\nLon: " + mLastLocation.getLongitude(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
        // Used for repeating request
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
                    Toast.makeText(
                            FindGasActivity.this,
                            "Lat: " + mLastLocation.getLatitude() +
                                    "\nLon: " + mLastLocation.getLongitude(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        };
        startLocationUpdates();
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
                "Could not get Location\n" +
                        "Please wait to connect",
                Toast.LENGTH_SHORT
        ).show();
    }

    private Integer radius = 2;
    private Boolean foundGas = false, isFirst = true;
    private String shopId;
    private Double shopLat, shopLng;
    private void getClosestGasStation() {
        DatabaseReference gasLocation = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Locations")
                .child("gasStation");

        GeoFire geoFire = new GeoFire(gasLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                radius
        );
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if( ! foundGas) {
                    foundGas = true;
                }
                shopId = key;
                if(isFirst) {
                    shopLat = location.latitude;
                    shopLng = location.longitude;
                    isFirst = false;
                } else {
                    if(isCloser(
                            location.latitude, location.longitude,
                            shopLat, shopLng
                    )) {
                        shopLat = location.latitude;
                        shopLng = location.longitude;
                    }
                }
            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("geoFireMoved", "Lat: " + location.latitude);
            }

            @Override
            public void onGeoQueryReady() {
                if ( ! foundGas) {
                    radius++;
                    getClosestGasStation();
                    return;
                }
                Intent i = new Intent(FindGasActivity.this, ViewNearestGasActivity.class);
                i.putExtra("shopId", shopId);
                i.putExtra("shopType", "gasStation");
                i.putExtra("shopLat", shopLat);
                i.putExtra("shopLng", shopLng);
                startActivity(i);
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(
                        FindGasActivity.this,
                        "@string/db_error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    public Boolean isCloser(Double lat1, Double lng1, Double lat2, Double lng2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lng1);

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lng2);

        float distanceInMeters1 = mLastLocation.distanceTo(location1);
        float distanceInMeters2 = mLastLocation.distanceTo(location2);

        return distanceInMeters2 < distanceInMeters1;
    }

}
