package com.vulcanice.vulcanice;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.vulcanice.vulcanice.Model.Request;

/**
 * Created by User on 13/12/2018.
 */

public class OwnerMainPage extends AppCompatActivity {
    // Layouts
    private LinearLayout mLayout;
    private ProgressBar pageLoader;
    private RecyclerView mListRequest;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView noRequest;
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.owner_main_page);

        initialData();
        displayRequestList();
    }

    private void initialData() {
        // Layouts
        mLayout = findViewById(R.id.layout_main_page);
        mLayout.setVisibility(View.GONE);
        pageLoader = findViewById(R.id.page_loader);
        mListRequest = findViewById(R.id.list_request);
        noRequest = findViewById(R.id.no_request);
        // Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            gotoSignIn();
        }
    }

    private boolean isFirst = true;
    private void displayRequestList() {
        Query requestList = mDatabase.getReference("Request")
                .child(mUser.getUid())
                .orderByChild("isAccepted")
                .equalTo(0);

        FirebaseRecyclerAdapter<Request, ListRequestViewHolder> firebaseAdapter;
        firebaseAdapter = new FirebaseRecyclerAdapter<Request, ListRequestViewHolder>
                (Request.class, R.layout.layout_request, ListRequestViewHolder.class, requestList) {
            @Override
            protected void populateViewHolder(ListRequestViewHolder viewHolder, Request model, int position) {
                viewHolder.bindListRequest(model, position);
                if (isFirst) {
                    viewHolder.setUserUid(mUser.getUid());
                    noRequest.setVisibility(View.GONE);
                    mLayout.setVisibility(View.VISIBLE);
                    isFirst = false;
                }
            }
        };
        mListRequest.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mListRequest.setLayoutManager(mLayoutManager);
        mListRequest.setAdapter(firebaseAdapter);
    }

    protected void gotoSignIn() {
        startActivity(new Intent(OwnerMainPage.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        System.exit(1);
                        finish();
                    }
                }).create().show();
    }
}
