package com.vulcanice.vulcanice;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vulcanice.vulcanice.ClientRequest.RequestShopActivity;


public class ViewShopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    TextView shopName;
    TextView shopDescription;
    TextView shopDistance;
    TextView shopOwner;
    TextView shopLat, shopLng;

    String shopId;
    Button BtnTrackShop, BtnRequestShop;
    private Integer pos;

    Context context;

    public ViewShopViewHolder(View itemView) {
        super(itemView);

        shopName = itemView.findViewById(R.id.shop_name);
        shopDescription = itemView.findViewById(R.id.shop_description);
        shopDistance = itemView.findViewById(R.id.shop_distance);
        shopOwner = itemView.findViewById(R.id.shop_owner);

        shopLat = itemView.findViewById(R.id.text_latitude);
        shopLng = itemView.findViewById(R.id.text_longitude);

        BtnTrackShop = itemView.findViewById(R.id.btn_track_shop);
        BtnRequestShop = itemView.findViewById(R.id.btn_request_shop);

        BtnTrackShop.setOnClickListener(this);
        BtnRequestShop.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        context = view.getContext();
        pos = getAdapterPosition();
        Intent intent;
        Integer id = view.getId();
        Log.d("shopLat", shopLat.getText().toString().trim());
        Log.d("shopLng", shopLng.getText().toString().trim());

        String mLat = shopLat.getText().toString().trim();
        String mLng = shopLng.getText().toString().trim();

        if (mLat.equals("") || mLng.equals("")) {
            return;
        }

        Double lat = Double.parseDouble(mLat);
        Double lng = Double.parseDouble(mLng);

        if (id == BtnTrackShop.getId()) {
            intent = new Intent(
                    context,
                    TrackShopActivity.class
            );
            intent.putExtra("shopLat", lat);
            intent.putExtra("shopLng", lng);
            context.startActivity(intent);
        } else if (id == BtnRequestShop.getId()) {
            Intent i = new Intent(
                    context,
                    RequestShopActivity.class
            );
            i.putExtra("shopId", shopId);
            i.putExtra("shopName", shopName.getText().toString());
            context.startActivity(i);
        } else {

        }
    }
}