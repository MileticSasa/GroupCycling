package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.groupcycling.MySharedPreferences;
import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelToken;
import com.example.groupcycling.model.ModelUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class GroupActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private CollectionReference ref;
    private FirebaseAuth auth;

    private Button trackBtn, seeParticipantsBtn, chatBtn;
    String groupName;
    private ModelUser user;


    public static String GROUP_NAME = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        groupName = intent.getStringExtra("name");

        GROUP_NAME = groupName;

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        ref = firestore.collection("Groups").document(""+groupName)
                .collection("Participants");

        setMyToken();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(groupName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));


        initViews();

        getCurrentUserData();

        seeParticipantsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GroupActivity.this, SeeParticipantsActivity.class);
                i.putExtra("name", groupName);
                startActivity(i);
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GroupActivity.this, GroupChatActivity.class);
                i.putExtra("name", groupName);
                startActivity(i);
            }
        });

        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GroupActivity.this, GroupShareActivity.class);
                i.putExtra("name", groupName);
                startActivity(i);
            }
        });
    }


    private void setMyToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(!task.isSuccessful()){
                    Log.d("OVDE", "Error getting token: " + task.getException());
                    return;
                }

                String token = task.getResult();
                ModelToken modelToken = new ModelToken(token);

                firestore.collection("Groups").document(""+groupName)
                        .collection("Tokens").document(""+auth.getCurrentUser().getUid())
                        .set(modelToken).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(GroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                MySharedPreferences.getInstance(GroupActivity.this).addToken(modelToken.getToken());
                            }
                        });

                firestore.collection("Tokens").document(""+auth.getCurrentUser().getUid())
                        .set(modelToken);
            }
        });
    }

    private void getCurrentUserData() {
        ref.document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                user = documentSnapshot.toObject(ModelUser.class);

            }
        });
    }

    private void initViews() {
        trackBtn = findViewById(R.id.dugme1);
        seeParticipantsBtn = findViewById(R.id.dugme2);
        chatBtn = findViewById(R.id.dugme3);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}