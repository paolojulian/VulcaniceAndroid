package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.vulcanice.vulcanice.Model.VCN_User;

public class EditAccountActivity extends AppCompatActivity{
    //FIREBASE
    private FirebaseDatabase mDatabase;
    private FirebaseUser user;
    private DatabaseReference mRef;
    //MODELS
    private VCN_User userModel;
    //FIELDS
    private EditText userName, mobile;
    private Spinner userType;
    private ProgressBar progressBar;
    private Button btnUpdateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        userType = findViewById(R.id.user_type);
        userName = findViewById(R.id.user_name);
        mobile = findViewById(R.id.mobile_number);
        btnUpdateAccount = findViewById(R.id.btn_update_account);

        mRef = mDatabase.getInstance().getReference();

        checkIfLoggedIn();
        getUserInfo();
    }

    protected void getUserInfo() {
        progressBar = findViewById(R.id.progress_bar);

        DatabaseReference ref = mDatabase.getInstance().getReference("Users")
                .child(user.getUid());
        mRef.child("Users")
            .child(user.getUid())
            .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(VCN_User.class);
                if(userModel == null) {
                    return;
                }
                populateUserType();
                populateFields();
                progressBar.setVisibility(View.GONE);
                eventOnUpdateAccount();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditAccountActivity.this, "@string/db_error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void eventOnUpdateAccount() {
        btnUpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                submitUpdateAccount();
            }
        });
    }

    protected void submitUpdateAccount() {
        VCN_User vcn_user = new VCN_User(
            userName.getText().toString().trim(),
            mobile.getText().toString().trim(),
            user.getEmail(),
            userType.getSelectedItem().toString().trim()
        );

        mRef.child("Users")
            .child(user.getUid())
                .setValue(vcn_user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                EditAccountActivity.this,
                                "Account Successfully Updated",
                                Toast.LENGTH_SHORT
                        ).show();

                        startActivity(new Intent(EditAccountActivity.this, MainPage.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                EditAccountActivity.this,
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

    protected void populateUserType() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.vcn_user_types, android.R.layout.simple_list_item_activated_1);
        userType.setAdapter(adapter);
        Integer userTypeIndex;
        if(userModel.getUser_type().equals("Client")) {
            userTypeIndex = 0;
        } else {
            userTypeIndex = 1;
        }
        userType.setSelection(userTypeIndex);
    }

    protected void populateFields() {
        userName.setText(userModel.getName());
        mobile.setText(userModel.getMobile());
    }

    protected void checkIfLoggedIn() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            gotoSignIn();
        }
    }

    @Override
    protected void onStart() {
        mDatabase = FirebaseDatabase.getInstance();
        super.onStart();
    }

    protected void gotoSignIn() {
        startActivity(new Intent(EditAccountActivity.this, MainActivity.class));
        finish();
    }

}
