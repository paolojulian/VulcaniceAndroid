package com.vulcanice.vulcanice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.vulcanice.vulcanice.Model.Request;

/**
 * Created by User on 21/06/2018.
 */

public class ListRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    View mView;
    public ListRequestViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void bindListRequest(Request request) {
        TextView clientName = mView.findViewById(R.id.client_name);
        TextView description = mView.findViewById(R.id.description);

        clientName.setText(request.getClientName());
        description.setText(request.getDescription());
    }

    @Override
    public void onClick(View view) {

    }
}
