package com.vulcanice.vulcanice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.mLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 11/06/2018.
 */

public class TrackRequestActivity extends AppCompatActivity implements RoutingListener, com.google.android.gms.location.LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {
    //MODEL
    private DatabaseReference mDatabaseRef;
    private FirebaseUser owner;
    private String clientUid;
    //LOCATION
    private mLocation clientLocation, ownerLocation;
    private LatLng clientLatLng, ownerLatLng;
    //MAP
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    //USER LOCATION
    protected FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    protected LocationCallback locationCallback;
    protected LocationRequest mLocationRequest;
    //MARKER
    private MarkerOptions clientMarker, ownerMarker;
    //ROUTE DISPLAY
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorSemiLight,R.color.colorLight,R.color.colorAccent,R.color.primary_dark_material_light};
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

        polylines = new ArrayList<>();
        getIntentData();
        setupDatabase();
        setupOwnerLocation();
        setupClientLocation();
        setupMarker();
        setupMap();
    }

    private void setupClientLocation() {
        DatabaseReference clientRef = mDatabaseRef.child("clientLocation")
                .child(clientUid);

        clientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( ! dataSnapshot.exists()) {
                    return;
                }
                clientLocation = dataSnapshot.getValue(mLocation.class);
                clientLatLng = new LatLng(clientLocation.getLatitude(), clientLocation.getLongitude());
                setupMarker();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void setupOwnerLocation() {
        setLocationRequest();
        setLocation();
        setOnLocationUpdate();
    }

    private void setupDatabase() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        owner = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setupMarker() {
        if (clientLatLng == null || ownerLatLng == null) {
            return;
        }
        clientMarker = new MarkerOptions()
                .position(clientLatLng)
                .snippet("Client Location")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.baseline_person_black_24));
        ownerMarker = new MarkerOptions()
                .position(ownerLatLng)
                .snippet("Your Location");
        mMap.addMarker(ownerMarker);
    }

    private void getIntentData() {
        Intent i = getIntent();
        clientUid = i.getExtras().getString("clientUid");
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
                        setupMarker();
                        getRouteToShop();
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
                setupMarker();
                getRouteToShop();
            }
        };
        startLocationUpdates();
    }

    private void setupMap() {
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_track_shop);
        mapFragment.getMapAsync(TrackRequestActivity.this);
    }

    private void getRouteToShop() {
        ownerLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        drawMarker();
        drawRoute();
    }

    private void drawMarker() {
        mMap.addMarker(clientMarker.position(clientLatLng).title("Client Location"));
        mMap.addMarker(ownerMarker.position(ownerLatLng).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ownerLatLng, 15));
    }

    private void drawRoute() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(clientLatLng, ownerLatLng)
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

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

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
            getRouteToShop();
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

