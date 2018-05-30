package com.vulcanice.vulcanice;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DashBoard extends AppCompatActivity {
    private ViewPager mViewPager;
    private SectionsPageAdapter mSectionsPageAdapter;
    private FloatingActionButton fabCreateShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        setupTabs();
        setupInputs();
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
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPage(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPage(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new DashBoardTab1(), "Vulcanize");
        adapter.addFragment(new DashBoardTab2(), "Gas");
        viewPager.setAdapter(adapter);
    }


}
