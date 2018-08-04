package com.vulcanice.vulcanice;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.Shop;
import com.vulcanice.vulcanice.Model.ShopList;
import com.vulcanice.vulcanice.Model.VCN_User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListShopAdapter extends RecyclerView.Adapter<ViewShopViewHolder>{

    Context c;
    Shop shopModel;
    VCN_User mUser;
    FirebaseDatabase mDatabase;
    ViewShopViewHolder mHolder;
    Location mLocation;
    Double mDistance;
    private String shopType;
    private Map<String, GeoLocation> shopArray;
    private List<String> shopKeys;
    private ShopList[] shopListArray;

    public ListShopAdapter(Context c, List<String> shopKeys, Location myLocation, String shopType) {
        this.c = c;
//        this.shopListArray = shopListArray;
//        this.shopArray = shopArray;
        this.shopKeys = shopKeys;
        this.shopType = shopType;
        mDatabase = FirebaseDatabase.getInstance();
        mLocation = myLocation;
    }

    @Override
    public ViewShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.cardview_list_shop, parent, false);
        return new ViewShopViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewShopViewHolder holder, int position) {
        mHolder = holder;
        if (shopKeys.get(position) == null) {
            return;
        }
        String key = shopKeys.get(position);
//        GeoLocation shopGeoLocation  = shopListArray[position].getShop_location();
//        mHolder.shopDistance.setText(distanceToUser(shopGeoLocation.latitude, shopGeoLocation.longitude) + " m");

        DatabaseReference shopReference = mDatabase.getReference()
                .child("Shops").child(shopType).child(key);

        shopReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shopModel = dataSnapshot.getValue(Shop.class);
                Log.d("test", shopModel + "");
//                if (shopModel == null) {
//                    return;
//                }
                mHolder.shopName.setText(shopModel.getName());
                mHolder.shopDescription.setText(shopModel.getDescription());
                Double lat = Double.parseDouble(shopModel.getLatitude());
                Double lng = Double.parseDouble(shopModel.getLongitude());
                mHolder.shopLat.setText(lat + "");
                mHolder.shopLng.setText(lng + "");
                mHolder.shopDistance.setText(distanceToUser(lat, lng) + " m");

                if ( ! shopModel.getType().equals("Gasoline Station")) {
                    mHolder.BtnRequestShop.setVisibility(View.VISIBLE);
                }

                DatabaseReference ownerReference = mDatabase.getReference()
                        .child("Users").child(shopModel.getOwner());
                ownerReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUser = dataSnapshot.getValue(VCN_User.class);
                        if (mUser == null) {
                            return;
                        }
                        mHolder.shopOwner.setText(mUser.getName());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public double distanceToUser(double lat1, double lng1) {

        Location shopLocation = new Location("");
        shopLocation.setLatitude(lat1);
        shopLocation.setLongitude(lng1);
        return mLocation.distanceTo(shopLocation);
    }
    @Override
    public int getItemCount() {
        return shopKeys.size();
//        return 1;
    }
}