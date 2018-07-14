package com.vulcanice.vulcanice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


public class ViewShopViewHolder extends RecyclerView.ViewHolder {

    TextView shopName;
    TextView shopDescription;
    TextView shopDistance;
    TextView shopOwner;

    public ViewShopViewHolder(View itemView) {
        super(itemView);

        shopName = itemView.findViewById(R.id.shop_name);
        shopDescription = itemView.findViewById(R.id.shop_description);
        shopDistance = itemView.findViewById(R.id.shop_distance);
        shopOwner = itemView.findViewById(R.id.shop_owner);

    }

}