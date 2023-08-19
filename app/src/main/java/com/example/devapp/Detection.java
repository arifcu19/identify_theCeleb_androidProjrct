package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.devapp.ml.ModelUnquant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Detection extends AppCompatActivity {


    private ImageView imageView;
    private TextView captureButton,selectButton;
    private TextView result, searchResult;

    private Bitmap bitmap;
    int imageSize = 224;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);


        ActionBar actionBar;
        actionBar = getSupportActionBar();

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("UserHistory").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference("UserHistory");


        imageView = findViewById(R.id.imageVW);
        selectButton = findViewById(R.id.btnSelectPicture);
        captureButton = findViewById(R.id.btnTakePicture);
        result = findViewById(R.id.resultOptions);
        searchResult = findViewById(R.id.clickOptions);

        getPermissiion();

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,10);
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,12);

            }
        });

    }


    void getPermissiion() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Detection.this, new String[]{Manifest.permission.CAMERA},11);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 11) {
            if (grantResults.length > 0) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    this.getPermissiion();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 10) {
            if (data != null) {
                imageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                    bitmap = Bitmap.createScaledBitmap(bitmap,imageSize,imageSize,false);
                    imageView.setImageBitmap(bitmap);
                    classifyImage(bitmap,imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (requestCode == 12) {
            if(data != null){
                bitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
                //Getting Image Uri
               String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),bitmap," "+System.currentTimeMillis(),null);
               Uri imageUri = Uri.parse(path);
               classifyImage(bitmap,imageUri);

            }

        }
        super.onActivityResult(requestCode, resultCode, data);

    }


    public String getFileExtension(Uri imageUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }


    public void classifyImage(Bitmap image, Uri imageUri){
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3); //4 byte for float, 4th dimension-3
            byteBuffer.order(ByteOrder.nativeOrder());

            //Pixel Values
            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){

                    int val = intValues[pixel ++]; //RGB

                    //Bitwise operation for extracting RGB from Pixel
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }


            inputFeature0.loadBuffer(byteBuffer);
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;

            float maxConfidenc = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidenc){
                    maxConfidenc = confidences[i];
                    maxPos = i;
                }
            }


            String[] classes = {"Angelina Jolie", "Arif Hasan", "Brad Pitt", "Hugh Jackman", "Johny Depp", "Kate Winslet", "Leonardo Decaprio", "Robert Downey Jr", "Tom Cruise", "Tom Hanks"};
            result.setText(classes[maxPos]);
            saveData(imageUri);
            searchResult.setText("Click here");
            searchResult.setPaintFlags(searchResult.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            searchResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String searchText = result.getText().toString().trim();
                    if (!searchText.isEmpty()) {
                        performGoogleSearch(searchText);
                    }
                }
            });

            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }


    }



    //Creating UserHistory
    private void saveData(Uri imageUri){
        Object timestamp = ServerValue.TIMESTAMP;
        String imageName = result.getText().toString().trim();
            StorageReference ref = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();

                            UserHistoryDetails userHistoryDetails = new UserHistoryDetails(imageName, downloadUri.toString(), timestamp);
                            String uploadId = databaseReference.push().getKey();
                            databaseReference.child(uploadId).setValue(userHistoryDetails);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Detection.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

    }

    private void performGoogleSearch(String searchText) {
        String encodedSearchText = Uri.encode(searchText);
        String searchUrl = "https://www.google.com/search?q=" + encodedSearchText;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
        startActivity(intent);
    }


}