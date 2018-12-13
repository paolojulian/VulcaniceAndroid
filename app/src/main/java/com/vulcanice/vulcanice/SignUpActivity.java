package com.vulcanice.vulcanice;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vulcanice.vulcanice.Model.VCN_User;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by paolo on 5/2/18.
 */

public class SignUpActivity extends AppCompatActivity {
    private EditText email, password, name, mobile;
    private Button btnLinkToSignIn, btnSignUp, btnResetPassword, btnChooseImg, btnUploadImg;
    private ImageView userImg;
    private Spinner userType;
    private ProgressBar progressBar;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PERMISSION_REQUEST_CODE = 7171;

    private Uri filePath;

    private String IMG_URL;
    private final int PICK_IMAGE_REQUEST = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        btnSignUp = findViewById(R.id.btn_sign_up);
        name = findViewById(R.id.signup_name);
        email = findViewById(R.id.signup_email);
        mobile = findViewById(R.id.mobile_number);
        password = findViewById(R.id.signup_password);
        progressBar = findViewById(R.id.progress_bar);
        btnChooseImg = findViewById(R.id.btn_choose_user_image);
        btnUploadImg = findViewById(R.id.btn_upload_image);
        userImg = findViewById(R.id.img_user);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        populateUserType();
        eventChooseImg();

        btnSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String inputName = name.getText().toString().trim();
                final String inputEmail = email.getText().toString().trim();
                final String inputPassword = password.getText().toString().trim();
                final String inputMobile = mobile.getText().toString().trim();
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
                if (TextUtils.isEmpty(inputMobile)) {
                    Toast.makeText(getApplicationContext(), "Please enter a mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(inputUserType)) {
                    Toast.makeText(getApplicationContext(), "Please select a user type", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);
                Intent i = new Intent(SignUpActivity.this, VerifyPhoneNumberActivity.class);
                i.putExtra("name", inputName);
                i.putExtra("email", inputEmail);
                i.putExtra("password", inputPassword);
                i.putExtra("mobile", inputMobile);
                i.putExtra("usertype", inputUserType);
                if (filePath != null) {
                    i.putExtra("filepath", filePath.toString());
                }
                startActivity(i);
//                auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
//                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                progressBar.setVisibility(View.GONE);
//
//                                if ( ! task.isSuccessful()) {
//                                    Toast.makeText(
//                                            SignUpActivity.this,
//                                            "Unable to process your request",
//                                            Toast.LENGTH_SHORT
//                                    ).show();
//                                    return;
//                                }
//                                AuthCredential credential = EmailAuthProvider.getCredential(inputEmail, inputPassword);
//                                setAdditionalFields(
//                                    new VCN_User(
//                                        inputName,
//                                        inputMobile,
//                                        inputEmail,
//                                        inputUserType
//                                    ),
//                                    credential
//                                );
//                                IMG_URL = ("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
//                                uploadImage();
//                                finish();
//                            }
//                        });
            }
        });
    }

    private void eventChooseImg() {
        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SignUpActivity.this, new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_CODE);
                    return;
                }
                chooseImage();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                userImg.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
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

    private void uploadImage() {
        if (filePath == null) {
            return;
        }

//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Uploading...");
//        progressDialog.show();

        StorageReference ref = storageReference.child(IMG_URL);
        ref.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        progressDialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        progressDialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    protected void populateUserType() {
        userType = findViewById(R.id.user_type);
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

    @Override
    protected void onStop() {
        super.onStop();
    }
}
