package com.vulcanice.vulcanice;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.Dash;
import com.google.firebase.auth.FirebaseAuth;

public class DashBoard extends AppCompatActivity {

    private ViewPager mViewPager;
    private SectionsPageAdapter mSectionsPageAdapter;
    private FloatingActionButton fabCreateShop;
    //FIREBASE
    private FirebaseAuth mAuth;
    //NAVIGATION
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggleDrawer;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        mAuth = FirebaseAuth.getInstance();

        //INPUTS
        setupTabs();
        setupInputs();
        //DRAWER
        setupDrawer();
        setupMenu();
        //EVENTS
        eventCreateShop();
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
        mDrawerLayout = findViewById(R.id.dash_board_drawer);
        mToggleDrawer = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.vcn_open, R.string.vcn_close );
        mDrawerLayout.addDrawerListener(mToggleDrawer);
        mToggleDrawer.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                        startActivity(new Intent(DashBoard.this, MainPage.class));
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
    }
}
