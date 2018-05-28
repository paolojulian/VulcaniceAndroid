package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vulcanice.vulcanice.Model.Shop;
import com.vulcanice.vulcanice.Model.User;

/**
 * Created by paolo on 5/3/18.
 */

public class DashBoardTab1 extends Fragment {
    private Button btnFindGas, btnFindVul, btnCreateShop;

    private RecyclerView mListShop;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private View view;
    private DatabaseReference gas_stations, vulcanizeRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dash_board_1, container, false);
//
        listShop();
        eventCreateShop();

        return view;
    }
    private void listShop() {
        vulcanizeRef = FirebaseDatabase.getInstance().getReference("Shops").child("Vulcanizing Stations");
        mListShop = view.findViewById(R.id.list_vul_shop);
        mListShop.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mListShop.setLayoutManager(mLayoutManager);

        mAdapter = new ListShopViewHolder();
        mListShop.setAdapter(mAdapter);
    }

    private void eventCreateShop() {
        btnCreateShop = view.findViewById(R.id.btn_create_shop);
        btnCreateShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreateShopActivity.class));
            }
        });
    }

    private void eventFind() {
        btnFindGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Finding nearest Gas Station", Toast.LENGTH_SHORT).show();
            }
        });
        btnFindVul.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Finding nearest Vulcanizing Shop", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return super.onOptionsItemSelected(item);
    }
}
