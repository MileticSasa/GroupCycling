package com.example.groupcycling.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.groupcycling.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
        else{
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}