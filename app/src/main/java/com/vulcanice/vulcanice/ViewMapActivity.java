package com.vulcanice.vulcanice;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.Manifest;

/**
 * Created by User on 06/08/2018.
 */

public class ViewMapActivity extends AppCompatActivity{
    // FOR GEOQUERY
    private Integer radius = 2;
    private Boolean foundGas = false, isFirst = true;
    private String shopId;
    private Double shopLat, shopLng;
    private GeoQuery geoQuery;

    private Context context;
    //LOCATION
    protected FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    protected LocationCallback locationCallback;
    protected LocationRequest mLocationRequest;

    //INTERVAL
    private long UPDATE_INTERVAL = 5 * 1000; /* 10 seconds */
    private long FASTEST_INTERVAL = 2 * 1000; /* 2 seconds */

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_map);
        setLocation();
        setLocationRequest();
        setOnLocationUpdate();
//        getClosestShop(R.string.db_gas + "");
//        getClosestShop(R.string.db_both + "");
//        getClosestShop(R.string.db_vul + "");

        context = ViewMapActivity.this;
    }

    private void setOnLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                        Toast.makeText(
                                context,
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
                            context,
                            "Lat: " + mLastLocation.getLatitude() +
                                    "\nLon: " + mLastLocation.getLongitude(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        };
        startLocationUpdates();
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

    /******
     *
     */
    private void setLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getClosestShop(String shopType) {
        DatabaseReference shopLocationRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Locations")
                .child(shopType);

        GeoFire geoFire = new GeoFire(shopLocationRef);

        geoQuery = geoFire.queryAtLocation(
                new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                radius
        );

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                shopId = key;
            }

            @Override
            public void onKeyExited(String key) {
                Toast.makeText(
                        context,
                        "Shop has been removed",
                        Toast.LENGTH_SHORT
                ).show();
                startActivity(new Intent(context, MainPage.class));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Toast.makeText(
                        context,
                        "Shop has been moved",
                        Toast.LENGTH_SHORT
                ).show();
                startActivity(new Intent(context, MainPage.class));
            }

            @Override
            public void onGeoQueryReady() {
                geoQuery.removeAllListeners();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(
                        context,
                        "@string/db_error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            responseNoLocation();
            return;
        }
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, locationCallback, null
        );
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    protected void responseNoLocation() {
        Toast.makeText(
                context,
                "Could not get Location\n" +
                        "Please wait to connect",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }
        stopLocationUpdates();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }
}
