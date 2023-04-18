package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.groupcycling.R;
import com.example.groupcycling.adapter.GroupListAdapter;
import com.example.groupcycling.model.ModelGroup;
import com.example.groupcycling.model.ModelGroupList;
import com.example.groupcycling.model.ModelParticipant;
import com.example.groupcycling.model.ModelUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupRideActivity extends AppCompatActivity implements GroupListAdapter.OnGroupListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private List<ModelGroupList> groupList;
    private GroupListAdapter adapter;

    private ActionBar actionBar;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String uid;
    private String password;

    private ModelParticipant participant;

    private ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_ride);

        recyclerView = findViewById(R.id.recView);
        fab = findViewById(R.id.fab);
        pb = findViewById(R.id.pBar);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        uid = auth.getCurrentUser().getUid();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Groups");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        participant = getUserData();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupRideActivity.this, CreateGroupActivity.class));
            }
        });
    }

    private ModelParticipant getUserData() {

        ModelParticipant modelParticipant = new ModelParticipant();

        DocumentReference userRef = firestore.collection("Users").document(""+uid);
        userRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    return;
                }

                if(value.exists()){
                    ModelUser user = value.toObject(ModelUser.class);
                    String id = user.getUid();
                    String name = user.getName();
                    String image = user.getImage();
                    GeoPoint latLng = user.getLatLng();

                    modelParticipant.setUid(id);
                    modelParticipant.setLatlng(latLng);
                    modelParticipant.setImage(image);
                    modelParticipant.setName(name);
                }
            }
        });

        return modelParticipant;
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadGroups();
    }

    private void loadGroups() {

        groupList = new ArrayList<>();
        pb.setVisibility(View.VISIBLE);

        CollectionReference reference = firestore.collection("Groups");
        reference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                groupList.clear();
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    ModelGroupList model = documentSnapshot.toObject(ModelGroupList.class);
                    groupList.add(model);
                }

                adapter = new GroupListAdapter(GroupRideActivity.this, groupList, GroupRideActivity.this::onGroupClick);
                recyclerView.setAdapter(adapter);
                pb.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupRideActivity.this, ""+e.toString(), Toast.LENGTH_SHORT).show();
                pb.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onGroupClick(int position) {

        ModelGroupList group = groupList.get(position);
        String groupName = group.getGroupName();
        String groupId = group.getTimestamp();

        EnterOrJoin(groupName);
    }

    private void EnterOrJoin(String groupName) {

        pb.setVisibility(View.VISIBLE);

        CollectionReference reference = firestore.collection("Groups").document(""+groupName)
                .collection("Participants");

        reference.document(""+uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()){
                    Intent intent = new Intent(GroupRideActivity.this, GroupActivity.class);
                    intent.putExtra("name", groupName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    pb.setVisibility(View.GONE);
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupRideActivity.this);
                    builder.setMessage("Join group? Enter password.");

                    EditText editText = new EditText(GroupRideActivity.this);
                    editText.setHint("******");
                    builder.setView(editText);

                    builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            password = editText.getText().toString().trim();

                            DocumentReference docRef = firestore.collection("Groups").document("" + groupName);
                            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()) {
                                                ModelGroup modelGroup = documentSnapshot.toObject(ModelGroup.class);
                                                String groupPass = modelGroup.getPassword();

                                                if (groupPass.equals(null)) {
                                                    return;
                                                }

                                                if (password.equals(groupPass)) {

                                                    reference.document("" + uid)
                                                            .set(participant)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Intent intent = new Intent(GroupRideActivity.this, GroupActivity.class);
                                                                    intent.putExtra("name", groupName);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    startActivity(intent);
                                                                    pb.setVisibility(View.GONE);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(GroupRideActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                                    pb.setVisibility(View.GONE);
                                                                }
                                                            });
                                                }
                                                else{
                                                    Toast.makeText(GroupRideActivity.this, "Insert correct password", Toast.LENGTH_SHORT).show();
                                                    pb.setVisibility(View.GONE);
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pb.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    });

                    builder.setCancelable(true);
                    builder.create().show();
                }
            }
        });
    }


}