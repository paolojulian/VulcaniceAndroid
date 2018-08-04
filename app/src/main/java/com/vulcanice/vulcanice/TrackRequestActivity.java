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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private DatabaseReference mDatabaseRef, myLocationRef, clientRef;
    private FirebaseUser currentUser;
    private String theirUid;
    private String locationReference;
    //LOCATION
    private mLocation theirLocation, myLocation;
    private LatLng theirLatLng, myLatLng;
    //LABELS
    private TextView routeLabel, routeDistance, routeDuration;
    private TextView routeLabel2, routeDistance2, routeDuration2;
    private LinearLayout route2;
    //STRINGS
    private String mType;
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
    private MarkerOptions theirMarker, myMarker;
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

        polylines = new ArrayList<>();
        getIntentData();
        setupDatabase();
        setupMyLocation();
        setupTheirLocation();
        setupMarker();
        setupMap();
    }

    private void getIntentData() {
        Intent i = getIntent();
        mType = i.getExtras().getString("type");
        theirUid = i.getExtras().getString("id");

        routeLabel = findViewById(R.id.label_1);
        routeDistance = findViewById(R.id.distance_1);
        routeDuration = findViewById(R.id.duration_1);

        routeLabel2 = findViewById(R.id.label_2);
        routeDistance2 = findViewById(R.id.distance_2);
        routeDuration2 = findViewById(R.id.duration_2);

        route2 = findViewById(R.id.route_2);
    }

    private void setupDatabase() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mType.equals("client")) {
            getSupportActionBar().setTitle("Tracking Owner");
            myLocationRef = FirebaseDatabase.getInstance()
                    .getReference("clientLocation")
                    .child(theirUid)
                    .child(currentUser.getUid());
        } else {
            getSupportActionBar().setTitle("Tracking Client");
            myLocationRef = FirebaseDatabase.getInstance()
                    .getReference("ownerLocation")
                    .child(currentUser.getUid())
                    .child(theirUid);
        }

    }

    private void setupTheirLocation() {
        if (mType.equals("client")) {
            clientRef = mDatabaseRef.child("ownerLocation")
                    .child(theirUid)
                    .child(currentUser.getUid());
        } else {
            clientRef = mDatabaseRef.child("clientLocation")
                    .child(currentUser.getUid())
                    .child(theirUid);
        }

        clientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( ! dataSnapshot.exists()) {
                    return;
                }
                theirLocation = dataSnapshot.getValue(mLocation.class);
                Log.d("theirLocation", theirLocation.getLatitude() + "");
                theirLatLng = new LatLng(theirLocation.getLatitude(), theirLocation.getLongitude());
                setupMarker();
                getRouteToTheir();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void setupMyLocation() {
        setLocationRequest();
        setLocation();
        setOnLocationUpdate();
    }

    private void setupMap() {
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_track_shop);
        mapFragment.getMapAsync(TrackRequestActivity.this);
    }

    private void setupMarker() {
        if (theirLatLng == null || myLatLng == null) {
            return;
        }
        String theirSnippet;
        if (mType.equals("client")) {
            theirSnippet = "Owner Location";
        } else {
            theirSnippet = "Client Location";
        }
        theirMarker = new MarkerOptions()
                .position(theirLatLng)
                .snippet(theirSnippet)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.baseline_person_black_24));
        myMarker = new MarkerOptions()
                .position(myLatLng)
                .snippet("Current Location");
        mMap.clear();
        mMap.addMarker(myMarker);
        mMap.addMarker(theirMarker);
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
                        updateMyLocation();
//                        setupMarker();
                        getRouteToTheir();
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
                if (mLastLocation == null) {
                    return;
                }
                updateMyLocation();
//                setupMarker();
                getRouteToTheir();
            }
        };
        startLocationUpdates();
    }

    private void updateMyLocation() {
        myLocation = new mLocation();
        myLocation.setLatitude(mLastLocation.getLatitude());
        myLocation.setLongitude(mLastLocation.getLongitude());
        myLocationRef.setValue(myLocation);

        myLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
    }

    private void getRouteToTheir() {
        drawMarker();
        drawRoute();
    }

    private void drawMarker() {
        String theirTitle;
        if (mType.equals("client")) {
            theirTitle = "Owner Location";
        } else {
            theirTitle = "Client Location";
        }
        mMap.clear();
        if (theirMarker != null) {
            mMap.addMarker(theirMarker.position(theirLatLng).title(theirTitle));
        }
        if (myMarker != null) {
            mMap.addMarker(myMarker.position(myLatLng).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18));
        }
    }

    private void drawRoute() {
        if (theirMarker == null || myMarker == null) {
            return;
        }
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(myLatLng, theirLatLng)
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
            getRouteToTheir();
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
        mFusedLocationClient.removeLocationUpdates(locationCallback);
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
        mLastLocation = location;
    }

    @Override
    public void onBackPressed() {
        startActivity(
                new Intent(TrackRequestActivity.this, MainPage.class)
        );
        super.onStop();
    }
}

