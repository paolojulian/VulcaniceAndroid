package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.vulcanice.vulcanice.Model.Request;
import com.vulcanice.vulcanice.ViewHolders.ListRequestViewHolder;

/**
 * Created by User on 21/06/2018.
 */

public class ViewRequestsActivity extends AppCompatActivity{
    private final String TAG = "TAG_" + ViewRequestsActivity.class.getName();
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseUser currentUser;
    protected FirebaseRecyclerAdapter<Request, ListRequestViewHolder> firebaseAdapter;

    private RecyclerView.LayoutManager mLayoutManager;
    private Query requestList;
    RecyclerView mListRequest;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);
        Log.d(TAG, "Init");

        setupDatabase();
        listRequests();
    }

    private void setupDatabase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            gotoSignIn();
        }
        mDatabase = FirebaseDatabase.getInstance();
    }

    private void listRequests() {
        requestList = mDatabase.getReference("Request")
                .child(currentUser.getUid())
                .orderByChild("isAccepted")
                .equalTo(0);
        displayList();
    }

    private boolean isFirst = true;
    private void displayList() {
        firebaseAdapter = new FirebaseRecyclerAdapter<Request, ListRequestViewHolder>
                (Request.class, R.layout.layout_request, ListRequestViewHolder.class, requestList) {
            @Override
            protected void populateViewHolder(ListRequestViewHolder viewHolder, Request model, int position) {
                viewHolder.bindListRequest(model, position);
                Log.d(TAG, "List: " + model);
                if (isFirst) {
                    viewHolder.setUserUid(currentUser.getUid());
                    isFirst = false;
                }
            }
        };
        mListRequest = findViewById(R.id.list_request);
        mListRequest.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mListRequest.setLayoutManager(mLayoutManager);
        mListRequest.setAdapter(firebaseAdapter);
    }

    protected void gotoSignIn() {
        startActivity(new Intent(ViewRequestsActivity.this, MainActivity.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAdapter.cleanup();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ViewRequestsActivity.this, MainPage.class);
        startActivity(i);
    }
}
