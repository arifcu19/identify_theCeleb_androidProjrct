package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Map;

public class Home extends AppCompatActivity implements View.OnClickListener {



    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private CardView review, detect, map, upload;
    private YouTubePlayerView youTubePlayerView;

    private RatingBar ratingBar;
    private TextView ratingValue;
    DatabaseReference ratedReference;


    private Spinner spinner;
    private String [] fields;
    private boolean isFirstSelection = true;

    int [] image = {R.drawable.ic_football,R.drawable.ic_cricket,R.drawable.ic_user};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");


        //For Spinner
        fields = getResources().getStringArray(R.array.fields);

        spinner = (Spinner) findViewById(R.id.spinnerId);
        AdapterSpinner adapterSpinner = new AdapterSpinner(this,image,fields);
        spinner.setAdapter(adapterSpinner);


        youTubePlayerView = findViewById(R.id.youTubeId);

        review = findViewById(R.id.goToReview);
        detect = findViewById(R.id.goToDetect);
        map = findViewById(R.id.goToMap);
        upload = findViewById(R.id.goToUpload);

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

        //Animation
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/index.html");


        //For youtube video
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {

                String videoId = "AKtTeEYG2uc";
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
                            Intent intent = new Intent(Home.this,SoccerClass.class);
                            startActivity(intent);
                            break;

                        case 1:
                            Intent intent2 = new Intent(Home.this,Cricketers.class);
                            startActivity(intent2);
                            break;

                        case 2:
                            Toast.makeText(Home.this, "Actors", Toast.LENGTH_SHORT).show();
                            break;

                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


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
                    ratingValue.setText("Rating "+String.valueOf(Math.floor(averageRating)));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.goToDetect:
                Intent intent = new Intent(Home.this,Detection.class);
                startActivity(intent);
                break;
            case R.id.goToReview:
                Intent i = new Intent(Home.this,ShowImageReview.class);
                startActivity(i);
                break;
            case R.id.goToMap:
                Intent ii = new Intent(Home.this,MapActivity.class);
                startActivity(ii);
                break;
            case R.id.goToUpload:
                Intent iii = new Intent(Home.this,ImageReview.class);
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
            case R.id.userProfileId:
                startActivity(new Intent(Home.this,ProfileUpdate.class));
                break;
            case R.id.logoutMenuId:
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(Home.this, Login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;

            case R.id.ratingMenuId:
                startActivity(new Intent(Home.this,RatingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}