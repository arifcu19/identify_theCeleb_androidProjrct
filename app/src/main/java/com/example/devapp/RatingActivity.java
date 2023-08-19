package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextView ratingValue, thankingText;

    private Button submitRating;
    private float userRating;
    String userId = "";
    DatabaseReference ratingReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ratingBar = findViewById(R.id.ratingId);
        ratingValue = findViewById(R.id.ratingValueId);
        thankingText = findViewById(R.id.thankingId);
        submitRating = findViewById(R.id.ratingSubmitId);


        ratingReference = FirebaseDatabase.getInstance().getReference("Rating");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = firebaseUser.getUid();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                userRating = rating;
                ratingValue.setText(""+userRating);

            }
        });

        submitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RatingDetails ratingDetails = new RatingDetails(userId, userRating);
                ratingReference.child(userId).setValue(ratingDetails)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    thankingText.setText("Thank you for rating us");
                                }
                                else{
                                    thankingText.setText("Error");
                                }
                            }
                        });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        ratingReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("userId")){
                    Object rating = dataSnapshot.child("rating").getValue();
                    float userRating = ((Number) rating).floatValue();
                    ratingBar.setRating(userRating);
                    ratingValue.setText(""+userRating);
                    ratingBar.setIsIndicator(true);
                    submitRating.setVisibility(View.GONE);
                    thankingText.setText("Thank you for rating us");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }
}