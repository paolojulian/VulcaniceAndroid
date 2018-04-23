package com.vulcanice.vulcanice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by User on 03/04/2018.
 */

public class ListShopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView textEmail;
    ItemClickListener itemClickListener;
    public ListShopViewHolder(View itemView) {
        super(itemView);
        textEmail = (TextView)itemView.findViewById(R.id.textEmail);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition());
    }
}
