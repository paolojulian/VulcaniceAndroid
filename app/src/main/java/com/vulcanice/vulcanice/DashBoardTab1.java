package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
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

    protected FirebaseRecyclerAdapter<Shop, ListShopViewHolder> firebaseAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dash_board_1, container, false);
//
        listShop();

        return view;
    }
    private void listShop() {
        vulcanizeRef = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child("Vulcanizing Station")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mListShop = view.findViewById(R.id.list_vul_shop);

        firebaseAdapter = new FirebaseRecyclerAdapter<Shop, ListShopViewHolder>
                (Shop.class, R.layout.listview_shop, ListShopViewHolder.class, vulcanizeRef) {
            @Override
            protected void populateViewHolder(ListShopViewHolder viewHolder, Shop model, int position) {
                viewHolder.bindListShop(model);
            }
        };
        mListShop.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mListShop.setLayoutManager(mLayoutManager);
        mListShop.setAdapter(firebaseAdapter);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        firebaseAdapter.cleanup();
    }
}
