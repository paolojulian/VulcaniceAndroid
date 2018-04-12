package com.vulcanice.vulcanice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by User on 03/04/2018.
 */

public class ListShopViewHolder extends RecyclerView.ViewHolder {

    public TextView textEmail;
    public ListShopViewHolder(View itemView) {
        super(itemView);
        textEmail = (TextView)itemView.findViewById(R.id.textEmail);
    }
}
