package com.vulcanice.vulcanice;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.Shop;

import java.util.ArrayList;

/**
 * Created by paolo on 5/29/18.
 */

public class ListShopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    View mView;
    public ListShopViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void bindListShop(Shop shop) {
        TextView shopTitle = mView.findViewById(R.id.shop_title);
        TextView shopDescription = mView.findViewById(R.id.shop_description);

        shopTitle.setText(shop.getName());
        shopDescription.setText(shop.getDescription());
    }
    @Override
    public void onClick(View view) {
//        final ArrayList<Shop> shops = new ArrayList<>();
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    shops.add(snapshot.getValue(Shop.class));
//                }
//
//                int itemPosition = getLayoutPosition();
//
//                Intent intent = new Intent(mContext, EditShopActivity.class);
//                intent.putExtra("name", itemPosition + "");
//                intent.putExtra("description", itemPosition + "");
//                intent.putExtra("latitude", itemPosition + "");
//                intent.putExtra("longitude", itemPosition + "");
//                intent.putExtra("name", itemPosition + "");
//
//                mContext.startActivity(intent);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
    }
}

