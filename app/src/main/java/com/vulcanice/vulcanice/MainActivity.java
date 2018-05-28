package com.vulcanice.vulcanice;

import android.content.Intent;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            startActivity(new Intent(MainActivity.this, MainPage.class));
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
