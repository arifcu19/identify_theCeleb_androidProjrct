package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.text.DecimalFormat;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private TextView headerUserName;
    private ImageView headerUserImage;

    private WebView webView, webViewSass;
    private SwipeRefreshLayout swipeRefreshLayout;

    private CardView review, detect, map, upload;
    private YouTubePlayerView youTubePlayerView;

    private RatingBar ratingBar;
    private TextView ratingValue;
    DatabaseReference ratedReference, userProfileReference;
    String userId = "";
    private String imageUri;

    private Spinner spinner;
    private String [] fields;
    private boolean isFirstSelection = true;

    int [] image = {R.drawable.ic_football,R.drawable.ic_cricket,R.drawable.ic_user};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        drawerLayout = findViewById(R.id.drawerLayoutId);
        navigationView = findViewById(R.id.navigationVwId);
        toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);

        // Getting the header view
        View headerView = navigationView.getHeaderView(0);
        headerUserName = headerView.findViewById(R.id.headerUserName);
        headerUserImage = headerView.findViewById(R.id.headerUserImage);
        headerUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullImageView();
            }
        });


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.OpenDrawer,R.string.CloseDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.navRating){
                    Intent intent = new Intent(HomeActivity.this,RatingActivity.class);
                    startActivity(intent);
                }
                else if(id == R.id.navHistory){
                    Intent intent = new Intent(HomeActivity.this,UserHistoryActivity.class);
                    startActivity(intent);
                }

                else if(id == R.id.navProfile){
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }

                drawerLayout.closeDrawer(GravityCompat.START);


                return true;
            }
        });


        //For Spinner
        fields = getResources().getStringArray(R.array.fields);

        spinner = (Spinner) findViewById(R.id.spinnerId);
        AdapterSpinner adapterSpinner = new AdapterSpinner(this,image,fields);
        spinner.setAdapter(adapterSpinner);


        youTubePlayerView = findViewById(R.id.youTubeId);

        review = findViewById(R.id.goToReview);
        detect = findViewById(R.id.goToDetect);
        map = findViewById(R.id.goToMap);
        upload = findViewById(R.id.goToCelebList);

        review.setOnClickListener(this);
        detect.setOnClickListener(this);
        map.setOnClickListener(this);
        upload.setOnClickListener(this);


        ratingBar = findViewById(R.id.showRatingId);
        ratingValue = findViewById(R.id.showRatingValueId);
        ratedReference = FirebaseDatabase.getInstance().getReference().child("Rating");


        swipeRefreshLayout = findViewById(R.id.swipeId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreate();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //Gsap Animation
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/gsap/index.html");

        //SaSS
        webViewSass = findViewById(R.id.webViewForSass);
        webViewSass.getSettings().setJavaScriptEnabled(true);
        webViewSass.loadUrl("file:///android_asset/sass/index.html");

        //For youtube video
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {

                String videoId = "SdHe-JseJfQ";
                youTubePlayer.cueVideo(videoId,0);
                super.onReady(youTubePlayer);
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(isFirstSelection == true){
                    isFirstSelection = false;
                }
                else{
                    switch(i) {
                        case 0:
                            Intent intent = new Intent(HomeActivity.this,SoccerClass.class);
                            startActivity(intent);
                            break;

                        case 1:
                            Toast.makeText(HomeActivity.this, "Cricketers", Toast.LENGTH_SHORT).show();
                            break;

                        case 2:
                            Toast.makeText(HomeActivity.this, "Actors", Toast.LENGTH_SHORT).show();
                            break;

                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //Remainder for profile upload
        userProfileReference = FirebaseDatabase.getInstance().getReference("Userprofile");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = firebaseUser.getUid();

        userProfileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(userId)){

                    //AlertDialog for profile upload
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                            builder.setIcon(R.drawable.ic_upload);
                            builder.setTitle(" ");
                            builder.setMessage("Upload your profile picture");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Upload", (DialogInterface.OnClickListener) (dialog, which) -> {
                                Intent intent = new Intent(HomeActivity.this,ProfileUpload.class);
                                startActivity(intent);

                            });

                            builder.setNegativeButton("Later", (DialogInterface.OnClickListener) (dialog, which) -> {
                                dialog.cancel();
                            });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                        }
                    },5000);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        ratedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int totalUser = (int) dataSnapshot.getChildrenCount();
                float sumOfRating = 0;

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Map<String,Object> map = (Map<String, Object>) ds.getValue();
                    Object totalRating = map.get("rating");
                    float rating = Float.parseFloat(String.valueOf(totalRating));
                    sumOfRating += rating;

                    float averageRating = (sumOfRating/totalUser);

                    ratingBar.setRating(averageRating);
                    DecimalFormat df = new DecimalFormat("#.#");
                    ratingValue.setText(df.format(averageRating));


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        });


        //Data Loading in Header Layout
        userProfileReference.child((userId)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    imageUri = snapshot.child("uimage").getValue().toString();
                    Glide.with(getApplicationContext()).load(imageUri).into(headerUserImage);
                    headerUserName.setText(snapshot.child("uname").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }





    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.goToDetect:
                Intent intent = new Intent(HomeActivity.this,Detection.class);
                startActivity(intent);
                break;
            case R.id.goToReview:
                Intent i = new Intent(HomeActivity.this, ReviewDisplay.class);
                startActivity(i);
                break;
            case R.id.goToMap:
                Intent ii = new Intent(HomeActivity.this,MapActivity.class);
                startActivity(ii);
                break;
            case R.id.goToCelebList:
                Intent iii = new Intent(HomeActivity.this, Pagination.class);
                startActivity(iii);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.logoutMenuId:
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(HomeActivity.this, Login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;

            case R.id.settingId:
                Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show();

        }
        return super.onOptionsItemSelected(item);
    }


    private void fullImageView(){
        Intent intent = new Intent(HomeActivity.this, PictureFullView.class);
        intent.putExtra("picture",imageUri);
        startActivity(intent);
    }

}