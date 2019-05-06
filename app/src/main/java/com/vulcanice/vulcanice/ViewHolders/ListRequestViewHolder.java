package com.vulcanice.vulcanice.ViewHolders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vulcanice.vulcanice.Model.Request;
import com.vulcanice.vulcanice.R;
import com.vulcanice.vulcanice.TrackRequestActivity;

import java.util.ArrayList;

/**
 * Created by User on 21/06/2018.
 */

public class ListRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    View mView;
    protected FloatingActionButton btnAccept, btnDecline;
    private ArrayList<String> requestIds;
    private Context context;
    private Integer id;
    private Integer pos;
    private String clientUid;
    private String userUid;

    public ListRequestViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        requestIds = new ArrayList<>();
    }

    public void bindListRequest(Request request, Integer position) {
        TextView clientName = mView.findViewById(R.id.client_name);
        TextView description = mView.findViewById(R.id.description);
        TextView pickupType = mView.findViewById(R.id.pickup_type);
        TextView vehicleType = mView.findViewById(R.id.vehicle_type);
        TextView vehicleColor = mView.findViewById(R.id.vehicle_color);
        btnAccept = mView.findViewById(R.id.btn_accept_request);
        btnDecline = mView.findViewById(R.id.btn_decline_request);

        clientName.setText(request.getClientName());
        pickupType.setText(request.getPickupType());
        vehicleType.setText(request.getVehicleType());
        vehicleColor.setText(request.getVehicleColor());
        description.setText(request.getDescription());
        requestIds.add(request.getClientUid());

        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    @Override
    public void onClick(View view) {
        context = view.getContext();
        id = view.getId();
        pos = getAdapterPosition();
        clientUid = requestIds.get(pos);

        switch (id) {
            case R.id.btn_accept_request:
                acceptRequest();
                break;
            case R.id.btn_decline_request:
                declineRequest();
                break;
            default:
                break;
        }
    }

    private void acceptRequest() {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                .child("Request").child(userUid).child(clientUid).child("isAccepted");

        requestRef.setValue(1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                context,
                                "Accepted Request",
                                Toast.LENGTH_SHORT
                        ).show();
                        Intent i = new Intent(context, TrackRequestActivity.class);
                        i.putExtra("id", clientUid);
                        i.putExtra("type", "owner");
                        context.startActivity(i);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                context,
                                "Unable to accept request\nPlease try again later",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void declineRequest() {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                .child("Request").child(userUid).child(clientUid).child("isAccepted");

        requestRef.setValue(2)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                context,
                                "Declined Successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                context,
                                "Unable to decline request\nPlease try again later",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void removeFromView() {

    }
}
