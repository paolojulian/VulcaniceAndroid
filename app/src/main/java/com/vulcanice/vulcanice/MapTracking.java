package com.vulcanice.vulcanice;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.Tracking;

import java.text.DecimalFormat;

public class MapTracking extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String email;
    DatabaseReference locations;

    Double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locations = FirebaseDatabase.getInstance().getReference("Locations");

        if ( getIntent() != null )
        {
            email = getIntent().getStringExtra("email");
            lat = getIntent().getDoubleExtra("lat", 0);
            lng = getIntent().getDoubleExtra("lng", 0);
        }

        if ( ! TextUtils.isEmpty(email) )
        {
            loadLocationForThisShop(email);
        }
    }

    private void loadLocationForThisShop(String email) {
        Query user_location = locations.orderByChild("email").equalTo(email);

        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Tracking tracking = postSnapShot.getValue(Tracking.class);
                    LatLng friendLocation = new LatLng(Double.parseDouble(tracking.getLatitude()),
                                                    Double.parseDouble(tracking.getLongitude()));;
                    Location currentUser = new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);

                    Location friend = new Location("");
                    friend.setLatitude(Double.parseDouble(tracking.getLatitude()));
                    friend.setLongitude(Double.parseDouble(tracking.getLongitude()));

                    getDistance(currentUser, friend);

                    mMap.addMarker(new MarkerOptions()
                            .position(friendLocation)
                            .title(tracking.getEmail())
                            .snippet("Distance " + new DecimalFormat("#.#")
                                    .format(getDistance(currentUser, friend)))
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    );
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12.0f));
                }
                LatLng currentMarker = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(currentMarker)
                        .title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private double getDistance(Location currentUser, Location friend) {
        double theta = currentUser.getLongitude() - friend.getLongitude();
        double dist = Math.sin( deg2rad(currentUser.getLatitude()) )
                    * Math.sin( deg2rad(friend.getLatitude()) )
                    * Math.cos( deg2rad(currentUser.getLatitude()) )
                    * Math.cos( deg2rad(friend.getLatitude()) )
                    * Math.cos( deg2rad(theta) );
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double deg2rad(double deg) {
        return(deg * Math.PI / 180.0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
