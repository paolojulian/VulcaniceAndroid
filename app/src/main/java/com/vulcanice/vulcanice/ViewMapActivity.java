package com.vulcanice.vulcanice;

import android.content.Context;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoQuery;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.ClientRequest.RequestShopActivity;
import com.vulcanice.vulcanice.Model.Shop;
import com.vulcanice.vulcanice.Model.VCN_User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 06/08/2018.
 */

/*********
 * TODO
 * check if arrived
 * will view shop if arrived
 */
public class ViewMapActivity extends AppCompatActivity
        implements RoutingListener, com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener,
    GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {
    private final String TAG = "TAG_ViewMap";
    // FOR GEOQUERY
    private Integer radius = 2;
    private Boolean foundGas = false, isFirst = true;
    private GeoQuery geoQuery;

    private Context context;
    //LOCATION
    protected FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    protected LocationCallback locationCallback;
    protected LocationRequest mLocationRequest;
    //DATABASE
    protected DatabaseReference shopLocationRef;
    protected DatabaseReference usersRef, ownerRef;
    protected FirebaseDatabase mDatabase;
    protected VCN_User userModel;
    //MAP
    private GoogleMap mMap;
    private MapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    //MARKERS
    private Marker userMarker;
    private List<Marker> shopMarker;
    //LISTENER
    private Boolean isTracking = false;
    //INTERFACE
    private LinearLayout viewShop, viewLegend;
    private Button requestShop, trackShop;
    private TextView txtTracking, txtShopName, txtShopOwner, txtDistance;
    //SHOP DETAILS
    private String shopId, shopName;
    private Double shopLat, shopLng;
    //USER
    private LatLng userLocation;

    //INTERVAL
    private long UPDATE_INTERVAL = 5 * 1000; /* 10 seconds */
    private long FASTEST_INTERVAL = 2 * 1000; /* 2 seconds */
    private static final int PERMISSION_REQUEST_CODE = 7171;
    //ROUTE DISPLAY
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorAccent,R.color.colorPrimaryDark,R.color.colorAccent,R.color.primary_dark_material_light};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_map);

        Log.d(TAG, "Init");

        setLocation();
        setLocationRequest();
        setOnLocationUpdate();

        setupReferences();
        setupInterface();
        setupListeners();

        setupMap();
        getShops();

        context = ViewMapActivity.this;
    }

    private void setupReferences() {
        mDatabase = FirebaseDatabase.getInstance();
        usersRef = mDatabase.getReference().child("Users");
    }

    private void setupInterface() {
        // Routing
        polylines = new ArrayList<>();
        // LinearLayouts
        viewShop = findViewById(R.id.view_map_shop);
        viewLegend = findViewById(R.id.view_map_legend);
        // Button
        requestShop = findViewById(R.id.view_map_request);
        trackShop = findViewById(R.id.view_map_track);
        // Text
        txtTracking = findViewById(R.id.view_map_tracking);
        txtShopName = findViewById(R.id.view_map_shop_name);
        txtShopOwner = findViewById(R.id.view_map_shop_owner);
        txtDistance = findViewById(R.id.view_map_distance);
    }

    private void setupListeners() {
        trackShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shopLat == null) return;
                if (shopLng == null) return;

                isTracking = true;

                requestShop.setVisibility(View.GONE);
                trackShop.setVisibility(View.GONE);
                txtTracking.setVisibility(View.VISIBLE);
                txtDistance.setVisibility(View.VISIBLE);
                trackShop();
            }
        });

        requestShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shopId == null) return;
                if (shopName == null) return;

                Intent i = new Intent(context, RequestShopActivity.class);
                i.putExtra("shopId", shopId);
                i.putExtra("shopName", shopName);
                startActivity(i);
            }
        });
    }

    private void trackShop() {
        if (userLocation == null) return;
        if (shopLat == null) return;
        if (shopLng == null) return;

        LatLng shopLocation = new LatLng(shopLat, shopLng);

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(userLocation, shopLocation)
                .build();
        routing.execute();
    }

    private void setupMap() {
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.view_map);
        mapFragment.getMapAsync(ViewMapActivity.this);
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
                        setUserLocation(location);
                        userMarker = mMap.addMarker(new MarkerOptions()
                                .position(userLocation)
                                .title("Your Location")
                        );
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    }
                });
        // Used for repeating request
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    setUserLocation(location);
                    userMarker.setPosition(userLocation);
        //        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
                if (isTracking) {
                    trackShop();
                }
            }
        };
        startLocationUpdates();
    }

    private void setUserLocation(Location location) {
        mLastLocation = location;
        Toast.makeText(
                context,
                "Lat: " + mLastLocation.getLatitude() +
                        "\nLon: " + mLastLocation.getLongitude(),
                Toast.LENGTH_SHORT
        ).show();
        userLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
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

    private void getShops() {
        shopLocationRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Shops");

        shopLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    for (DataSnapshot shopList: child.getChildren()) {
                        Shop shop = shopList.getValue(Shop.class);
                        if (shop != null) {
                            drawShop(shop);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError + "");
                toast(R.string.db_error + "");
                finish();
            }
        });

    }

    private void drawShop(Shop shop) {
        LatLng location = new LatLng(
                Double.parseDouble(shop.getLatitude()),
                Double.parseDouble(shop.getLongitude())
        );

        BitmapDescriptor icon;
        String snippet;
        Integer tag;
        switch(shop.getType()) {
            case "Gasoline Station":
                icon = (BitmapDescriptorFactory.fromResource(R.mipmap.baseline_ev_station_black_24));
                snippet = "Gasoline Station";
                break;
            case "Vulcanizing Station":
                icon = (BitmapDescriptorFactory.fromResource(R.mipmap.baseline_local_car_wash_black_24));
                snippet = "Vulcanizing Station";
                break;
            case "Both":
                icon = (BitmapDescriptorFactory.fromResource(R.mipmap.baseline_store_mall_directory_black_24));
                snippet = "Gas/Vulcanizing Station";
                break;
            default:
                icon = (BitmapDescriptorFactory.fromResource(R.mipmap.baseline_store_mall_directory_black_24));
                snippet = "Gas/Vulcanizing Station";
                break;
        }
        Marker shopMarker = mMap.addMarker(new MarkerOptions()
            .position(location)
            .title(shop.getName())
            .snippet(snippet)
            .icon(icon)
        );
        shopMarker.setTag(shop);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Shop shop = (Shop) marker.getTag();
        /*
         * If pointer clicked is either your location or a null shop reference
         */
        if (shop == null) {
            viewShop.setVisibility(View.GONE);
            txtTracking.setVisibility(View.GONE);
            viewLegend.setVisibility(View.VISIBLE);
            return false;
        }
        viewShop.setVisibility(View.VISIBLE);
        requestShop.setVisibility(View.VISIBLE);
        trackShop.setVisibility(View.VISIBLE);
        txtTracking.setVisibility(View.GONE);
        txtDistance.setVisibility(View.GONE);
        viewLegend.setVisibility(View.GONE);

        /*
         * Gasoline station doesnt have request
         */
        if (shop.getType().equals("Gasoline Station")) {
            requestShop.setVisibility(View.GONE);
        } else {
            requestShop.setVisibility(View.VISIBLE);
        }

        /*
         * Set xml text
         */
        Location shopLocation = new Location("shopLocation");
        shopLocation.setLatitude(Double.parseDouble(shop.getLatitude()));
        shopLocation.setLongitude(Double.parseDouble(shop.getLongitude()));

        txtShopName.setText(shop.getName());
        setOwnerName(shop.getOwner());
        /*
         * Set shop details
         */
        shopId = shop.getOwner() + "_" + shop.getName();
        shopName = shop.getName();
        shopLat = Double.parseDouble(shop.getLatitude());
        shopLng = Double.parseDouble(shop.getLongitude());

        return false;
    }

    private void setOwnerName(String ownerId) {
        ownerRef = usersRef.child(ownerId);

        ownerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(VCN_User.class);
                if (userModel == null) {
                    return;
                }
                txtShopOwner.setText(userModel.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        mMap.setOnMarkerClickListener(this);
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

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
        }
        stopLocationUpdates();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(
                context,
                "Latdd: " + location.getLatitude() +
                        "\nLondd: " + location.getLongitude(),
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        removeRoute();
        polylines = new ArrayList<>();
        //add route(s) to the map.
//        for (int i = 0; i <route.size(); i++) {
//            //Only 2 routes is available
//            if (i == 2) {
//                break;
//            }
//            //In case of more than 5 alternative routes
            int colorIndex = shortestRouteIndex % COLORS.length;
            Route route = routes.get(shortestRouteIndex);

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + shortestRouteIndex * 3);
            polyOptions.addAll(route.getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            txtDistance.setText("Distance: - " + route.getDistanceValue() + "m");

//        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void toast(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void removeRoute() {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
    }
}
