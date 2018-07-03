package com.vulcanice.vulcanice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.Shop;

public class ViewNearestShopActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    //STRINGS
    private String shopId, shopType;
    private Float currentShopDistance;
    private Double shopLat, shopLng;
    //VIEW
    private AppCompatTextView shopName, shopAddress, constShopType, shopDistance;
    private Button btnTrackShop, btnRequestShop;
    //MODEL
    private String dbGas, dbVul, dbBoth;
    private Shop shopModel;
    //LOCATION
    protected FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    protected LocationCallback locationCallback;
    protected LocationRequest mLocationRequest;
    //INTERVAL
    private long UPDATE_INTERVAL = 10 * 1000; /* 10 seconds */
    private long FASTEST_INTERVAL = 5 * 1000; /* 2 seconds */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_nearest_shop);

        initData();
        eventTrackShop();
        eventRequestShop();

        setLocationRequest();
        setLocation();
        setOnLocationUpdate();

        displayShopDetails();
    }

    protected void initData() {
        setupDbNames();
        setupExtras();
        setupView();
        setupFirebase();
        setActivityTitle();
    }

    private void setupDbNames() {
        dbGas = this.getString(R.string.db_gas);
        dbVul = this.getString(R.string.db_vul);
        dbBoth = "both";
    }

    private void setupExtras() {
        Intent i                = getIntent();
        shopId                  = i.getExtras().getString("shopId");
        shopType                = i.getExtras().getString("shopType");
        shopLat                 = i.getExtras().getDouble("shopLat");
        shopLng                 = i.getExtras().getDouble("shopLng");
        currentShopDistance     = i.getExtras().getFloat("shopDistance");
    }

    private void setupView() {
        shopName        = findViewById(R.id.shop_name);
        shopAddress     = findViewById(R.id.shop_address);
        shopDistance    = findViewById(R.id.shop_distance);
        btnTrackShop    = findViewById(R.id.btn_track_shop);
        btnRequestShop  = findViewById(R.id.btn_request_shop);

        if (shopType.equals(dbGas)) {
            btnRequestShop.setVisibility(View.GONE);
            return;
        }
        btnRequestShop.setVisibility(View.VISIBLE);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
    }

    protected void setActivityTitle() {
        if (shopType.equals(dbGas)) {
            setTitle("Nearest Gas Station");
            return;
        }
        if (shopType.equals(dbVul)) {
            setTitle("Nearest Vulcanizing Station");
            return;
        }
        setTitle("Nearest Station");
    }

    private void eventTrackShop() {
        btnTrackShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackShop();
            }
        });
    }

    private void trackShop() {
        Intent i = new Intent(
                ViewNearestShopActivity.this,
                TrackShopActivity.class
        );
        i.putExtra("shopLat", shopLat);
        i.putExtra("shopLng", shopLng);
        startActivity(i);
    }

    private void eventRequestShop() {
        if (shopType.equals(dbGas)) {
            return;
        }
        btnRequestShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestShop();
            }
        });
    }

    private void requestShop() {
        Intent i = new Intent(
                ViewNearestShopActivity.this,
                RequestShopActivity.class
        );
        i.putExtra("shopId", shopId);
        i.putExtra("shopName", shopName.getText().toString());
        startActivity(i);
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
                        showDistance();
                        Log.d("userLat",
                                "Lat: " + mLastLocation.getLatitude() + "\n" +
                                        "Lon: " + mLastLocation.getLongitude()
                        );
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
                showDistance();
                Log.d("userLat",
                        "Lat: " + mLastLocation.getLatitude() + "\n" +
                                "Lon: " + mLastLocation.getLongitude()
                );
            }
        };
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, locationCallback, null
        );
    }

    protected void displayShopDetails() {
        shopDistance.setText(currentShopDistance + " m");
        DatabaseReference shopReference = mDatabase.getReference()
                .child("Shops").child(shopType).child(shopId);

        shopReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shopModel = dataSnapshot.getValue(Shop.class);
                if (shopModel == null) {
                    return;
                }
                shopName.setText(shopModel.getName());
                shopAddress.setText(shopModel.getDescription());
                shopLat = Double.valueOf(shopModel.getLatitude());
                shopLng = Double.valueOf(shopModel.getLongitude());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewNearestShopActivity.this, "@string/db_error", Toast.LENGTH_SHORT).show();
            }
        });

    }
    protected void responseNoLocation() {
        Toast.makeText(
                ViewNearestShopActivity.this,
                "Could not get Location\n" +
                        "Please wait to connect",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void showDistance() {
        if(mLastLocation == null) {
            return;
        }
        Location locationShop = new Location("");
        locationShop.setLongitude(shopLng);
        locationShop.setLatitude(shopLat);

        float distanceInMeters = mLastLocation.distanceTo(locationShop);
        shopDistance.setText(distanceInMeters + " m");
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
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
    protected void onStop() {
        super.onStop();
    }
    @Override
    public void onBackPressed() {
        startActivity(
                new Intent(ViewNearestShopActivity.this, MainPage.class)
        );
        super.onStop();
    }
}

