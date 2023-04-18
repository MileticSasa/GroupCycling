package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.groupcycling.Constants;
import com.example.groupcycling.MySharedPreferences;
import com.example.groupcycling.NotificationPack.Data;
import com.example.groupcycling.NotificationPack.NotificationSender;
import com.example.groupcycling.NotificationPack.retrofit.APIService;
import com.example.groupcycling.NotificationPack.retrofit.MyResponse;
import com.example.groupcycling.NotificationPack.retrofit.RetrofitClient;
import com.example.groupcycling.R;
import com.example.groupcycling.adapter.GroupChatAdapter;
import com.example.groupcycling.model.ModelGroupMessage;
import com.example.groupcycling.model.ModelToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 1000;
    private static final int IMAGE_PICK_GALLERY_CODE = 2000;

    private String[] cameraPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private String[] storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private FirebaseAuth aut;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private String storage_paht = "Ride_Imgs/";
    private CollectionReference collectionRef;

    private RecyclerView recyclerView;
    private ImageButton attachBtn, sendBtn;
    private EditText etMessage;
    private ProgressBar pb;

    private static String myGroupName;
    String myName, myId;
    private Uri imageUri = null;

    private APIService service;
    private String refreshToken = "";
    private String myToken = "";
    private ArrayList<String> tokens = new ArrayList<>();

    private GroupChatAdapter groupChatAdapter;
    private ArrayList<ModelGroupMessage> messages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        service = RetrofitClient.getClient(Constants.URL).create(APIService.class);

        Intent intent = getIntent();
        myGroupName = intent.getStringExtra("name");

        myName = MySharedPreferences.getInstance(this).getMyName();
        myId = MySharedPreferences.getInstance(this).getMyUid();

        ActionBar bar = getSupportActionBar();
        bar.setTitle(myGroupName);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));

        aut = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        collectionRef = firestore.collection("Groups").document(""+myGroupName)
                .collection("Chat");
        storageRef = FirebaseStorage.getInstance().getReference();

        myToken = MySharedPreferences.getInstance(this).getToken();

        initViews();

        getTokens();

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageImportDialog();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etMessage.getText().toString().trim();

                if(TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatActivity.this, "Can't send an empty message", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendMessage(message);

                    for(String toke : tokens){
                        if(!Objects.equals(toke, myToken)){
                            sendNotification(toke, myName, "Sent a message!");
                        }
                    }

                    updateToken();
                }
            }
        });
    }

    private void getTokens() {
        firestore.collection("Groups").document(""+myGroupName).collection("Tokens")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        tokens.clear();
                        for (DocumentSnapshot ds : queryDocumentSnapshots){
                            String stina = Objects.requireNonNull(ds.toObject(ModelToken.class)).getToken();
                            tokens.add(stina);
                        }
                    }
                });
    }

    private void showImageImportDialog() {
        String[] options = {"Camera", "Galery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            if(!checkCameraPermission()){
                                requestCameraPermission();
                            }
                            else{
                                pickFromCamera();
                            }
                        }
                        else{
                            if(!checkStoragePermission()){
                                requestStoragePermission();
                            }
                            else{
                                pickFromGallery();
                            }
                        }
                    }
                }).show();
    }


    private void pickFromGallery() {
        Intent galeryIntent = new Intent(Intent.ACTION_PICK);
        galeryIntent.setType("image/*");
        startActivityForResult(galeryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        String path = MediaStore.Images.Media.insertImage(
                this.getApplicationContext().getContentResolver(), bitmap, "val", null);

        Uri uri = Uri.parse(path);
        sendImageMessage(uri);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private boolean checkCameraPermission() {
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        return result1 && result2;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted && cameraAccepted){
                        pickFromCamera();
                    }
                    else{
                        Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case IMAGE_PICK_CAMERA_CODE:
                    if(data != null){
                        onCaptureImageResult(data);
                    }
                    break;
                case IMAGE_PICK_GALLERY_CODE:
                    if(data != null) {
                        sendImageMessage(data.getData());
                    }
                    break;
            }
        }
    }

    private void sendImageMessage(Uri imageUri) {
        pb.setVisibility(View.VISIBLE);

        String filePath = storage_paht + "_" + "" + aut.getUid() + System.currentTimeMillis();

        final StorageReference ref = storageRef.child(filePath);

        if(imageUri != null) {
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!p_uriTask.isSuccessful()) ;
                            Uri p_downloadUri = p_uriTask.getResult();

                            if (p_uriTask.isSuccessful()) {
                                String timeStamp = "" + System.currentTimeMillis();

                                ModelGroupMessage message =
                                        new ModelGroupMessage(p_downloadUri.toString(), myName, myId, timeStamp, "image");

                                collectionRef.document(timeStamp).set(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                etMessage.setText("");
                                                pb.setVisibility(View.GONE);
                                                Toast.makeText(GroupChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pb.setVisibility(View.GONE);
                                                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void sendMessage(String message) {
        String timestamp = "" + System.currentTimeMillis();
        String senderName = myName;
        String senderId = myId;

        ModelGroupMessage groupMessage =
                new ModelGroupMessage(message, senderName, senderId, timestamp, "text");

        //sending message
        collectionRef.document(timestamp).set(groupMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        etMessage.setText("");
                        Toast.makeText(GroupChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendNotification(String token, String senderName, String message) {

        Data data = new Data(senderName, message);

        NotificationSender notificationSender = new NotificationSender(data, token);
        service.sendNotification(notificationSender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if(response.isSuccessful()){
                    Log.d("ONDE", "onResponse: "+ "TTTTTTTOOOOOOOOOO");
                }
                else{
                    Log.d("OVDE", "onResponse: "+ "ne valja");
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Log.d("OVDE", "onFailure: "+t.getLocalizedMessage());
            }
        });
    }

    private void updateToken() {
        String myUid = aut.getCurrentUser().getUid();

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        refreshToken = s;

                        if(refreshToken.length() > 0){
                            ModelToken modelToken = new ModelToken(refreshToken);
                            FirebaseFirestore.getInstance().collection("Groups").document(""+myGroupName)
                                    .collection("Tokens").document(""+myUid).set(modelToken);

                            MySharedPreferences.getInstance(GroupChatActivity.this).addToken(refreshToken);
                        }
                    }
                });
    }


    private void loadGroupMessages() {

        firestore.collection("Groups").document(""+myGroupName)
                .collection("Chat")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Toast.makeText(GroupChatActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(value != null){
                            messages.clear();

                            for(QueryDocumentSnapshot doc : value){
                                ModelGroupMessage groupMessage = doc.toObject(ModelGroupMessage.class);
                                String myId = aut.getUid();
                                messages.add(groupMessage);
                            }

                            groupChatAdapter.setGroupMessages(messages);
                            recyclerView.smoothScrollToPosition(groupChatAdapter.getLastPosition());
                        }
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();

        initViews();

        myToken = MySharedPreferences.getInstance(this).getToken();
        loadGroupMessages();
        getTokens();
    }


    private void initViews() {

        recyclerView = findViewById(R.id.poruke_view);
        attachBtn = findViewById(R.id.attachBtn);
        sendBtn = findViewById(R.id.sendBtn);
        etMessage = findViewById(R.id.message_et);
        pb = findViewById(R.id.pb);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        groupChatAdapter = new GroupChatAdapter(this, null);
        recyclerView.setAdapter(groupChatAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}