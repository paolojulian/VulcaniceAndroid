package com.vulcanice.vulcanice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnSignUp;
    private final static int LOGIN_PERMISSION=1000;
    private FirebaseUser user;
    //STATIC_VALUES
    private static final int PERMISSION_REQUEST_CODE = 7171;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            startActivity(new Intent(MainActivity.this, MainPage.class));
            return;
        }

        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.INTERNET
                    },PERMISSION_REQUEST_CODE);
                    return;
                }
                startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                    .setAllowNewEmailAccounts(true)
                    .setTheme(R.style.AppTheme)
                    .build()
                    ,LOGIN_PERMISSION
                );
            }
        });
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if (requestCode == LOGIN_PERMISSION )
        {
            startNewActivity(resultCode, data);
        }
    }

    private void startNewActivity( int resultCode, Intent data ) {
        if ( resultCode == RESULT_OK ) {
            Intent intent = new Intent( MainActivity.this, MainPage.class );
            startActivity( intent );
            finish();
        }
    }
}
