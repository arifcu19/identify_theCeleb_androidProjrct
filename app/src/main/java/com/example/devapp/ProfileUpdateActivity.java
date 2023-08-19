package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.HashMap;
import java.util.Map;

public class ProfileUpdateActivity extends AppCompatActivity {

    private ImageView updateProfileImage;
    private EditText updateProfileName;
    private Button updateProfileButton;

    DatabaseReference profileDatabaseReference;
    StorageReference profileStorageReference;

    private static final int IMAGE_PICKER_REQUEST_CODE = 1;
    Uri imageUri;
    String userId = "";
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");

        updateProfileImage = findViewById(R.id.updateProfileImageId);
        updateProfileName = findViewById(R.id.updateProfileNameId);
        updateProfileButton = findViewById(R.id.updateButtonId);
        username = getIntent().getStringExtra("userName");
        updateProfileName.setText(username);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        profileDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Userprofile");
        profileStorageReference = FirebaseStorage.getInstance().getReference();


        updateProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(updateProfileName.getText().toString().isEmpty()){
                    updateProfileName.setText(username);
                }
                updateProfile();
            }
        });

    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                updateProfileImage.setImageURI(imageUri);
            }
        }
    }



    private void updateProfile() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Profile Updating");
        progressDialog.show();
        String updateName = updateProfileName.getText().toString();


         if (imageUri == null ) {
            profileDatabaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        profileDatabaseReference.child(userId).child("uname").setValue(updateName);
                        progressDialog.dismiss();
                        //backToProfileActivity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        else{
            final StorageReference uploader = profileStorageReference.child("profileimages/" + "img" + System.currentTimeMillis());
            uploader.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    final Map<String, Object> map = new HashMap<>();
                                    map.put("uimage", uri.toString());
                                    map.put("uname", username);

                                    profileDatabaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                profileDatabaseReference.child(userId).updateChildren(map);
                                            } else {
                                                profileDatabaseReference.child(userId).setValue(map);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileUpdateActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                    //backToProfileActivity();
                                }
                            });

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Updated " + (int) progress + "%");
                        }
                    });
        }


    }

    private void backToProfileActivity(){
        Intent i = new Intent(ProfileUpdateActivity.this, ProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

}