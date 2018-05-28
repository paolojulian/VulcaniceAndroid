package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.Dash;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by paolo on 5/27/18.
 */

public class MainPage extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggleDrawer;
    private NavigationView mNavigationView;
    private Button btnAddShop;

    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mAuth = FirebaseAuth.getInstance();
        setupDrawer();
        setupMenu();
    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if ( currentUser == null )
        {
            gotoSignIn();
        }
        super.onStart();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    //        Toast.makeText(MainPage.this, "Test", Toast.LENGTH_SHORT).show();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggleDrawer.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
    }

    private void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggleDrawer = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.vcn_open, R.string.vcn_close );
        mDrawerLayout.addDrawerListener(mToggleDrawer);
        mToggleDrawer.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupMenu() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.manage_account:
                        startActivity(new Intent(MainPage.this, SignUpActivity.class));
                        return true;

                    case R.id.manage_shop:
                        startActivity(new Intent(MainPage.this, DashBoard.class));
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
        startActivity(new Intent(MainPage.this, MainActivity.class));
    }
}

