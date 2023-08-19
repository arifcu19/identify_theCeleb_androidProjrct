package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.ArrayList;
import java.util.List;

public class UserHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<UserHistoryDetails> userHistoryDetailsList;
    DatabaseReference databaseReference;
    private ProgressBar progressBar;
    long expirationDurationInMillis, storedTimestamp, expirationThreshold, currentTimestamp;

    ImageView deleteAllData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");

        recyclerView = findViewById(R.id.historyRecycleViewId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.recycleProgressId);

        deleteAllData = findViewById(R.id.deleteAllHistoryId);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        userHistoryDetailsList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("UserHistory").child(userId);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot datasnap : snapshot.getChildren()) {
                    UserHistoryDetails userHistoryDetails = datasnap.getValue(UserHistoryDetails.class);
                    userHistoryDetailsList.add(userHistoryDetails);

                }

                progressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(UserHistoryActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });


        FirebaseRecyclerOptions<UserHistoryDetails> options =
                new FirebaseRecyclerOptions.Builder<UserHistoryDetails>()
                        .setQuery(databaseReference, UserHistoryDetails.class)
                        .build();


        FirebaseRecyclerAdapter<UserHistoryDetails, UserHistoryViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UserHistoryDetails, UserHistoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserHistoryViewHolder holder, int position, @NonNull UserHistoryDetails model) {
                UserHistoryDetails userHistoryDetails = userHistoryDetailsList.get(position);

                holder.history_TextView.setText(userHistoryDetails.getImageName());
                Picasso.get().load(userHistoryDetails.getImageUri())
                        .fit()
                        .centerCrop()
                        .into(holder.history_image_View);


                int pos = position;
                //Deleting all history

                if(userHistoryDetails != null){
                    deleteAllData.setImageResource(R.drawable.ic_delete_all);
                    deleteAllData.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(UserHistoryActivity.this, "Delete all", Toast.LENGTH_SHORT).show();
                            deleteAllHistory();
                        }
                    });
                }


                //Auto delete history
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                         expirationDurationInMillis = 43200000;
                         storedTimestamp = (long) userHistoryDetails.getTimestamp();
                         expirationThreshold = storedTimestamp + expirationDurationInMillis;
                         currentTimestamp = System.currentTimeMillis();

                        if (currentTimestamp >= expirationThreshold) {
                            DatabaseReference itemRef = getRef(pos);
                            String imagePath = userHistoryDetails.getImageUri();
                            deleteItem(itemRef, imagePath);
                        }
                    }
                }, 5000);


                holder.bottomSheet.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        final Dialog dialog = new Dialog(UserHistoryActivity.this);
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
                                String imagePath = userHistoryDetails.getImageUri();
                                deleteItem(itemRef, imagePath);
                                dialog.dismiss();

                            }
                        });


                        dialog.show();
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().getAttributes().windowAnimations = com.karumi.dexter.R.style.Base_Widget_AppCompat_PopupWindow;
                        dialog.getWindow().setGravity(Gravity.BOTTOM);


                        return false;
                    }
                });

            }

            @NonNull
            @Override
            public UserHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_history_layout, parent, false);
                return new UserHistoryViewHolder(view);

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
                        Toast.makeText(UserHistoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserHistoryActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void deleteAllHistory(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("UserHistory");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(userId)){
                    databaseRef.child(userId).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
