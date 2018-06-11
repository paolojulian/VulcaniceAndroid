package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.vulcanice.vulcanice.Model.VCN_User;

import org.w3c.dom.Text;

/**
 * Created by paolo on 5/2/18.
 */

public class SignUpActivity extends AppCompatActivity {
    private EditText email, password, name;
    private Button btnLinkToSignIn, btnSignUp, btnResetPassword;
    private Spinner userType;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        name = (EditText) findViewById(R.id.signup_name);
        email = (EditText) findViewById(R.id.signup_email);
        password = (EditText) findViewById(R.id.signup_password);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        populateUserType();

        btnSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String inputName = name.getText().toString().trim();
                final String inputEmail = email.getText().toString().trim();
                final String inputPassword = password.getText().toString().trim();
                final String inputUserType = userType.getSelectedItem().toString();

                if (TextUtils.isEmpty(inputEmail)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(inputPassword)) {
                    Toast.makeText(getApplicationContext(), "Password Required!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(inputPassword)) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter a minimum of 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(inputUserType)) {
                    Toast.makeText(getApplicationContext(), "Please select a user type", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);
                auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if ( ! task.isSuccessful()) {
                                    Toast.makeText(
                                            SignUpActivity.this,
                                            "Authentication Failed!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }
                                AuthCredential credential = EmailAuthProvider.getCredential(inputEmail, inputPassword);
                                setAdditionalFields(
                                    new VCN_User(
                                        inputName,
                                        inputEmail,
                                        inputUserType
                                    ),
                                    credential
                                );
                                finish();
                            }
                        });
            }
        });
    }

    protected void setAdditionalFields(VCN_User user, final AuthCredential credential) {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if ( ! task.isSuccessful())
                {
                    Toast.makeText(
                            SignUpActivity.this,
                            "Authentication Failed!",
                            Toast.LENGTH_SHORT
                    ).show();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    user.delete();
                                }
                            });
                }
                Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    protected void populateUserType() {
        userType = (Spinner) findViewById(R.id.user_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.vcn_user_types, android.R.layout.simple_list_item_activated_1);
        userType.setAdapter(adapter);
        userType.setSelection(0);
    }
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
