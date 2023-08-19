package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadingUserProfile extends AppCompatActivity {

    private ImageView userImage;
    private TextView userEmail, userName, userPlace, userBirthdate;
    private Button messageButton;

    private RecyclerView recyclerView;
    private List<PostLoaderClass> postLoaderClassList;

    DatabaseReference profileDatabaseReference, usersDatabaseReference, connectionDataRef, postDatabaseReference;
    StorageReference profileStorageReference;
    private String imageUri;
    String receiverUserId = "";
    String currentUserId = "";
    String username;

    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_user_profile);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");


        profileDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Userprofile");
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        profileStorageReference = FirebaseStorage.getInstance().getReference();
        connectionDataRef = FirebaseDatabase.getInstance().getReference("ConnectedWith");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user.getUid();


        userImage = findViewById(R.id.userImageLoading);
        userName = findViewById(R.id.userProfileNameLoading);
        userEmail = findViewById(R.id.userProfileEmailLoading);
        userPlace = findViewById(R.id.userProfilePlaceLoading);
        userBirthdate = findViewById(R.id.userProfileBirthDateLoading);
        messageButton = findViewById(R.id.messageBtnId);

        recyclerView = findViewById(R.id.recycleVWPostReceiver);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postDatabaseReference = FirebaseDatabase.getInstance().getReference("Post");
        postLoaderClassList = new ArrayList<>();

        //Getting User Id
        receiverUserId = getIntent().getStringExtra("userId");
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullImageView();
            }
        });


        connectButton = findViewById(R.id.connectBtnId);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectButton.setText("Requested");
                connectionRequest();
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoadingUserProfile.this,MessagingActivity.class);
                intent.putExtra("receiveruserId",receiverUserId);
                startActivity(intent);
            }
        });





        postDatabaseReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot datasnap : snapshot.getChildren()) {
                    PostLoaderClass postLoaderClass = datasnap.getValue(PostLoaderClass.class);
                    postLoaderClassList.add(postLoaderClass);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoadingUserProfile.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });


        FirebaseRecyclerOptions<PostLoaderClass> options =
                new FirebaseRecyclerOptions.Builder<PostLoaderClass>()
                        .setQuery(postDatabaseReference, PostLoaderClass.class)
                        .build();


        FirebaseRecyclerAdapter<PostLoaderClass, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostLoaderClass, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull PostLoaderClass model) {

                PostLoaderClass postLoaderClass = postLoaderClassList.get(position);

/*

                if (postLoaderClass != null) {

                    try {
                        if(postLoaderClass.getPost().toString().isEmpty()){
                            Toast.makeText(LoadingUserProfile.this, "Okkk", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            holder.postText.setText(postLoaderClass.getPost());
                            Glide.with(getApplicationContext()).load(imageUri).into(holder.userPostImage);
                            holder.userPostName.setText(username);
                        }

                    }catch (Exception e){
                        Toast.makeText(LoadingUserProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(LoadingUserProfile.this, "Null", Toast.LENGTH_SHORT).show();
                }



*/


            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
                return new PostViewHolder(view);

            }
        };

        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);



    }


    @Override
    protected void onStart() {
        super.onStart();

        usersDatabaseReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userEmail.setText(snapshot.child("email").getValue().toString());
                userBirthdate.setText(snapshot.child("bDate").getValue().toString());
                userPlace.setText(snapshot.child("divPlusDis").getValue().toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profileDatabaseReference.child((receiverUserId)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    imageUri = snapshot.child("uimage").getValue().toString();
                    Glide.with(getApplicationContext()).load(imageUri).into(userImage);
                    username =snapshot.child("uname").getValue().toString();
                    userName.setText(username);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Checking Connection Request
        connectionDataRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String uId = snapshot.child("userId").getValue().toString();
                    if(uId.contentEquals(receiverUserId)){
                        connectButton.setText("Requested");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void connectionRequest() {
        final Map<String, Object> map = new HashMap<>();
        map.put("userId", currentUserId.toString());

        connectionDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                connectionDataRef.child(receiverUserId).setValue(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void fullImageView(){
        Intent intent = new Intent(LoadingUserProfile.this, PictureFullView.class);
        intent.putExtra("picture",imageUri);
        startActivity(intent);
    }


}