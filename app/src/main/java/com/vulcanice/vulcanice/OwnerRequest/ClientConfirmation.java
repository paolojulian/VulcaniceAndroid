package com.vulcanice.vulcanice.OwnerRequest;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Library.RequestHelper;
import com.vulcanice.vulcanice.MainActivity;
import com.vulcanice.vulcanice.OwnerMainPage;
import com.vulcanice.vulcanice.R;
import com.vulcanice.vulcanice.TrackRequestActivity;

import java.security.acl.Owner;

public class ClientConfirmation extends AppCompatActivity {

    protected Context context;
    protected ProgressBar progressBar;
    protected String clientUid;
    // Database
    protected FirebaseAuth mAuth;
    protected DatabaseReference requestList;
    protected FirebaseDatabase mDatabase;
    protected FirebaseUser mUser;
    // Helpers
    public RequestHelper fetchList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_client_to_respond_to_request);

        initialSetup();
    }

    private void initialSetup() {
        context = ClientConfirmation.this;
        setupViews();
        getExtras();
        setupEventListeners();
    }

    private void setupViews() {
        progressBar = findViewById(R.id.progress_bar);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            gotoSignIn();
        }
    }

    private void getExtras() {
        Intent i = getIntent();
        clientUid = i.getExtras().getString("id");
    }

    private void setupEventListeners() {
        fetchList = new RequestHelper(requestList);

        requestList = mDatabase.getReference("Request")
                .child(mUser.getUid())
                .child(clientUid)
                .child("isAccepted");

        requestList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( ! dataSnapshot.exists()) {
                    return;
                }
                String isAccepted = dataSnapshot.getValue().toString();
                // Request is confirmed
                switch (isAccepted) {
                    case "4":
                        Intent i = new Intent(context, TrackRequestActivity.class);
                        i.putExtra("id", clientUid);
                        i.putExtra("type", "owner");
                        context.startActivity(i);
                        finish();
                        requestList.removeEventListener(this);
                        break;
                    case "3":
                        String clientDeclined = "The client have cancelled the request.";
                        Toast.makeText(ClientConfirmation.this, clientDeclined, Toast.LENGTH_SHORT).show();
                        Intent ownerMainPageIntent = new Intent(context, OwnerMainPage.class);
                        startActivity(ownerMainPageIntent);
                        finish();
                        requestList.removeEventListener(this);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String dbError = context.getString(R.string.db_error);
                Toast.makeText(context, dbError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void gotoSignIn() {
        if (mAuth != null) {
            mAuth.signOut();
        }
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }
}
