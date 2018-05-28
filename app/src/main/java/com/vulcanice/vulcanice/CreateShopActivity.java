package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.vulcanice.vulcanice.Model.Shop;

/**
 * Created by paolo on 5/27/18.
 */

public class CreateShopActivity extends AppCompatActivity{
    private DatabaseReference mDatabase;
    private EditText shopName, shopDescription;
    private AppCompatButton btnSubmitShop;
    private ProgressBar progressBar;
    private Spinner shopType;
    private Shop mShop;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shop);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null)
        {
            startActivity(new Intent(CreateShopActivity.this, MainActivity.class));
        }
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        eventCreateShop();
    }

    private void eventCreateShop() {
        btnSubmitShop = findViewById(R.id.btn_shop_create);
        btnSubmitShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                setInputValues();
                saveShop();
            }
        });
    }

    private void setInputValues() {
        shopName = (EditText) findViewById(R.id.input_shop_name);
        shopDescription = (EditText) findViewById(R.id.input_shop_description);
        shopType = (Spinner) findViewById(R.id.spinner_shop_type);

        mShop = new Shop(
            shopName.getText().toString(),
            shopDescription.getText().toString(),
            shopType.getSelectedItem().toString()
        );
        mShop.setLocation(
            "16.408850807625164",
            "120.59794975356021"
        );
    }

    private void saveShop() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Shops")
                .child(mShop.getType())
                .child(user.getUid())
                .child(mShop.getName())
                .setValue(mShop)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(
                        CreateShopActivity.this,
                        "Shop was successfully added!",
                        Toast.LENGTH_SHORT
                    ).show();
                    startActivity(
                        new Intent(CreateShopActivity.this, DashBoard.class)
                    );
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(
                            CreateShopActivity.this,
                            "@string/db_error",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            })
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);
                }
            });
    }
}
