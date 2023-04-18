package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.groupcycling.R;
import com.example.groupcycling.adapter.AvatarAdapter;
import com.example.groupcycling.model.ModelAvatar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class AvatarsActivity extends AppCompatActivity implements AvatarAdapter.onAvatarClickListener {

    private RecyclerView recyclerView;
    private ArrayList<ModelAvatar> avatars;
    private AvatarAdapter adapter;

    String storagePath = "Ride_Imgs/";
    String profilePhoto;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    DocumentReference reference;
    CollectionReference cr;
    StorageReference storageReference;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatars);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));

        addAvatars();

        recyclerView = findViewById(R.id.recView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new AvatarAdapter(this, avatars, this::onAvatarClick);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        cr = firestore.collection("Users");
        reference = firestore.collection("Users").document(""+user.getUid());
        storageReference = FirebaseStorage.getInstance().getReference();

        progressBar = findViewById(R.id.pb);
    }

    private void addAvatars() {
        avatars = new ArrayList<>();

        avatars.add(new ModelAvatar(R.drawable.kuce));
        avatars.add(new ModelAvatar(R.drawable.leptir));
        avatars.add(new ModelAvatar(R.drawable.majmun));
        avatars.add(new ModelAvatar(R.drawable.maslacak_beo));
        avatars.add(new ModelAvatar(R.drawable.maslacak_zut));
        avatars.add(new ModelAvatar(R.drawable.orao));
        avatars.add(new ModelAvatar(R.drawable.puska));
        avatars.add(new ModelAvatar(R.drawable.liberetz));
    }

    @Override
    public void onAvatarClick(int position) {
        ModelAvatar avatar = avatars.get(position);
        int img = avatar.getImage();

        Bitmap bitmap = ((BitmapDrawable) this.getResources().getDrawable(img)).getBitmap();

        String path = MediaStore.Images.Media.insertImage(this.getApplicationContext().getContentResolver(), bitmap, "val", null);

        Uri uri = Uri.parse(path);
        uploadImage(uri);
    }

    private void uploadImage(Uri imageUri) {

        progressBar.setVisibility(View.VISIBLE);

        String filePathAndName = storagePath + "" + profilePhoto + "_" + user.getUid() + System.currentTimeMillis();

        final StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());

                if(uriTask.isSuccessful()){

                    Uri downloadUri = uriTask.getResult();
                    //image upload
                    HashMap<String, Object> map = new HashMap<>();
                    map.put(profilePhoto, downloadUri.toString());

                    reference.update("image", downloadUri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AvatarsActivity.this, "Image updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AvatarsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AvatarsActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(AvatarsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}