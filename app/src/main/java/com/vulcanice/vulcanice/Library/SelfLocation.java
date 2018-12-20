package com.vulcanice.vulcanice.Library;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class SelfLocation implements com.google.android.gms.location.LocationListener,
    GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private Context context;
    //USER LOCATION
    protected FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    protected LocationCallback locationCallback;
    protected LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000; /* 10 seconds */
    private long FASTEST_INTERVAL = 2 * 1000; /* 2 seconds */

    public SelfLocation() {}

    public SelfLocation(Context context) {
        this.context = context;
        this.mLocationRequest = new LocationRequest();
        this.mLocationRequest.setInterval(UPDATE_INTERVAL);
        this.mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        this.mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(this.mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public LocationCallback getLocationCallback() {
        return this.locationCallback;
    }

    public void setLocationCallback(LocationCallback lc) {
        this.locationCallback = lc;
    }

    public FusedLocationProviderClient getFusedLocationClient() {
        if (ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return this.mFusedLocationClient;
    }

    public LocationRequest getLocationRequest() {
        return this.mLocationRequest;
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (this.mFusedLocationClient == null) {
            return;
        }
        this.mFusedLocationClient.requestLocationUpdates(
                this.mLocationRequest, this.locationCallback, null
        );
    }

    public void stopLocationUpdates() {
        if (mFusedLocationClient == null) {
            return;
        }
        this.mFusedLocationClient.removeLocationUpdates(this.locationCallback);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
