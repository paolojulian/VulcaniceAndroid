package com.vulcanice.vulcanice;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vulcanice.vulcanice.Model.VCN_User;

import java.util.concurrent.TimeUnit;

/**
 * Created by Pipz on 12/12/2018.
 * This page verifies if the given phone number on sign up is valid
 */

public class VerifyPhoneNumberActivity extends AppCompatActivity
        implements View.OnClickListener{

    private static final String TAG = "TAG_VerifyMobile";
    // STATES
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNUP_FAILED = 5;
    private static final int STATE_SIGNUP_SUCCESS = 6;
    // EXTRAS
    private String name, email, password, mobile, usertype;
    private Uri filePath;
    // Layouts
    private EditText txtVerificationCode;
    private Button btnResendVerificationCode;
    private Button btnVerifyMobile;
    private ProgressBar progressBar;
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    // Callbacks
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    // Others
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_phone_number);

        initialProcess();
        setupCallbacks();
        startPhoneNumberVerification();
    }

    private void initialProcess() {
        // layouts
        txtVerificationCode = findViewById(R.id.txt_verification_code);
        btnResendVerificationCode = findViewById(R.id.btn_resend_verification_code);
        btnVerifyMobile = findViewById(R.id.btn_verify_mobile);
        progressBar = findViewById(R.id.progress_bar);
        // Assign Click Listeners
        btnResendVerificationCode.setOnClickListener(this);
        btnVerifyMobile.setOnClickListener(this);
        // Extras
        Bundle extras = getIntent().getExtras();
        name = extras.getString("name");
        email = extras.getString("email");
        password = extras.getString("password");
        mobile = extras.getString("mobile");
        usertype = extras.getString("usertype");
        if (extras.getString("filepath") != null) {
            filePath = Uri.parse(extras.getString("filepath"));
        }
        // Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void setupCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: " + phoneAuthCredential);

                mVerificationInProgress = false;

                updateUI(STATE_VERIFY_SUCCESS, phoneAuthCredential, null);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed: " + e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }


            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
    }

    /**
     * SEND VERIFICATION CODE TO PHONE
     */
    private void startPhoneNumberVerification() {
        Log.d("mobile", mobile);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true;
//        mStatusText.setVisibility(View.INVISIBLE);
    }
    /**
     * RESEND VERIFICATION CODE
     * @param token
     */
    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,        // Phone number to verify
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            // Immediately signout user, verification is the only thing needed
                            mAuth.signOut();
                            createUser();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(STATE_VERIFY_FAILED, credential, task);
                        }
                    }
                });
    }

    private void createUser() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(VerifyPhoneNumberActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if ( ! task.isSuccessful()) {
                            Toast.makeText(
                                    VerifyPhoneNumberActivity.this,
                                    R.string.db_error,
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }
                        setAdditionalFields();
                        uploadImage();
                    }
                });
    }

    protected void setAdditionalFields() {
        final AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        VCN_User user = new VCN_User(
            name,
            mobile,
            email,
            usertype
        );

        Log.d("User: ", mAuth.getCurrentUser().getUid());
        mDatabase.getReference("Users")
                .child(mAuth.getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);

                if ( ! task.isSuccessful()) {
                    signupFailed(credential);
                    return;
                }

                signupSuccess();
            }
        });
    }

    private void uploadImage() {
        if (filePath == null) {
            return;
        }

//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Uploading...");
//        progressDialog.show();

        String IMG_URL = ("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        StorageReference ref = storageReference.child(IMG_URL);
        ref.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        progressDialog.dismiss();
                        Toast.makeText(VerifyPhoneNumberActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        progressDialog.dismiss();
                        Toast.makeText(VerifyPhoneNumberActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
//                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_verify_mobile:
                String code = txtVerificationCode.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    txtVerificationCode.setError("Cannot be empty");
                    return;
                }
                disableViews(
                        btnResendVerificationCode,
                        btnVerifyMobile
                );
                progressBar.setVisibility(View.VISIBLE);
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.btn_resend_verification_code:
                resendVerificationCode(mResendToken);
                break;
            default:
                break;
        }
    }


    private void updateUI(int uiState, PhoneAuthCredential cred, Task task) {
        switch(uiState) {
            case STATE_VERIFY_SUCCESS:
                disableViews(
                        btnResendVerificationCode,
                        btnVerifyMobile
                );
                if (cred != null && cred.getSmsCode() != null) {
                    txtVerificationCode.setText(cred.getSmsCode());
                } else {
                    txtVerificationCode.setText(R.string.instant_validation);
//                    txtVerificationCode.setTextColor(R.color.colorAccent);
                }
                break;
            case STATE_VERIFY_FAILED:
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                    Toast.makeText(
                            VerifyPhoneNumberActivity.this,
                            "Invalid verification code",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void signupFailed(AuthCredential credential) {
        Toast.makeText(
                VerifyPhoneNumberActivity.this,
                "Authentication Failed!",
                Toast.LENGTH_SHORT
        ).show();
        final FirebaseUser user = mAuth.getCurrentUser();
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.delete();
                    }
                });
    }

    private void signupSuccess() {

        Toast.makeText(
                VerifyPhoneNumberActivity.this,
                "Registration Successful",
                Toast.LENGTH_SHORT).show();
        startActivity(new Intent(
                VerifyPhoneNumberActivity.this,
                MainActivity.class));
        finish();
    }

    /**
     * PRIVATE HELPERS
     */

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }
}
