package com.example.devapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private CardView loginButton;
    private TextView forgotPassword, gotoSignup;
    private CheckBox saveLoginCheckBox;

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;


    private EditText edtEmail, edtPassword;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#236488"));
        actionBar.setBackgroundDrawable(colorDrawable);


        loginButton =  findViewById(R.id.signinButton);
        gotoSignup = findViewById(R.id.gotoSignupId);
        forgotPassword = findViewById(R.id.forgotPassword);

        loginButton.setOnClickListener(this);
        gotoSignup.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);


        edtEmail = findViewById(R.id.emailId);
        edtPassword = findViewById(R.id.passwordId);

        saveLoginCheckBox = (CheckBox)findViewById(R.id.remember);
        loginPreferences = this.getApplicationContext().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            edtEmail.setText(loginPreferences.getString("email", ""));
            edtPassword.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
        }

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.signinButton:
                login();
                remember();
                break;

            case R.id.gotoSignupId:
                Intent intent = new Intent(Login.this,Registration.class);
                startActivity(intent);
                break;

            case R.id.forgotPassword:
                Intent intent2 = new Intent(Login.this,ForgotPassword.class);
                startActivity(intent2);
                break;
        }
    }

    private void login() {

        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

       if(email.isEmpty()){
            edtEmail.setError("email required");
            edtEmail.requestFocus();
            return;
        }
        else if(password.isEmpty()) {
           edtPassword.setError("email required");
           edtPassword.requestFocus();
           return;}

       else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
           edtEmail.setError("Valid email required");
           edtEmail.requestFocus();
           return;
       }

       else if(password.length() < 6){
           edtPassword.setError("password should be at least 6 charcter");
           edtPassword.requestFocus();
           return;
       }
        else {
            progressDialog.setMessage("Wait while login");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if(user.isEmailVerified()){
                            nextActivity();
                        }else{
                            user.sendEmailVerification();
                            Toast.makeText(Login.this,"Email is not verified",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        progressDialog.dismiss();
                        String s = ""+task.getException();
                        if(s.length() == 137){
                            Toast.makeText(Login.this,"Network connection needed",Toast.LENGTH_SHORT).show();
                        }
                        else if(s.length() == 127){
                            Toast.makeText(Login.this,"Password invalid",Toast.LENGTH_SHORT).show();
                        }

                        else{
                            Toast.makeText(Login.this,""+task.getException(),Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }


    public  void remember(){

        InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtEmail.getWindowToken(), 0);

        String nEmail = edtEmail.getText().toString();
        String nPass = edtPassword.getText().toString();

        if (saveLoginCheckBox.isChecked()) {
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.putString("email", nEmail);
            loginPrefsEditor.putString("password", nPass);
            loginPrefsEditor.commit();
        } else {
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if(firebaseUser != null){
            Intent i = new Intent(Login.this, Home.class);
            startActivity(i);
            finish();
        }
    }

    private void nextActivity(){
        Intent i = new Intent(Login.this, Home.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

}