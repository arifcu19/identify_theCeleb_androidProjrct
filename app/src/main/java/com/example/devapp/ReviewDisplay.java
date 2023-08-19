package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReviewDisplay extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ReviewUpload> reviewUploadList;
    DatabaseReference databaseReference, likeref;
    private ProgressBar progressBar;

    private FloatingActionButton floatingActionButton;
    Boolean testclick = false;

    private Geocoder geocoder;
    String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_display);

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");


        geocoder = new Geocoder(this);
        recyclerView = findViewById(R.id.reView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.recycleProgressId);

        floatingActionButton = findViewById(R.id.fabUplaodId);

        reviewUploadList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Upload");
        likeref =  FirebaseDatabase.getInstance().getReference("Likes");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ReviewDisplay.this,GiveReview.class);
                startActivity(i);
            }
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot datasnap : snapshot.getChildren()){
                    ReviewUpload reviewUpload = datasnap.getValue(ReviewUpload.class);
                    reviewUploadList.add(reviewUpload);
                }

                progressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(ReviewDisplay.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });



        FirebaseRecyclerOptions<ReviewUpload> options =
                new FirebaseRecyclerOptions.Builder<ReviewUpload>()
                        .setQuery(databaseReference, ReviewUpload.class)
                        .build();


        FirebaseRecyclerAdapter<ReviewUpload, MyViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ReviewUpload, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull ReviewUpload model) {
                ReviewUpload reviewUpload = reviewUploadList.get(position);
                holder.textView.setText(reviewUpload.getImageName());
                holder.userTextView.setText(reviewUpload.getUsername());
                Glide.with(getApplicationContext()).load(reviewUpload.getUserImage()).into(holder.userImage);

                Double latitude = reviewUpload.getLatitude();
                Double longitude = reviewUpload.getLongitude();
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String placeName = address.getAddressLine(0);
                        holder.currentLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location, 0, 0, 0);
                        holder.currentLocation.setText(placeName);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Picasso.get().load(reviewUpload.getImageUri())
                        .fit()
                        .centerCrop()
                        .into(holder.imageView);



                holder.userTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ReviewDisplay.this, LoadingUserProfile.class);
                        intent.putExtra("userId",reviewUpload.getUserId());
                        startActivity(intent);
                    }
                });




              //Bottom Sheet for data delete
               int pos = position;
               if(reviewUpload.getUserId().toString().equals(userId)){
               //User Profile Visiting
               int newColor = Color.rgb(70,130,180); // Change this to the desired color
               holder.userTextView.setTextColor(newColor);
               holder.userTextView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       Intent intent = new Intent(ReviewDisplay.this, ProfileActivity.class);
                       startActivity(intent);
                   }
               });

               //Dynamically alotted three dot button
               Drawable imageDrawable = getResources().getDrawable(R.drawable.ic_three_dot);
               holder.reviewBottomsheet.setImageDrawable(imageDrawable);

                holder.reviewBottomsheet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Dialog dialog = new Dialog(ReviewDisplay.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.bottom_sheet_layout);
                        LinearLayout shareLayout = dialog.findViewById(R.id.layoutShareId);
                        LinearLayout removeItem = dialog.findViewById(R.id.removeLayoutId);

                        shareLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }

                        });

                        removeItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DatabaseReference itemRef = getRef(pos);
                                String imagePath = reviewUpload.getImageUri();
                                deleteItem(itemRef, imagePath);
                                dialog.dismiss();

                            }
                        });


                        dialog.show();
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().getAttributes().windowAnimations = com.karumi.dexter.R.style.Base_Widget_AppCompat_PopupWindow;
                        dialog.getWindow().setGravity(Gravity.BOTTOM);

                    }
                });
            }

                //For full image view
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ReviewDisplay.this, PictureFullView.class);
                        intent.putExtra("picture",reviewUpload.getImageUri());
                        startActivity(intent);
                    }
                });


                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String userId = firebaseUser.getUid();
                String postkey = getRef(position).getKey();

                holder.getLikeStatus(postkey,userId);


                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testclick = true;

                        likeref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(testclick == true){
                                    if(snapshot.child(postkey).hasChild(userId)){
                                        likeref.child(postkey).child(userId).removeValue();
                                        testclick = false;
                                    }
                                    else{
                                        likeref.child(postkey).child(userId).setValue(true);
                                        testclick = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });

                holder.commentsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(),CommentPanel.class);
                        intent.putExtra("postkey",postkey);
                        startActivity(intent);
                    }
                });



            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_like_comment_layout,parent,false);
                return new MyViewHolder(view);

            }
        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void deleteItem(DatabaseReference itemRef, String imagePath) {

        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);
        imageRef.delete();

        itemRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ReviewDisplay.this, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReviewDisplay.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}