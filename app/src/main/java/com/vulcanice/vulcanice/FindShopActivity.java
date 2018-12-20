package com.vulcanice.vulcanice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ProgressBar;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.vulcanice.vulcanice.Library.SelfLocation;
import com.vulcanice.vulcanice.Model.Shop;

public class FindShopActivity extends AppCompatActivity {
    private final String TAG = "TAG_" + FindShopActivity.class.getName();
    private Context context;
    //DATABASE
    protected FirebaseUser user;
    //VIEW
    protected ProgressBar progressBar;
    //LOCATION
    protected SelfLocation selfLocation;
    protected FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    protected LocationCallback locationCallback;
    protected LocationRequest mLocationRequest;
    //TYPE
    protected String shopType, dbGas, dbVul, dbBoth;
    private static final int PERMISSION_REQUEST_CODE = 7171;
    /*
    * gasStation
    * vulcanizeStation
    * */
    //INTERVAL
    private long UPDATE_INTERVAL = 5 * 1000; /* 10 seconds */
    private long FASTEST_INTERVAL = 2 * 1000; /* 2 seconds */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_nearest_shop);
        Log.d(TAG, "Init");
        context = FindShopActivity.this;

        checkIfLoggedIn();
        setData();
        setActivityTitle();
        //GET LOCATION
        selfLocation = new SelfLocation(context);
//        setLocationRequest();
//        setLocation();
        setOnLocationUpdate();
//        listGas();
    }

    private void setData() {
        Intent i = getIntent();
        //extra
        shopType = i.getExtras().getString("shopType");
        dbGas = this.getString(R.string.db_gas);
        dbVul = this.getString(R.string.db_vul);
        dbBoth = "both";
    }

    private void setActivityTitle() {
        if (shopType.equals(dbGas)) {
            setTitle("Finding nearest Gas Station");
            return;
        }
        if (shopType.equals(dbVul)) {
            setTitle("Finding nearest Vulcanizing Station");
            return;
        }
        setTitle("Finding nearest Station");

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
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.INTERNET
            },PERMISSION_REQUEST_CODE);
            return;
        }
        selfLocation.getFusedLocationClient().getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.d(TAG, "No Location: " + location);
                            responseNoLocation();
                            return;
                        }
                        mLastLocation = location;
                        getClosestShop();
                        Toast.makeText(
                                FindShopActivity.this,
                                "Lat: " + mLastLocation.getLatitude() +
                                        "\nLon: " + mLastLocation.getLongitude(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
        // Used for repeating request
        selfLocation.setLocationCallback(new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
                    Toast.makeText(
                            FindShopActivity.this,
                            "Lat: " + mLastLocation.getLatitude() +
                                    "\nLon: " + mLastLocation.getLongitude(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
        selfLocation.startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selfLocation != null) {
            selfLocation.startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (selfLocation != null) {
            selfLocation.stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }
        super.onStop();
    }

    protected void checkIfLoggedIn() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(FindShopActivity.this, MainActivity.class));
        }
    }

    protected void responseNoLocation() {
        Toast.makeText(
                FindShopActivity.this,
                "Could not get Location\n" +
                        "Please wait to connect",
                Toast.LENGTH_SHORT
        ).show();
    }

    private Integer radius = 2;
    private Boolean foundGas = false, isFirst = true;
    private String shopId;
    private Double shopLat, shopLng;
    private GeoQuery geoQuery;
    private void getClosestShop() {
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
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                setFoundGas();
                shopId = key;
                setNewClosestShop(location);
            }


            @Override
            public void onKeyExited(String key) {
                Toast.makeText(
                        FindShopActivity.this,
                        "Shop has been removed",
                        Toast.LENGTH_SHORT
                ).show();
                startActivity(new Intent(FindShopActivity.this, MainPage.class));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Toast.makeText(
                        FindShopActivity.this,
                        "Shop has been moved",
                        Toast.LENGTH_SHORT
                ).show();
                startActivity(new Intent(FindShopActivity.this, MainPage.class));
            }

            @Override
            public void onGeoQueryReady() {
                if (radius == 10) {
                    Toast.makeText(FindShopActivity.this, "No Shop/s Found\n within 10km", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ( ! foundGas) {
                    radius++;
                    getClosestShop();
                    return;
                }
                viewShop();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(
                        FindShopActivity.this,
                        "@string/db_error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void setFoundGas() {
        if(foundGas) {
            return;
        }
        foundGas = true;
    }

    private void setNewClosestShop(GeoLocation location) {
        if(isFirst) {
            shopLat = location.latitude;
            shopLng = location.longitude;
            isFirst = false;
            return;
        }
        setIfCloser(location);
    }

    private void setIfCloser(GeoLocation location) {
        if( isCloser(location.latitude, location.longitude, shopLat, shopLng) ) {
            shopLat = location.latitude;
            shopLng = location.longitude;
        }
    }

    private void viewShop() {
        Intent i = new Intent(FindShopActivity.this, ViewNearestShopActivity.class);
        i.putExtra("shopId", shopId);
        i.putExtra("shopType", shopType);
        i.putExtra("shopLat", shopLat);
        i.putExtra("shopLng", shopLng);
        startActivity(i);
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
