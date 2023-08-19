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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class ProfileActivity extends AppCompatActivity {

    private ImageView userImage;
    private TextView userEmail, userName, userPlace, userBirthdate, userPassword;
    private String username, useremail, user_birthdate, userpassword;

    private EditText postText;
    private Button postButton;
    private RecyclerView recyclerView;
    private List<PostLoaderClass> postLoaderClassList;


    DatabaseReference profileDatabaseReference, usersDatabaseReference, postDatabaseReference;
    StorageReference profileStorageReference;
    private String imageUri;

    String userId = "";
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");


        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.userProfileName);
        userEmail = findViewById(R.id.userProfileEmail);
        userPlace = findViewById(R.id.userProfilePlace);
        userBirthdate = findViewById(R.id.userProfileBirthDate);
        userPassword = findViewById(R.id.userProfilePassword);
        postText = findViewById(R.id.postTextId);
        postButton = findViewById(R.id.postButtonId);
        recyclerView = findViewById(R.id.recycleVWPostCurrentUser);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullImageView();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        profileDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Userprofile");
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        profileStorageReference = FirebaseStorage.getInstance().getReference();
        postDatabaseReference = FirebaseDatabase.getInstance().getReference("Post");
        postLoaderClassList = new ArrayList<>();


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postMethod();
            }
        });


        postDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(userId)){
                    for (DataSnapshot datasnap : snapshot.child(userId).getChildren()) {
                        PostLoaderClass postLoaderClass = datasnap.getValue(PostLoaderClass.class);
                        postLoaderClassList.add(postLoaderClass);
                    }
                }
                else{
                    count = 1;
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });


        FirebaseRecyclerOptions<PostLoaderClass> options =
                new FirebaseRecyclerOptions.Builder<PostLoaderClass>()
                        .setQuery(postDatabaseReference, PostLoaderClass.class)
                        .build();


        FirebaseRecyclerAdapter<PostLoaderClass, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostLoaderClass, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull PostLoaderClass model) {

            /*    if(count == 0){
                PostLoaderClass postLoaderClass = postLoaderClassList.get(position);
                holder.postText.setText(postLoaderClass.getPost());
                Glide.with(getApplicationContext()).load(imageUri).into(holder.userPostImage);
                holder.userPostName.setText(userName.getText());
                }
                else {
                    holder.postText.setText("");
                    Glide.with(getApplicationContext()).load(R.drawable.ic_account).into(holder.userPostImage);
                }*/

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.updateProfileMenuId:
                goToUpdateProfile();
                break;

            case R.id.reportGenertaeMenuId:
                goToReportGen();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();

        usersDatabaseReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                useremail = snapshot.child("email").getValue().toString();
                userEmail.setText(useremail);
                username = snapshot.child("username").getValue().toString();
                userName.setText(username);
                userpassword = snapshot.child("password").getValue().toString();
                userPassword.setText(userpassword);
                user_birthdate = snapshot.child("bDate").getValue().toString();
                userBirthdate.setText(user_birthdate);
                userPlace.setText(snapshot.child("divPlusDis").getValue().toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profileDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(userId)){
                    imageUri = snapshot.child(userId).child("uimage").getValue().toString();
                    Glide.with(getApplicationContext()).load(imageUri).into(userImage);
                }
                else{
                    userImage.setImageResource(R.drawable.ic_account);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fullImageView(){
        Intent intent = new Intent(ProfileActivity.this, PictureFullView.class);
        intent.putExtra("picture",imageUri);
        startActivity(intent);
    }

    private void goToUpdateProfile(){
        Intent intent = new Intent(ProfileActivity.this,ProfileUpdateActivity.class);
        intent.putExtra("userName",username);
        startActivity(intent);
    }

    private void goToReportGen(){
        Intent intent = new Intent(ProfileActivity.this, ReportGenerator.class);
        intent.putExtra("picture",imageUri);
        intent.putExtra("userName",username);
        intent.putExtra("userEmail",useremail);
        intent.putExtra("userPassword",userpassword);
        intent.putExtra("userBirthdate",user_birthdate);
        startActivity(intent);
    }

    private void postMethod() {
        String postTexts = postText.getText().toString();
        if(!postTexts.isEmpty()){
            String postkey = postDatabaseReference.push().getKey();
            final Map<String, Object> map = new HashMap<>();
            map.put("post", postTexts);
            map.put("timestamp", System.currentTimeMillis());

            postDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    postDatabaseReference.child(userId).child(postkey).setValue(map);
                    postText.setText("");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}