package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vulcanice.vulcanice.Model.Request;
import com.vulcanice.vulcanice.Model.VCN_User;

/**
 * Created by User on 16/06/2018.
 */

public class RequestShopActivity extends AppCompatActivity{
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    protected Button btnRequest;
    protected TextView clientDescription;
    protected ProgressBar progressBar;
    protected Request request;
    protected VCN_User vcnUser;
    protected String shopId;

    private OnSuccessListener onRequestSuccess = new OnSuccessListener() {
        @Override
        public void onSuccess(Object o) {
            Intent gotoTrackRequest = new Intent(
                   RequestShopActivity.this,
                    TrackRequestActivity.class
            );
            gotoTrackRequest.putExtra("shopId", shopId);
//            startActivity(gotoTrackRequest);
        }
    };

    private OnFailureListener onRequestFailed = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(
                    RequestShopActivity.this,
                    "Failed to request tracking\nPlease try again later",
                    Toast.LENGTH_SHORT
            ).show();
        }
    };

    private OnCompleteListener onRequestCompleted = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            progressBar.setVisibility(View.GONE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_shop);

        setupView();
        setupFirebase();
        getUserInfo();
        eventRequestShop();
    }

    private void setupView() {
        btnRequest = findViewById(R.id.btn_request);
        clientDescription = findViewById(R.id.user_description);
        progressBar = findViewById(R.id.progress_bar);

        Intent i = getIntent();
        shopId = i.getExtras().getString("shopId");
        shopId = shopId.split("_")[0];
    }

    private void eventRequestShop() {
        View.OnClickListener onClickRequest = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vcnUser == null) {
                    return;
                }
                requestShop();
            }
        };

        btnRequest.setOnClickListener(onClickRequest);
    }
    private void requestShop() {

        String nClientDescription = clientDescription.getText().toString().trim();
        if (nClientDescription.equals("")) {
            alertNoDescription();
            return;
        }
        request.setDescription(nClientDescription);
        request.setValid(true);
        request.setClientUid(currentUser.getUid());

        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference requestReference = mDatabase.getReference()
                .child("Request").child(shopId).child(currentUser.getUid());

        requestReference.setValue(request)
                .addOnSuccessListener(onRequestSuccess)
                .addOnFailureListener(onRequestFailed)
                .addOnCompleteListener(onRequestCompleted);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
    }

    private void getUserInfo() {
        request = new Request();
        DatabaseReference shopReference = mDatabase.getReference()
                .child("Users").child(currentUser.getUid());

        shopReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                vcnUser = dataSnapshot.getValue(VCN_User.class);
                if (vcnUser == null) {
                    return;
                }
                request.setClientName(vcnUser.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String dbError = RequestShopActivity.this.getString(R.string.db_error);
                Toast.makeText(RequestShopActivity.this, dbError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void alertNoDescription() {
        Toast.makeText(
                RequestShopActivity.this,
                "Please enter a description",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        startActivity(
                new Intent(RequestShopActivity.this, MainPage.class)
        );
        super.onStop();
    }
}
