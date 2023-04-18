package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groupcycling.MySharedPreferences;
import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEt, emailEt, passwordEt;
    private Button registerBtn;
    private TextView haveAcc;

    private FirebaseAuth auth;

    private ModelUser modelUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));

        initViews();

        auth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEt.getText().toString().trim();
                String pass = passwordEt.getText().toString().trim();
                String nickname = nameEt.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailEt.setError("Invalid email");
                    emailEt.setFocusable(true);
                }
                else if(pass.length() < 6){
                    passwordEt.setError("Password must have at least 6 characters");
                    passwordEt.setFocusable(true);
                }
                else if(nickname.length() < 1){
                    nameEt.setError("You have to enter nickname");
                    nameEt.setFocusable(true);
                }
                else{
                    registerUser(email, pass, nickname);
                }
            }
        });

        haveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String pass, String nickname) {

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();

                            String email = user.getEmail();
                            String uid = user.getUid();

                            modelUser = new ModelUser();
                            modelUser.setEmail(email);
                            modelUser.setUid(uid);
                            modelUser.setName(nickname);
                            modelUser.setOnlineStatus("");
                            modelUser.setImage("");
                            modelUser.setDistance(0.0);
                            modelUser.setTopSpeed(0.0);
                            modelUser.setLatLng(null);

                            MySharedPreferences.getInstance(RegisterActivity.this).addMyUid(uid);

                            FirebaseFirestore base = FirebaseFirestore.getInstance();
                            base.collection("Users").document(uid).set(modelUser)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            Toast.makeText(RegisterActivity.this, "You are member now!", Toast.LENGTH_SHORT).show();

                                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {

        nameEt = findViewById(R.id.name_Et);
        emailEt = findViewById(R.id.email_Et);
        passwordEt = findViewById(R.id.password_Et);
        registerBtn = findViewById(R.id.registerBtn);
        haveAcc = findViewById(R.id.have_accountTv);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}