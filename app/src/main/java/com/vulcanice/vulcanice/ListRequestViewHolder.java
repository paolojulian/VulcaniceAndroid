package com.vulcanice.vulcanice;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vulcanice.vulcanice.Model.Request;

/**
 * Created by User on 21/06/2018.
 */

public class ListRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    View mView;
    protected Button btnAccept, btnDecline;
    public ListRequestViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void bindListRequest(Request request) {
        TextView clientName = mView.findViewById(R.id.client_name);
        TextView description = mView.findViewById(R.id.description);
        btnAccept = mView.findViewById(R.id.btn_accept_request);
        btnDecline = mView.findViewById(R.id.btn_decline_request);

        clientName.setText(request.getClientName());
        description.setText(request.getDescription());

        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Integer id = view.getId();
        Log.d("position_clicked", getAdapterPosition() + "");
        if (id == btnAccept.getId()) {
            Log.d("click", "accept");
        } else if (id == btnDecline.getId()) {
            Log.d("click", "decline");
        }
    }
}
