package com.vulcanice.vulcanice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 11/06/2018.
 */

public class TrackShopActivity extends AppCompatActivity implements RoutingListener, com.google.android.gms.location.LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {
    //DATABASE
    private LatLng shopLocation;
    //MAP
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    //LABELS
    private TextView routeLabel, routeDistance, routeDuration;
    private TextView routeLabel2, routeDistance2, routeDuration2;
    private LinearLayout route2;
    //USER LOCATION
    protected FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    protected LocationCallback locationCallback;
    protected LocationRequest mLocationRequest;
    //MARKER
    private MarkerOptions userMarker, shopMarker;
    //ROUTE DISPLAY
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorAccent,R.color.colorPrimaryDark,R.color.colorAccent,R.color.primary_dark_material_light};
    //PERMISSION
    private static final int PERMISSION_REQUEST_CODE = 7171;
    //INTERVAL
    private long UPDATE_INTERVAL = 5 * 1000; /* 10 seconds */
    private long FASTEST_INTERVAL = 2 * 1000; /* 2 seconds */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_shop);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.INTERNET
            },PERMISSION_REQUEST_CODE);
        }

        setDatas();
        setupMap();
        setLocationRequest();
        setLocation();
        setOnLocationUpdate();
    }

    private void setDatas() {
        polylines = new ArrayList<>();
        getIntentData();
        userMarker = new MarkerOptions();
        shopMarker = new MarkerOptions()
                .position(shopLocation)
                .snippet("Shop Location")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.baseline_ev_station_black_24));
        routeLabel = findViewById(R.id.label_1);
        routeDistance = findViewById(R.id.distance_1);
        routeDuration = findViewById(R.id.duration_1);

        routeLabel2 = findViewById(R.id.label_2);
        routeDistance2 = findViewById(R.id.distance_2);
        routeDuration2 = findViewById(R.id.duration_2);

        route2 = findViewById(R.id.route_2);
    }

    private void getIntentData() {
        Intent i = getIntent();
        Double shopLng = i.getExtras().getDouble("shopLng");
        Double shopLat = i.getExtras().getDouble("shopLat");
        shopLocation = new LatLng(shopLat, shopLng);
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

    private void setOnLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            return;
                        }
                        mLastLocation = location;
                        LatLng userLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        drawMarker(userLocation);
                        drawMarker(userLocation);
                    }
                });
        // Used for repeating request
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
                }
                LatLng userLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                drawRoute(userLocation);
            }
        };
        startLocationUpdates();
    }

    private void setupMap() {
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_track_shop);
        mapFragment.getMapAsync(TrackShopActivity.this);
    }

    private void getRouteToShop() {
    }

    private void drawMarker(LatLng userLocation) {
        mMap.addMarker(userMarker.position(userLocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    private void drawRoute(LatLng userLocation) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(userLocation, shopLocation)
                .build();
        routing.execute();
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {
            //Only 2 routes is available
            if (i == 2) {
                break;
            }
            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            displayRoute(i, route.get(i));
        }
    }

    private void displayRoute(int i, Route route) {
        String rLabel = "Route " + (i+1);
        String rDistance = "Distance: - " + route.getDistanceValue() + "m";
        String rDuration = "Duration: - " + convertToTime(route.getDurationValue());

        if (i == 1) {
            route2.setVisibility(View.VISIBLE);
            routeLabel2.setText(rLabel);
            routeDistance2.setText(rDistance);
            routeDuration2.setText(rDuration);
            return;
        }
        routeLabel.setText(rLabel);
        routeDistance.setText(rDistance);
        routeDuration.setText(rDuration);
    }

    private String convertToTime(Integer seconds) {
        String time = "";
        Integer hour = 0, min = 0;
        Boolean hasHour = false, hasMin = false;
        if (seconds >= 3600) {
            hour = (int) Math.floor(seconds / 3600);
            seconds = seconds - (hour * 3600);
            hasHour = true;
        }
        if (seconds >= 60) {
            min = (int) Math.floor(seconds / 60);
            seconds = seconds - (min * 60);
            hasMin = true;
        }
        if (hasHour) {
            if (hour < 10) {
                time += "0" + hour + ":";
            } else {
                time += hour + ":";
            }
        }
        if (hasMin) {
            if (min < 10) {
                time += "0" + min + ":";
            } else {
                time += min + ":";
            }
        }
        return time += seconds;
    }

    @Override
    public void onRoutingCancelled() {
        erasePolyLines();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.INTERNET
            },PERMISSION_REQUEST_CODE);
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng userLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            drawMarker(userLocation);
            drawMarker(userLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { mGoogleApiClient.connect(); }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        erasePolyLines();
    }

    public void erasePolyLines() {
        for(Polyline line: polylines) {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.INTERNET
            },PERMISSION_REQUEST_CODE);
        }
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.addMarker(shopMarker);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
