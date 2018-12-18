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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListShopAdapter extends RecyclerView.Adapter<ViewShopViewHolder>{

    Context c;
    Shop shopModel;
    VCN_User mUser;
    DatabaseReference shopReference;
    ViewShopViewHolder mHolder;
    Location mLocation;
    Double mDistance;
    private ArrayList<Shop> shops;

    public ListShopAdapter(Context c, ArrayList<Shop> shops, Location userLocation) {
        this.c = c;

        this.shops = shops;
        this.mLocation = userLocation;
    }

    @Override
    public ViewShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.cardview_list_shop, parent, false);
        return new ViewShopViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewShopViewHolder holder, int position) {
        mHolder = holder;
        if (shops.get(position) == null) {
            return;
        }
        Shop shop = shops.get(position);
        mHolder.shopId = shop.getOwner() + "_" + shop.getName();
        mHolder.shopName.setText(shop.getName());
        mHolder.shopDescription.setText(shop.getDescription());
        Double lat = Double.parseDouble(shop.getLatitude());
        Double lng = Double.parseDouble(shop.getLongitude());
        mHolder.shopLat.setText(lat + "");
        mHolder.shopLng.setText(lng + "");
        mHolder.shopDistance.setText(distanceToUser(lat, lng) + " m");

        if ( ! shop.getType().equals("Gasoline Station")) {
            mHolder.BtnRequestShop.setVisibility(View.VISIBLE);
        }
        mHolder.shopOwner.setText("");
    }

    public double distanceToUser(double lat1, double lng1) {

        Location shopLocation = new Location("");
        shopLocation.setLatitude(lat1);
        shopLocation.setLongitude(lng1);
        return mLocation.distanceTo(shopLocation);
    }
    @Override
    public int getItemCount() {
        return shops.size();
//        return 1;
    }
}
