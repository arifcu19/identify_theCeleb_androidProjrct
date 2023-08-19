package com.example.devapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class PictureFullView extends AppCompatActivity {

    private PhotoView fullPicDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_full_view);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");

        fullPicDisplay = findViewById(R.id.fullProfilePicDisplay);
        String image = getIntent().getStringExtra("picture");
        Glide.with(this).load(image).into(fullPicDisplay);

        if(image == null){
            Glide.with(this).load(R.drawable.ic_account).into(fullPicDisplay);
        }
    }

}