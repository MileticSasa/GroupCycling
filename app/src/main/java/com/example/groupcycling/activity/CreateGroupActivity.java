package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelGroup;
import com.example.groupcycling.model.ModelParticipant;
import com.example.groupcycling.model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameEt, passwordEt;
    private Button create;

    private ActionBar actionBar;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    DocumentReference reference;

    ModelUser modelcuga;

    List<ModelParticipant> participantList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("Users").document(""+user.getUid());

        getUserData();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Create group");
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));

        groupNameEt = findViewById(R.id.groupName);
        passwordEt = findViewById(R.id.password);
        create = findViewById(R.id.createGroupBtn);

        participantList = new ArrayList<>();

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreatingGroup();
            }
        });

    }


    private void startCreatingGroup() {

        String group_name = groupNameEt.getText().toString();
        String password = passwordEt.getText().toString();
        String group_id = "" + System.currentTimeMillis();
        String creator_name = modelcuga.getName();

        if(TextUtils.isEmpty(group_name)){
            Toast.makeText(this, "Please, enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please, enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        ModelParticipant participant = new ModelParticipant();
        participant.setUid(modelcuga.getUid());
        participant.setName(modelcuga.getName());
        participant.setImage(modelcuga.getImage());
        participant.setLatlng(modelcuga.getLatLng());

        createGroup(group_name, password, group_id, creator_name, participant);
    }

    private void createGroup(String group_name, String password, String group_id, String creatorName, ModelParticipant participant) {

        ModelGroup modelGroup = new ModelGroup(group_id, group_name, password, creatorName);

        CollectionReference ref = firestore.collection("Groups");
        ref.document(""+group_name)
                .set(modelGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        ref.document(""+group_name).collection("Participants").document(participant.getUid())
                .set(participant).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CreateGroupActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateGroupActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }


    private void getUserData(){

        reference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){

                            modelcuga = documentSnapshot.toObject(ModelUser.class);

                        }
                        else{
                            Toast.makeText(CreateGroupActivity.this, "Problem...", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}