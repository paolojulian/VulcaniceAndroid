package com.vulcanice.vulcanice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vulcanice.vulcanice.Model.Shop;

/**
 * Created by paolo on 5/27/18.
 */

public class CreateShopActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    //LAYOUT
    private EditText shopName, shopDescription;
    private AppCompatButton btnSubmitShop;
    private ProgressBar progressBar;
    private Spinner shopType;
    //MODEL
    private DatabaseReference mDatabase;
    private Shop mShop;
    private FirebaseUser user;
    //MAP
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    //STATIC_VALUES
    private static final int PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RES_REQUEST = 7172;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shop);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(CreateShopActivity.this, MainActivity.class));
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.INTERNET
            },PERMISSION_REQUEST_CODE);
        }
        mShop = new Shop();
        setupMap();
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        eventCreateShop();
    }

    private void setupMap() {
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.create_shop_map);
        mapFragment.getMapAsync(this);
    }

    private void eventCreateShop() {
        btnSubmitShop = findViewById(R.id.btn_shop_create);
        btnSubmitShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                setInputValues();
                saveShop();
            }
        });
    }

    private void setInputValues() {
        shopName = findViewById(R.id.input_shop_name);
        shopDescription = findViewById(R.id.input_shop_description);
        shopType = findViewById(R.id.spinner_shop_type);
        mShop.setName(shopName.getText().toString().trim());
        mShop.setDescription(shopDescription.getText().toString().trim());
        mShop.setType(shopType.getSelectedItem().toString().trim());
        mShop.setOwner(user.getUid());
        if ( ! mShop.is_valid()) {
            Toast.makeText(
                    CreateShopActivity.this,
                    "Please fill up the missing fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if ( ! mShop.is_location_valid()) {
            Toast.makeText(
                    CreateShopActivity.this,
                    "Invalid Location",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
    }

    private String getShopType(String shopType) {
        Log.d("ShopType", shopType);
        if (shopType.equals("Gasoline Station")) {
            return "gasStation";
        } else if (shopType.equals("Vulcanizing Station")) {
            return "vulcanizeStation";
        } else {
            return "both";
        }
    }

    private void saveShop() {
        String shopType = getShopType(mShop.getType());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference shopReference = mDatabase.child("PendingShops")
                .child(shopType).child(user.getUid() + "_" + mShop.getName());

        shopReference.setValue(mShop)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                CreateShopActivity.this,
                                "Successfully Requested Shop\nPlease wait for Authorized users to Confirm your Shop",
                                Toast.LENGTH_SHORT
                        ).show();
                        startActivity(
                                new Intent(CreateShopActivity.this, DashBoard.class)
                        );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                CreateShopActivity.this,
                                "@string/db_error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
//        DatabaseReference mLocationRef = mDatabase.child("Locations")
//                .child(shopType);
//        GeoFire geoFire = new GeoFire(mLocationRef);
//        geoFire.setLocation(
//                user.getUid() + "_" + mShop.getName(),
//                new GeoLocation(
//                        Double.parseDouble(mShop.getLatitude()),
//                        Double.parseDouble(mShop.getLongitude())
//                ),
//                new GeoFire.CompletionListener() {
//                    @Override
//                    public void onComplete(String key, DatabaseError error) {
//                        startActivity(
//                                new Intent(CreateShopActivity.this, DashBoard.class)
//                        );
//                    }
//                }
//        );
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
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                mShop.setLocation(
                        String.valueOf(latLng.latitude),
                        String.valueOf(latLng.longitude)
                );
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.addMarker(markerOptions);
            }
        });
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
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            mShop.setLocation(
                    String.valueOf(lat),
                    String.valueOf(lng)
            );

            LatLng loc = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(loc).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

        }
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Location services connection failed!!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
    @Override
    public void onLocationChanged(Location location) {

    }

    public void onConnectionSuspended(int i) { mGoogleApiClient.connect(); }
}
