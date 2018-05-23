package com.vulcanice.vulcanice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by paolo on 5/2/18.
 */

public class SignUpActivity extends AppCompatActivity {
    private EditText email, password;
    private Button btnLinkToSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        btnLinkToSignIn = (Button) findViewById(R.id.btn_sign_in);
        btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);
        email = (EditText) findViewById(R.id.signup_email);
        password = (EditText) findViewById(R.id.signup_password);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        btnResetPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLinkToSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String inputEmail = email.getText().toString().trim();
                String inputPassword = password.getText().toString().trim();

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

                progressBar.setVisibility(View.VISIBLE);
                auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(
                                        SignUpActivity.this,
                                        "createUserWithEmail:onComplete " + task.isSuccessful(),
                                        Toast.LENGTH_SHORT
                                ).show();
                                progressBar.setVisibility(View.GONE);

                                if ( ! task.isSuccessful()) {
                                    Toast.makeText(
                                            SignUpActivity.this,
                                            "Authentication Failed!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                } else {
                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
