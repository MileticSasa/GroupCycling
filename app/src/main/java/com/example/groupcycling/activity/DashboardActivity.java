package com.example.groupcycling.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.groupcycling.R;
import com.example.groupcycling.fragment.ProfileFragment;
import com.example.groupcycling.fragment.RideFragment;
import com.example.groupcycling.fragment.TopListFragment;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private ActionBar actionBar;

    private LinearLayout homeLayout, rideLayout, scoreLayout;
    public int selected_fragment_number = 1;

    ImageView profileImg, rideImg, scoreImg;
    TextView profileTxt, rideTxt, scoreTxt;

    private String mUid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));
        actionBar.setTitle("");

        auth = FirebaseAuth.getInstance();

        homeLayout = findViewById(R.id.home_layout);
        scoreLayout = findViewById(R.id.score_layout);
        rideLayout = findViewById(R.id.ride_layout);
        profileImg = findViewById(R.id.imageHome);
        profileTxt = findViewById(R.id.txtHome);
        rideImg = findViewById(R.id.imageRide);
        rideTxt = findViewById(R.id.txtRide);
        scoreImg = findViewById(R.id.imageScore);
        scoreTxt = findViewById(R.id.txtScore);

        setVisibleFragment();


        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected_fragment_number = 1;
                setVisibleFragment();
                getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        rideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected_fragment_number = 2;
                setVisibleFragment();
                getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.fragment_container, new RideFragment()).commit();
            }
        });

        scoreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected_fragment_number = 3;
                setVisibleFragment();
                getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.fragment_container, new TopListFragment()).commit();
            }
        });
    }

    private void setVisibleFragment() {
        switch (selected_fragment_number){
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                profileImg.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
                profileTxt.setTextColor(Color.parseColor("#FFFFFF"));
                rideImg.setColorFilter(Color.parseColor("#877A7A"), PorterDuff.Mode.SRC_IN);
                rideTxt.setTextColor(Color.parseColor("#877A7A"));
                scoreImg.setColorFilter(Color.parseColor("#877A7A"), PorterDuff.Mode.SRC_IN);
                actionBar.setTitle("Profile");
                scoreTxt.setTextColor(Color.parseColor("#877A7A"));
                break;
            case 2:
                rideImg.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
                rideTxt.setTextColor(Color.parseColor("#FFFFFF"));
                scoreImg.setColorFilter(Color.parseColor("#877A7A"), PorterDuff.Mode.SRC_IN);
                scoreTxt.setTextColor(Color.parseColor("#877A7A"));
                profileImg.setColorFilter(Color.parseColor("#877A7A"), PorterDuff.Mode.SRC_IN);
                profileTxt.setTextColor(Color.parseColor("#877A7A"));
                actionBar.setTitle("Ride");
                break;
            case 3:
                scoreImg.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
                scoreTxt.setTextColor(Color.parseColor("#FFFFFF"));
                rideImg.setColorFilter(Color.parseColor("#877A7A"), PorterDuff.Mode.SRC_IN);
                rideTxt.setTextColor(Color.parseColor("#877A7A"));
                profileImg.setColorFilter(Color.parseColor("#877A7A"), PorterDuff.Mode.SRC_IN);
                profileTxt.setTextColor(Color.parseColor("#877A7A"));
                actionBar.setTitle("Top scorers");
                break;
        }
    }
}