package com.vulcanice.vulcanice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vulcanice.vulcanice.Model.Session;
import com.vulcanice.vulcanice.Model.VCN_User;

public class DashBoard extends AppCompatActivity {
    private final String TAG = "TAG_DashBoard";
    private Session session;
    private Context context = DashBoard.this;

    private ViewPager mViewPager;
    private SectionsPageAdapter mSectionsPageAdapter;
    private FloatingActionButton fabCreateShop;
    //MODELS
    private VCN_User userModel;
    //FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseUser currentUser;
    private DatabaseReference mRef;
    //NAVIGATION
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggleDrawer;
    private NavigationView mNavigationView;
    private ImageView navImg;

    private TextView notifCount, navName, navEmail, navMobile;

    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        Log.d(TAG, "Init");

        session = new Session(context);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //INPUTS
        setupToolBar();
        setupTabs();
        setupInputs();
        //DRAWER
        setupDrawer();
        setupMenu();
        setupText();
        setupUserImage();
        //EVENTS
        eventCreateShop();
    }

    private void setupText() {
        View headerLayout = mNavigationView.getHeaderView(0);
        navEmail = headerLayout.findViewById(R.id.navigation_email);
        navMobile = headerLayout.findViewById(R.id.navigation_mobile);
        navName = headerLayout.findViewById(R.id.navigation_name);
        navImg = headerLayout.findViewById(R.id.navigation_img_user);
        if (session.exists()) {
            navEmail.setText(session.getEmail());
            navMobile.setText(session.getMobile());
            navName.setText(session.getName());
        } else {
            DatabaseReference ref = mDatabase.getInstance().getReference("Users")
                    .child(currentUser.getUid());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(VCN_User.class);
                    if (userModel == null) {
                        return;
                    }
                    navEmail.setText(userModel.getEmail());
                    navMobile.setText(userModel.getMobile());
                    navName.setText(userModel.getName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void setupToolBar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void eventCreateShop() {
        fabCreateShop = findViewById(R.id.fab_create_shop);
        fabCreateShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(
                    new Intent(DashBoard.this, CreateShopActivity.class)
                );
            }
        });
    }

    private void setupInputs() {
    }

    private void setupTabs() {
        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        setupViewPage(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPage(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new DashBoardTab1(), "Vulcanize");
        adapter.addFragment(new DashBoardTab2(), "Gas");
        adapter.addFragment(new DashBoardTab3(), "Both");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggleDrawer.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawer() {
        mDrawer = findViewById(R.id.dash_board_drawer);
        mToggleDrawer = new ActionBarDrawerToggle(this, mDrawer, R.string.vcn_open, R.string.vcn_close );
        mDrawer.addDrawerListener(mToggleDrawer);
        mToggleDrawer.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupUserImage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images").child(currentUser.getUid());

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

    private void setupMenu() {
        mNavigationView = findViewById(R.id.dash_board_navigation);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.manage_account:
                        startActivity(new Intent(DashBoard.this, EditAccountActivity.class));
                        return true;
                    case R.id.main_page:
                        startActivity(new Intent(DashBoard.this, OwnerMainPage.class));
                        finish();
                        return true;
                    case R.id.logout:
                        mAuth.signOut();
                        gotoSignIn();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }
    protected void gotoSignIn() {
        startActivity(new Intent(DashBoard.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if(mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawers();
        } else {
            startActivity(new Intent(DashBoard.this, OwnerMainPage.class));
            finish();
        }
    }
}
