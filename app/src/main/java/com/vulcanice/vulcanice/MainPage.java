package com.vulcanice.vulcanice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vulcanice.vulcanice.Model.VCN_User;

/**
 * Created by paolo on 5/27/18.
 */

public class MainPage extends AppCompatActivity {

    private Context context = MainPage.this;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseUser currentUser;
    private String userType;
    //MODELS
    private VCN_User userModel;

    private VCN_User user;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggleDrawer;
    private NavigationView mNavigationView;

    private Button BtnFindGas, BtnFindVul, BtnFindBoth,
            BtnListVul, BtnListGas, BtnListBoth;
    private ImageButton BtnNotification;
    private TextView notifCount, navName, navEmail, navMobile;
    private ImageView navImg;

    private Toolbar mToolbar;
    //NOTIFICATION
    private NotificationCompat.Builder mBuilder;
    private PendingIntent pendingIntent;
    private NotificationManager mNotificationManager;

    public FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PERMISSION_REQUEST_CODE = 7171;
    private Uri filePath;

    private String IMG_URL;
    private final int PICK_IMAGE_REQUEST = 71;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        setupToolbar();
        setupBtn();
//        setupNotification();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        eventGetClosestGas();
        setupDrawer();
        setupMenu();

        View headerLayout = mNavigationView.getHeaderView(0);
        navEmail = headerLayout.findViewById(R.id.navigation_email);
        navMobile = headerLayout.findViewById(R.id.navigation_mobile);
        navName = headerLayout.findViewById(R.id.navigation_name);
        navImg = headerLayout.findViewById(R.id.navigation_img_user);

        setupUserImage();
        setupText();
    }

    private void setupUserImage() {
        storageReference = FirebaseStorage.getInstance().getReference("images").child(currentUser.getUid());

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                GlideApp
                        .with(context)
                        .load(uri.toString())
                        .placeholder(R.drawable.default_prof_pic)
                        .into(navImg);
            }
        });

    }

    private void setupText() {


        DatabaseReference ref = mDatabase.getInstance().getReference("Users")
                .child(currentUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(VCN_User.class);
                if (userModel == null) {
                    return;
                }
                navEmail.setText(userModel.getEmail());
                navMobile.setText(userModel.getMobile());
                navName.setText(userModel.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void setupBtn() {
        BtnFindGas = findViewById(R.id.btn_find_gas);
        BtnFindVul = findViewById(R.id.btn_find_vul);
        BtnFindBoth = findViewById(R.id.btn_find_both);
        BtnListVul = findViewById(R.id.btn_list_vul);
        BtnListGas = findViewById(R.id.btn_list_gas);
        BtnListBoth = findViewById(R.id.btn_list_both);
    }

    private void setupNotification() {
        mBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(MainPage.this, ViewRequestsActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);

        setupNotificationActions();
        mBuilder.setSmallIcon(R.drawable.vulcanice_logo);
        mBuilder.setContentTitle("My notification");
        mBuilder.setContentText("Hello World!");
    }

    private void setupNotificationActions() {
        NotificationCompat.Action accept_request = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher, "Accept", pendingIntent
        ).build();
        NotificationCompat.Action decline_request = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher, "Decline", pendingIntent
        ).build();

        mBuilder.addAction(accept_request);
        mBuilder.addAction(decline_request);
    }

    private void eventGetClosestGas() {
        final Intent iFindShop = new Intent(MainPage.this, FindShopActivity.class);

        BtnFindGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iFindShop.putExtra("shopType", "gasStation");
                startActivity(iFindShop);
            }
        });

        BtnFindVul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iFindShop.putExtra("shopType", "vulcanizeStation");
                startActivity(iFindShop);
            }
        });

        BtnFindBoth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iFindShop.putExtra("shopType", "both");
                startActivity(iFindShop);

            }
        });
        BtnListVul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainPage.this, ViewListShopActivity.class);
                i.putExtra("shopType", "vulcanizeStation");
                startActivity(i);
            }
        });
        BtnListGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainPage.this, ViewListShopActivity.class);
                i.putExtra("shopType", "gasStation");
                startActivity(i);
            }
        });
        BtnListBoth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainPage.this, ViewListShopActivity.class);
                i.putExtra("shopType", "both");
                startActivity(i);
            }
        });
    }

    protected void getUserType() {
        DatabaseReference ref = mDatabase.getReference("Users")
                .child(currentUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(VCN_User.class);
                if (user == null) {
                    userType = "Client";
                    return;
                }
                userType = user.getUser_type();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainPage.this, "@string/db_error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        if (currentUser == null) {
            gotoSignIn();
        }
        mDatabase = FirebaseDatabase.getInstance();
        getUserType();
        super.onStart();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    //        Toast.makeText(MainPage.this, "Test", Toast.LENGTH_SHORT).show();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggleDrawer.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_check_requests:
                Log.d("test", "test");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        setupMenuItems(menu);
        displayNotifCount();
        return true;
    }

    private void displayNotifCount() {
        DatabaseReference notifReference = mDatabase.getReference()
                                                .child("Request")
                                                .child(currentUser.getUid());
        notifReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    if (item.child("isAccepted").getValue().toString().equals("0")) {
                        count ++;
                    }
                }
                notifCount.setText(Integer.toString(count));
//                mNotificationManager.notify(001, mBuilder.build());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupMenuItems(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_check_requests);

        eventNotificationCount(menuItem);
        eventClickNotification(menuItem);
    }

    private void eventClickNotification(MenuItem menuItem) {
        BtnNotification = menuItem.getActionView().findViewById(R.id.btn_notif);
        BtnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notifCount.getText().toString().equals("0")) {
                    return;
                }
                startActivity(new Intent(MainPage.this, ViewRequestsActivity.class));
            }
        });
    }

    private void eventNotificationCount(MenuItem menuItem) {
        notifCount = menuItem.getActionView().findViewById(R.id.notif_count);
    }

    private void setupToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void setupDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggleDrawer = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.vcn_open, R.string.vcn_close);
        mDrawerLayout.addDrawerListener(mToggleDrawer);
        mToggleDrawer.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupMenu() {
        mNavigationView =  findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.manage_account:
                        startActivity(new Intent(MainPage.this, EditAccountActivity.class));
                        return true;

                    case R.id.manage_shop:
                        if ( ! isConnected()) {
                            return true;
                        }
                        if (userType.equals("Client")) {
                            Toast.makeText(
                                    MainPage.this,
                                    "Must be a Shop Owner to manage shop",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return true;
                        }
                        startActivity(new Intent(MainPage.this, DashBoard.class));
                        return true;

                    case R.id.view_map:
                        if ( ! isConnected()) {
                            return true;
                        }
                        startActivity(new Intent(MainPage.this, ViewMapActivity.class));
                        return true;

                    case R.id.logout:
                        mAuth.signOut();
                        gotoSignIn();
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    protected boolean isConnected() {
        if (user == null) {
            Toast.makeText(
                    MainPage.this,
                    "Please Connect to the Internet",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }
        return true;
    }
    protected void gotoSignIn() {
        startActivity(new Intent(MainPage.this, MainActivity.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void gotoHome() {
        startActivity(new Intent(MainPage.this, MainPage.class));
    }
}

