package com.vulcanice.vulcanice;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vulcanice.vulcanice.Model.Request;

import java.util.ArrayList;

/**
 * Created by User on 21/06/2018.
 */

public class ListRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    View mView;
    protected Button btnAccept, btnDecline;
    private ArrayList<String> requestIds;

    public ListRequestViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        requestIds = new ArrayList<>();
    }

    public void bindListRequest(Request request, Integer position) {
        TextView clientName = mView.findViewById(R.id.client_name);
        TextView description = mView.findViewById(R.id.description);
        btnAccept = mView.findViewById(R.id.btn_accept_request);
        btnDecline = mView.findViewById(R.id.btn_decline_request);

        clientName.setText(request.getClientName());
        description.setText(request.getDescription());
        requestIds.add(request.getClientUid());

        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Context context = view.getContext();
        Integer id = view.getId();
        Integer pos = getAdapterPosition();
        String clientUid = requestIds.get(pos);
        if (id == btnAccept.getId()) {
            Intent i = new Intent(view.getContext(), TrackRequestActivity.class);
            i.putExtra("clientUid", clientUid);
            context.startActivity(i);
        } else if (id == btnDecline.getId()) {
        }
    }
}
