package com.vulcanice.vulcanice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by paolo on 5/21/18.
 */

public class ListVulcanizingShops extends AppCompatActivity{

    private FirebaseDatabase database;
    private DatabaseReference locationRef;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_list_vul_shops);

        database = FirebaseDatabase.getInstance();
        locationRef = database.getReference("locations");
//        mListView = (ListView) findViewById(R.id.ListVulShops);
    }
}
