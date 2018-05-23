package com.vulcanice.vulcanice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by paolo on 5/3/18.
 */

public class DashBoardTab1 extends Fragment {
    private static final String TAG = "Tab1Fragment";
    private Button btnTab1;
    DatabaseReference gas_stations, vulcanize_stations;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dash_board_1, container, false);
        btnTab1 = view.findViewById(R.id.btnTab1);

        btnTab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "TEST TAB1", Toast.LENGTH_SHORT).show();
            }
        });

        gas_stations = FirebaseDatabase.getInstance().getReference("Locations");
        vulcanize_stations = FirebaseDatabase.getInstance().getReference("Locations");

        return view;
    }

    private double get_distance(double lat_user, double lon_user, double lat_location, double lon_location) {
        double theta = lon_user - lon_location;
        double dist = Math.sin( deg2rad(lat_user) ) * Math.sin( deg2rad(lat_location) )
                + Math.cos( deg2rad(lat_user) ) * Math.cos( deg2rad(lat_location) )
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of seperation
        dist = dist * 1852; // 1852 meters per nautical mile
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
