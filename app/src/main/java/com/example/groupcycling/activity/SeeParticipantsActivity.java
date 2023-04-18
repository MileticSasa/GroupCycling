package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import com.example.groupcycling.R;
import com.example.groupcycling.adapter.ParticipantsAdapter;
import com.example.groupcycling.model.ModelParticipant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SeeParticipantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ParticipantsAdapter adapter;
    private ArrayList<ModelParticipant> participantList = new ArrayList<>();
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_participants);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowHomeEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));

        Intent intent = getIntent();
        groupName = intent.getStringExtra("name");
        bar.setTitle(groupName);
        Intent intent1 = getIntent();

        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParticipantsAdapter(this, null);
        recyclerView.setAdapter(adapter);
        loadParticipants();

    }

    private void loadParticipants() {
        participantList = new ArrayList<>();

        CollectionReference ref = FirebaseFirestore.getInstance().collection("Groups")
                .document(""+groupName).collection("Participants");

        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot dc : queryDocumentSnapshots){
                            ModelParticipant model = dc.toObject(ModelParticipant.class);
                            participantList.add(model);
                        }

                        adapter.setParticipantList(participantList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SeeParticipantsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}