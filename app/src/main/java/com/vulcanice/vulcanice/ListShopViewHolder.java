package com.vulcanice.vulcanice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by User on 03/04/2018.
 */

public class ListShopViewHolder extends RecyclerView.Adapter<ListShopViewHolder.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView shopTitle, shopDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            shopTitle = (TextView) itemView;
            shopDescription = (TextView) itemView;
        }
    }

}
