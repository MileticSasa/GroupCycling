package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelUser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SingleRideActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 5000;
    public static final int FAST_UPDATE_INTERVAL = 1000;
    public static final int PERMISSIONS_FINE_LOCATION = 99;

    private static final String REF_NAME = "Users";

    private TextView speedTv, topSpeedTv, distanceTv;
    private Button startBtn, finishBtn, showMapBtn, saveBtn;
    private Chronometer chronometer;

    private Location currentLocation;
    private static List<Location> savedLocations;

    private FusedLocationProviderClient fusedClient;
    //private LocationRequest locationRequest;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private String latitude, longitude;
    private List<Float> topSpeeds;
    private boolean running;
    private float topSpeed;
    float distance = 0;

    String currentDistance;
    Double currDistance, oldDistance;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private CollectionReference collectionReference;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_ride);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection(REF_NAME);
        user = auth.getCurrentUser();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My ride");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));

        savedLocations = new ArrayList<>();
        topSpeeds = new ArrayList<>();

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FAST_UPDATE_INTERVAL);

        initViews();
        disableButtons();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        if(locationResult != null){
                            Location myLocation = locationResult.getLastLocation();
                            updatePosition(myLocation);
                            startChronometer();
                            finishBtn.setEnabled(true);
                            startBtn.setEnabled(false);
                        }
                    }
                };

                startLocationUpdating();
                saveBtn.setEnabled(false);
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdating();
                stopChronometer();
                saveBtn.setEnabled(true);
                finishBtn.setEnabled(false);
                startBtn.setEnabled(true);
                try {
                    currDistance = Double.parseDouble(parsiraj(distanceTv.getText().toString()));
                }catch (Exception e){
                    Log.d("TAG", "onClick: "+e.getMessage());
                    currDistance = 0.00;
                    Toast.makeText(SingleRideActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleRideActivity.this, MapsActivity.class);
                intent.putExtra("Value", 10);
                startActivity(intent);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currDistance += oldDistance;

                collectionReference.document(""+user.getUid()).update("distance", currDistance)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SingleRideActivity.this, "Values updated", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SingleRideActivity.this, "Ne valja", Toast.LENGTH_SHORT).show();
                            }
                        });

                resetUiValues();
                saveBtn.setEnabled(false);
            }
        });

        updateGps();
    }

    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.document(""+user.getUid())
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.d(String.valueOf(SingleRideActivity.this), "onEvent: "+error.toString());
                            return;
                        }

                        if(value.exists()){
                            ModelUser user = value.toObject(ModelUser.class);
                            oldDistance = user.getDistance();
                        }
                    }
                });
    }

    private void updateGps() {

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            fusedClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        Location location = task.getResult();
                        if(location != null){
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                            updateLocation(latLng, geoPoint);
                        }
                    }
                }
            });
        }
        else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateLocation(LatLng latLng, GeoPoint geoPoint) {
        if(null != geoPoint){
            collectionReference.document(""+user.getUid()).update("latLng", geoPoint)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SingleRideActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void resetUiValues() {
        speedTv.setText("0.0");
        topSpeedTv.setText("0.0");
        distanceTv.setText("0.0");
        resetChronometer();
    }

    private void updatePosition(Location myLocation) {
        savedLocations.add(myLocation);

        if(savedLocations.size() > 0){
            distanceCount(savedLocations);
        }

        updateUiValues(myLocation);
    }

    private void updateUiValues(Location myLocation) {
        latitude = String.valueOf(myLocation.getLatitude());
        longitude = String.valueOf(myLocation.getLongitude());

        DecimalFormat df = new DecimalFormat("0.00");

        if(myLocation.hasSpeed()){
            float speed = (float)((myLocation.getSpeed() * 3600) / 1000);
            topSpeeds.add(speed);

            String s = df.format(speed);
            speedTv.setText(s);

            if(topSpeeds.size()>0){
                topSpeed = getTopSpeed(speed);
            }

            String ts = df.format(topSpeed);
            topSpeedTv.setText(ts);
        }
    }

    String parsiraj(String s){

        String sa = "";
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == ',')
                sa = s.substring(0, i)+'.'+s.substring(i+1);
        }

        return sa;
    }

    private float getTopSpeed(float speed) {
        float top = topSpeeds.get(0);

        if(topSpeeds.size() > 1){
            for(int i = 1; i < topSpeeds.size(); i++){
                if(top < topSpeeds.get(i)){
                    top = topSpeeds.get(i);
                }
            }
        }

        if(topSpeeds.size() > 100){
            topSpeeds.clear();
            topSpeeds.add(top);
        }

        return top;
    }

    private void distanceCount(List<Location> savedLocations) {
        Location origin;
        Location destination;
        Location jedina;

        origin = savedLocations.get(0);
        for(int i = 1; i < savedLocations.size(); i++){
            destination = savedLocations.get(i);
            distance += origin.distanceTo(destination);
            if(origin != savedLocations.get(savedLocations.size() - 1)){
                jedina = origin;
                origin = destination;
                savedLocations.remove(jedina);
            }
        }

        float distanceInKm = distance / 1000;
        DecimalFormat df = new DecimalFormat("0.00");
        String s = df.format(distanceInKm);

        distanceTv.setText(df.format(distanceInKm));
    }

    private void startChronometer() {
        if(!running){
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
        }
    }

    private void stopChronometer(){
        if(running){
            chronometer.stop();
            running = false;
        }
    }

    private void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    private void startLocationUpdating() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            return;
        }

        fusedClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGps();
    }

    private void stopLocationUpdating(){
        fusedClient.removeLocationUpdates(locationCallback);
    }

    private void disableButtons() {
        finishBtn.setEnabled(false);
        saveBtn.setEnabled(false);
    }

    public static List<Location> getSavedLocations(){
        return savedLocations;
    }

    private void initViews() {
        speedTv = findViewById(R.id.speedTv);
        topSpeedTv = findViewById(R.id.topSpeedTv);
        distanceTv = findViewById(R.id.distanceTv);
        chronometer = findViewById(R.id.chronometer);
        startBtn = findViewById(R.id.startBtn);
        finishBtn = findViewById(R.id.finishBtn);
        showMapBtn = findViewById(R.id.showMap);
        saveBtn = findViewById(R.id.saveBtn);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}