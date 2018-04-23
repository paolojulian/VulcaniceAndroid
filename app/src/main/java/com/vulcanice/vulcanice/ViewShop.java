package com.vulcanice.vulcanice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by paolo on 4/23/18.
 */

public class ViewShop extends AppCompatActivity{
    TextView shopTitle;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_shop);

        shopTitle = (TextView) findViewById(R.id.listShop);
        shopTitle.setText("Test");
    }
}