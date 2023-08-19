package com.example.devapp;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.itextpdf.text.DocumentException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportGenerator extends AppCompatActivity {

    private TextView userName, userEmail, userBirthDate, userPassword;
    private String username, useremail, birthDate, password;
    private ImageView imageView;
    Button pdfG;
    int pageHeight = 850;
    int pagewidth = 830;
    Bitmap bmp, scaledbmp;
    String formattedDate;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private Bitmap receivedImageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_generator);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);
        this.setTitle("AppDev");

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        formattedDate = df.format(date);


        imageView = findViewById(R.id.reportImageViewId);
        userName = findViewById(R.id.userNameInReport);
        userEmail = findViewById(R.id.userEmailInReport);
        userBirthDate = findViewById(R.id.userBirtdateInReport);
        userPassword = findViewById(R.id.userPasswordInReport);
        pdfG = findViewById(R.id.reportGenerateBtn);

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.celeb);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

        String image = getIntent().getStringExtra("picture");
        Glide.with(ReportGenerator.this).load(image).into(imageView);
        username = getIntent().getStringExtra("userName");
        useremail = getIntent().getStringExtra("userEmail");
        birthDate = getIntent().getStringExtra("userBirthdate");
        password = getIntent().getStringExtra("userPassword");

        userName.setText("User Name: "+username);
        userEmail.setText("User Email: "+useremail);
        userBirthDate.setText("User Birthdate: "+birthDate);
        userPassword.setText("User Password: "+password);

        new DownloadImageTask().execute(image);


        pdfG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePdfAndSave();
            }

        });

    }

    private void generatePdfAndSave() {
        if (checkPermission()) {
            generatePDF();
        } else {
            requestPermission();
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                receivedImageBitmap = result;
            } else {

            }
        }
    }

    private void generatePDF() {

        String fileName = "celeb.pdf";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        startActivityForResult(intent, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    FileOutputStream outputStream = (FileOutputStream) getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        createPdf(outputStream);
                        outputStream.close();
                        Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void createPdf(FileOutputStream outputStream) throws DocumentException, IOException {

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint title = new Paint();

        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        Canvas canvas = myPage.getCanvas();
        canvas.drawBitmap(scaledbmp, 206, 20, paint);
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        float lineY = 200;
        canvas.drawLine(260, lineY, canvas.getWidth() - 260, lineY, title);

        title.setTextSize(15);
        title.setColor(ContextCompat.getColor(this, R.color.black));

        if (receivedImageBitmap != null) {
            int imageWidth = receivedImageBitmap.getWidth();
            int imageHeight = receivedImageBitmap.getHeight();
            float scaleWidth = (float) 200 / imageWidth;
            float scaleHeight = (float) 200 / imageHeight;

            // Choose the smaller scale factor to maintain the aspect ratio
            float scale = Math.min(scaleWidth, scaleHeight);

            int scaledWidth = (int) (imageWidth * scale);
            int scaledHeight = (int) (imageHeight * scale);

            float imageX = (pagewidth - scaledWidth) / 3; // Center the image horizontally
            float imageY = (pageHeight - scaledHeight) / 3; // Center the image vertically

            canvas.drawBitmap(Bitmap.createScaledBitmap(receivedImageBitmap, 180, 200, true), imageX, imageY, paint);

        }

        canvas.drawText("A portal for IT professionals", 260, 140, title);
        canvas.drawText("Explore the World", 260, 160, title);
        canvas.drawText(""+formattedDate, 260, 180, title);


        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.black));
        title.setTextSize(15);

        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("User Name: "+username, 346, 520, title);
        canvas.drawText("User Email: "+useremail, 346, 540, title);
        canvas.drawText("User Birthdate: "+birthDate, 346, 560, title);
        canvas.drawText("User Password: "+password, 346, 580, title);
        pdfDocument.finishPage(myPage);
        pdfDocument.writeTo(outputStream);
        pdfDocument.close();

    }

    private boolean checkPermission() {
        int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (!writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }




}