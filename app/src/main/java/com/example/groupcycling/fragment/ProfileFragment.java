package com.example.groupcycling.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groupcycling.MySharedPreferences;
import com.example.groupcycling.R;
import com.example.groupcycling.activity.AvatarsActivity;
import com.example.groupcycling.activity.LoginActivity;
import com.example.groupcycling.activity.RegisterActivity;
import com.example.groupcycling.model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Objects;


public class ProfileFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    private static final int IMAGE_PICK_CAMERA_CODE = 102;
    private static final int IMAGE_PICK_GALLERY_CODE = 103;

    String cameraPermissions[];
    String storagePermissions[];

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    DocumentReference reference;
    CollectionReference cr;
    StorageReference storageReference;

    String storagePath = "Ride_Imgs/";
    String photoPath = "";

    TextView nameTxt, distanceTxt, speedTxt;
    ImageView logoIv;
    FloatingActionButton fab;

    Uri imageUri;

    String uid;
    String profilePhoto;
    ProgressBar pb;
    ModelUser modelUser;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        cr = firestore.collection("Users");
        reference = firestore.collection("Users").document("" + FirebaseAuth.getInstance().getUid());
        storageReference = FirebaseStorage.getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        initViews(view);

        profilePhoto = "image";

        getData();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private void getData() {
        pb.setVisibility(View.VISIBLE);

        reference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        modelUser = documentSnapshot.toObject(ModelUser.class);

                        nameTxt.setText(modelUser.getName());
                        distanceTxt.setText("" + modelUser.getDistance());
                        speedTxt.setText(""+modelUser.getTopSpeed());
                        try {
                            Picasso.get().load(modelUser.getImage()).into(logoIv);
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_image_default).into(logoIv);
                        }

                        pb.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pb.setVisibility(View.GONE);
                    }
                });
    }

    private void showEditProfileDialog() {

        String options[] = {"Edit name", "Edit image"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Chose action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        showNameEditDialog();
                        break;
                    case 1:
                        showImageEditDialog();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.create().show();
    }

    private void showImageEditDialog() {

        String options[] = {"Camera", "Gallery", "From Avatars"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (i == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                } else if (i == 2) {
                    choseAvatar();
                }
            }
        });
        builder.create().show();
    }

    private void choseAvatar() {
        Intent intent = new Intent(getActivity(), AvatarsActivity.class);
        getActivity().startActivity(intent);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return result;
    }

    private void pickFromCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return result1 && result2;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), "Camera and storage permissions required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Storage permissions required", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                if(data != null){
                    onCaptureImageResult(data);
                }
            }

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                if (data != null) {
                    imageUri = data.getData();

                    uploadImage(imageUri);
                }
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        String path = MediaStore.Images.Media.insertImage(
                getActivity().getApplicationContext().getContentResolver(), bitmap, "val", null);

        Uri uri = Uri.parse(path);
        uploadImage(uri);
    }

    private void uploadImage(Uri imageUri) {
        pb.setVisibility(View.VISIBLE);

        String filePathAndName = storagePath + "" + profilePhoto + "_" + user.getUid() + System.currentTimeMillis();

        if(imageUri != null){
            final StorageReference storageReference2nd = storageReference.child(filePathAndName);
            storageReference2nd.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;

                    if (uriTask.isSuccessful()) {

                        Uri downloadUri = uriTask.getResult();
                        //image upload
                        HashMap<String, Object> map = new HashMap<>();
                        map.put(profilePhoto, downloadUri.toString());

                        reference.update("image", downloadUri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pb.setVisibility(View.GONE);
                                Toast.makeText(requireActivity().getApplicationContext(), "Image updated", Toast.LENGTH_SHORT).show();

                                if(modelUser.getImage() != null) {
                                    deleteOldImageFromStorage();
                                }
                                getData();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pb.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Error uploading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pb.setVisibility(View.GONE);

                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteOldImageFromStorage() {
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(modelUser.getImage());
        ref.delete();
    }

    private void showNameEditDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter name");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Enter name");
        layout.addView(editText);

        builder.setView(layout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = editText.getText().toString().trim();

                if (!TextUtils.isEmpty(value)) {

                    MySharedPreferences.getInstance(getActivity().getApplicationContext()).addMyName(value);

                    reference.update("name", value)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_logout).setVisible(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_logout) {
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews(View view) {
        nameTxt = view.findViewById(R.id.nameTxt);
        logoIv = view.findViewById(R.id.logo);
        distanceTxt = view.findViewById(R.id.distanceTxt);
        speedTxt = view.findViewById(R.id.speedTxt);
        fab = view.findViewById(R.id.fab);
        pb = view.findViewById(R.id.pb);
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }
}