package com.vulcanice.vulcanice.Library;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.Request;

import java.util.ArrayList;

/**
 * Created by User on 20/12/2018.
 */

public class RequestHelper {
    private final String TAG = "TAG_RequestHelper";
    DatabaseReference db;
    ArrayList<Request> requests = new ArrayList<>();

    public RequestHelper(DatabaseReference db) {
        this.db = db;
    }

    /**
     * Retrieve all requests from the user
     * 0 = Pending
     * 1 = Accepted
     * 2 = Declined
     * 3 = Cancel
     * @param uid - user to get requests from
     * @return
     */
    public ArrayList<Request> retrieve(String uid) {
        Query requestList = this.db
                .child(uid)
                .orderByChild("isAccepted")
                .equalTo(0);

        requestList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                requests.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Request request = ds.getValue(Request.class);
                    requests.add(request);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//
//        requestList.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                Request request = dataSnapshot.getValue(Request.class);
//
//                Log.d(TAG, "onChildAdded: " + request);
//
//                requests.add(request);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                Request request = dataSnapshot.getValue(Request.class);
//
//                Log.d(TAG, "onChildChanged: " + request);
//
//                if ( ! request.getIsAccepted().equals(0)) {
//                    requests.remove(request);
//                    return;
//                }
//
//                if (requests.contains(request)) {
//                    return;
//                }
//
//                requests.add(request);
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                Request request = dataSnapshot.getValue(Request.class);
//
//                Log.d(TAG, "onChildRemoved: " + request);
//
//                requests.remove(request);
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                Request request = dataSnapshot.getValue(Request.class);
//
//                Log.d(TAG, "onChildMoved: " + request);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
        return requests;
    }
//
//    public void accept(DatabaseReference requestRef, String clientUid) {
//        requestRef.setValue(1)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(
//                                context,
//                                "Accepted Request",
//                                Toast.LENGTH_SHORT
//                        ).show();
//                        Intent i = new Intent(context, TrackRequestActivity.class);
//                        i.putExtra("id", clientUid);
//                        i.putExtra("type", "owner");
//                        context.startActivity(i);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(
//                                context,
//                                "Unable to accept request\nPlease try again later",
//                                Toast.LENGTH_SHORT
//                        ).show();
//                    }
//                });
//    }
//
//    public void decline() {
//
//    }
}
