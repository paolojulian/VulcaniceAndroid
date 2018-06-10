package com.vulcanice.vulcanice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vulcanice.vulcanice.Model.Shop;

/**
 * Created by paolo on 5/29/18.
 */

public class ListGasViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    View mView;
    public ListGasViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void bindListGas(Shop shop) {
        TextView shopTitle = mView.findViewById(R.id.gas_station_name);
        TextView shopDescription = mView.findViewById(R.id.gas_station_address);

        shopTitle.setText(shop.getName());
        shopDescription.setText(shop.getDescription());
    }

    @Override
    public void onClick(View view) {
    }
}
