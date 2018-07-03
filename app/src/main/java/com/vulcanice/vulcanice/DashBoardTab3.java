package com.vulcanice.vulcanice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.vulcanice.vulcanice.Model.Shop;

/**
 * Created by paolo on 5/3/18.
 */

public class DashBoardTab3 extends Fragment {
    private RecyclerView mListShop;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private View view;
    private DatabaseReference vulcanizeRef;
    private Query vulcanizeQuery;

    protected FirebaseRecyclerAdapter<Shop, ListShopViewHolder> firebaseAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dash_board_3, container, false);
        listShop();
        return view;
    }

    private void listShop() {
        vulcanizeRef = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child("both");
        vulcanizeQuery = vulcanizeRef
                .orderByChild("owner")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mListShop = view.findViewById(R.id.list_both);

        firebaseAdapter = new FirebaseRecyclerAdapter<Shop, ListShopViewHolder>
                (Shop.class, R.layout.layout_list_both, ListShopViewHolder.class, vulcanizeQuery ) {
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

    @Override
    public void onResume() {
        super.onResume();
//        ((DashBoard) getActivity())
//                .setActionBarTitle("Vulcanizing/Gas Stations");
        getActivity().setTitle("Vulcanizing/Gas Stations");
    }
}

