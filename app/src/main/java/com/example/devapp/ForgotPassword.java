package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText edt_email;
    private CardView resetButton;
    ProgressDialog progressDialog;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);

        edt_email =  findViewById(R.id.emailId);
        resetButton = findViewById(R.id.resetId);

        progressDialog = new ProgressDialog(this);



        auth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword(){

        String email = edt_email.getText().toString().trim();

        if(email.isEmpty()){
            edt_email.setError("email required");
            edt_email.requestFocus();
            return;
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edt_email.setError("Valid email required");
            edt_email.requestFocus();
            return;
        }

        else{

            progressDialog.setMessage("Wait while password is reset");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPassword.this,"Check your email to reset password",Toast.LENGTH_SHORT).show();
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPassword.this,"Something Wrong",Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

    }
}