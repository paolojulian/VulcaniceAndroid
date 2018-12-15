package com.vulcanice.vulcanice;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vulcanice.vulcanice.Model.Request;
import com.vulcanice.vulcanice.Model.Session;
import com.vulcanice.vulcanice.Model.VCN_User;

import java.security.acl.Owner;

/**
 * Created by User on 13/12/2018.
 */

public class OwnerMainPage extends AppCompatActivity {
    private final String TAG = "TAG_OwnerMainPage";
    private Session session;
    private Context context = OwnerMainPage.this;
    // Layouts
    private LinearLayout mLayout;
    private ProgressBar pageLoader;
    private RecyclerView mListRequest;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView noRequest;
    private Toolbar mToolbar;
    private NavigationView mSideBar;
    // Drawer
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggleDrawer;
    // Sidebar
    private TextView navName, navEmail, navMobile;
    private ImageView navImg;
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    // Others
    private String mUserType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.owner_main_page);

        Log.d(TAG, "Init");

        initialData();
        sideBarCallBacks();
        displayRequestList();
    }

    private void initialData() {
        session = new Session(context);
        // Layouts
        mLayout = findViewById(R.id.layout_main_page);
        mLayout.setVisibility(View.GONE);
        pageLoader = findViewById(R.id.page_loader);
        mListRequest = findViewById(R.id.list_request);
        noRequest = findViewById(R.id.no_request);
        mToolbar = findViewById(R.id.toolbar);
        mDrawer = findViewById(R.id.drawer_layout);

        // Sidebar
        mSideBar = findViewById(R.id.sidebar);
        View sideBar = mSideBar.getHeaderView(0);
        navEmail = sideBar.findViewById(R.id.navigation_email);
        navMobile = sideBar.findViewById(R.id.navigation_mobile);
        navName = sideBar.findViewById(R.id.navigation_name);
        navImg = sideBar.findViewById(R.id.navigation_img_user);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            gotoSignIn();
        }
        Log.d(TAG, "User: " + mUser.getUid());

        // Extras
        Bundle extras = getIntent().getExtras();
        mUserType = session.getUser_type();
        Log.d(TAG, "Usertype: " + mUserType);

        // Toolbar
        setSupportActionBar(mToolbar);
        // Drawer
        mDrawer.closeDrawers();
        mToggleDrawer = new ActionBarDrawerToggle(this, mDrawer, R.string.vcn_open, R.string.vcn_close);
        mDrawer.addDrawerListener(mToggleDrawer);
        mToggleDrawer.syncState();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupDrawer() {
        if (session.exists()) {
            navEmail.setText(session.getEmail());
            navMobile.setText(session.getMobile());
            navName.setText(session.getName());
        } else {
            DatabaseReference ref = mDatabase.getInstance().getReference("Users")
                    .child(session.getUid());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    VCN_User user = dataSnapshot.getValue(VCN_User.class);
                    if (user == null) {
                        return;
                    }
                    navEmail.setText(user.getEmail());
                    navMobile.setText(user.getMobile());
                    navName.setText(user.getName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, databaseError + "");
                }
            });
        }

        // SETUP IMAGE
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("images")
                .child(session.getUid());

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                GlideApp
                        .with(context)
                        .load(uri.toString())
                        .placeholder(R.drawable.default_prof_pic)
                        .into(navImg);
            }
        });
    }

    private void sideBarCallBacks() {
        mSideBar.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.manage_account:
                        startActivity(new Intent(OwnerMainPage.this, EditAccountActivity.class));
                        return true;

                    case R.id.manage_shop:
                        if ( ! isConnected()) {
                            return true;
                        }
                        if ( ! mUserType.equals("Shop Owner")) {
                            toast("Must be a Shop Owner to manage shop");
                            return true;
                        }
                        startActivity(new Intent(OwnerMainPage.this, DashBoard.class));
                        return true;

                    case R.id.logout:
                        gotoSignIn();
                        return true;

                    default:
                        return false;
                }
            }
        });
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

    protected boolean isConnected() {
        if (mUser == null) {
            toast("Please connect to the internet");
            return false;
        }
        return true;
    }

    protected void gotoSignIn() {
        if (mAuth != null) {
            mAuth.signOut();
        }
        startActivity(new Intent(OwnerMainPage.this, MainActivity.class));
        finish();
    }

    public void toast(String message) {
        Toast.makeText(
                OwnerMainPage.this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggleDrawer.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawers();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("Are you sure you want to log out?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            gotoSignIn();
                        }
                    }).create().show();
        }
    }
}
