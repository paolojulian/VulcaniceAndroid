package com.vulcanice.vulcanice.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vulcanice.vulcanice.Model.Request;
import com.vulcanice.vulcanice.OwnerRequest.ClientConfirmation;
import com.vulcanice.vulcanice.R;


import java.util.ArrayList;

/**
 * Created by User on 20/12/2018.
 */

public class RequestListAdapter extends BaseAdapter {

    String _userUid;
    Context _context;
    ArrayList<Request> _requests;

    public RequestListAdapter(Context context, ArrayList<Request> requests, String userUid) {
        this._context = context;
        this._requests = requests;
        this._userUid = userUid;
    }

    @Override
    public int getCount() {
        return _requests.size();
    }

    @Override
    public Object getItem(int pos) {
        return _requests.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final Context context = this._context;
        if (convertView == null) {
            convertView = LayoutInflater
                    .from(_context)
                    .inflate(R.layout.adapter_card_request, viewGroup, false);
        }

        final Request request = (Request) this.getItem(position);

        TextView clientName = convertView.findViewById(R.id.client_name);
        TextView clientDescription = convertView.findViewById(R.id.description);
        TextView pickupType = convertView.findViewById(R.id.pickup_type);
        TextView vehicleType = convertView.findViewById(R.id.vehicle_type);
        TextView vehicleColor = convertView.findViewById(R.id.vehicle_color);
        TextView repairType = convertView.findViewById(R.id.repair_type);
        final FloatingActionButton acceptRequest = convertView.findViewById(R.id.btn_accept_request);
        final FloatingActionButton declineRequest = convertView.findViewById(R.id.btn_decline_request);

        clientName.setText(request.getClientName());
        clientDescription.setText(request.getDescription());
        pickupType.setText(request.getPickupType());
        vehicleType.setText(request.getVehicleType());
        vehicleColor.setText(request.getVehicleColor());
        repairType.setText(request.getRepairType());

        final String userUid = this._userUid;

        acceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept(request.getClientUid());
            }
        });
        declineRequest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Decline Request?")
                        .setMessage("Are you sure you want to decline the request?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                decline(request.getClientUid());
                            }
                        }).create().show();
            }
        });

        return convertView;
    }

    public void accept(final String clientUid) {
        final Context context = this._context;
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                .child("Request").child(this._userUid).child(clientUid).child("isAccepted");
        requestRef.setValue(1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                context,
                                "Accepted Request",
                                Toast.LENGTH_SHORT
                        ).show();
                        Intent i = new Intent(context, ClientConfirmation.class);
                        i.putExtra("id", clientUid);
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

    private void decline(final String clientUid) {
        final Context context = this._context;
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                .child("Request").child(this._userUid).child(clientUid).child("isAccepted");

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
}
