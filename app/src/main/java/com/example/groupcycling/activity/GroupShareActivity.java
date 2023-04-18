package com.example.groupcycling.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groupcycling.LocationService;
import com.example.groupcycling.R;
import com.example.groupcycling.marker.ClusterManagerRenderer;
import com.example.groupcycling.marker.ClusterMarker;
import com.example.groupcycling.model.ModelParticipant;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

public class GroupShareActivity extends AppCompatActivity implements OnMapReadyCallback, ClusterManager.OnClusterItemClickListener {

    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final int LOCATION_UPDATE_INTERVAL = 2000;

    private FirebaseAuth aut;
    private FirebaseFirestore firestore;
    private CollectionReference ref;

    private FusedLocationProviderClient fusedClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private ArrayList<ModelParticipant> participants = new ArrayList<>();

    private ClusterManager clusterManager;
    private ClusterManagerRenderer clusterManagerRenderer;
    private ArrayList<ClusterMarker> clusterMarkers = new ArrayList<>();

    private Runnable runnable;
    private Handler handler = new Handler();

    private Button startBtn, stopBtn;
    private TextView textSpeed;
    private ImageView imageView;
    private MapView mapView;

    private static String myGroupName;
    private ModelParticipant currentParticipant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_share);

        Intent intent = getIntent();
        myGroupName = intent.getStringExtra("name");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(myGroupName);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_color)));

        aut = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        ref = firestore.collection("Groups").document(""+myGroupName)
                .collection("Participants");

        getCurrentParticipant();
        initViews();
        initMap(savedInstanceState);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationService();
                getUsersList();
                startBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.VISIBLE);

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationService();
                stopBtn.setVisibility(View.GONE);
                startBtn.setVisibility(View.VISIBLE);
            }
        });
    }


    private void getCurrentParticipant() {
        ref.document(aut.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentParticipant = documentSnapshot.toObject(ModelParticipant.class);
            }
        });
    }


    private void getUsersList(){

        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                participants.clear();
                for(DocumentSnapshot doc : queryDocumentSnapshots){
                    ModelParticipant participant = doc.toObject(ModelParticipant.class);
                    participants.add(participant);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupShareActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startLocationService(){

        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.setAction(LocationService.ACTION_START_LOCATION_SERVICE);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                GroupShareActivity.this.startForegroundService(serviceIntent);
            }
            else{
                startService(serviceIntent);
            }
        }
    }

    private void stopLocationService(){

        if(isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.setAction(LocationService.ACTION_STOP_LOCATION_SERVICE);


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                GroupShareActivity.this.startForegroundService(serviceIntent);
            }
            else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning(){

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.groupride.LocationService".equals(serviceInfo.service.getClassName())){
                if(serviceInfo.foreground){
                    return true;
                }
            }
        }

        Log.d("TAG", "isLocationServiceRunning: not running");
        return false;
    }

    private void initViews() {
        startBtn = findViewById(R.id.btn);
        stopBtn = findViewById(R.id.btn2);
        stopBtn.setVisibility(View.GONE);
        textSpeed = findViewById(R.id.tvSpeed);
        mapView = findViewById(R.id.map);
        imageView = findViewById(R.id.zoom);
    }

    public static String getMyGroupName(){
        return myGroupName;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    private ArrayList<ModelParticipant> getParticipants(){
        ArrayList<ModelParticipant> list = new ArrayList<>();

        CollectionReference reference = firestore.collection("Groups")
                .document(""+myGroupName).collection("Participants");

        reference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                list.clear();
                for(DocumentSnapshot doc : queryDocumentSnapshots){
                    ModelParticipant participant = doc.toObject(ModelParticipant.class);
                    list.add(participant);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupShareActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        return list;
    }

    private void initMap(Bundle savedInstanceState){
        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap map){

        //setting camera zoom on my starting position
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentParticipant != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                    currentParticipant.getLatlng().getLatitude(), currentParticipant.getLatlng().getLongitude()),
                            15.0f));
                }
            }
        });

        ref.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    return;
                }

                if(value != null){
                    participants.clear();
                    for(DocumentChange dc : value.getDocumentChanges()){
                        DocumentSnapshot doc = dc.getDocument();
                        ModelParticipant participant = doc.toObject(ModelParticipant.class);
                        participants.add(participant);
                    }
                }
                addMarkers(map, participants);
            }

        });
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        getCurrentParticipant();

        mapView.onResume();

        startGettingParticipantLocationRunnable();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        startGettingParticipantLocationRunnable();
    }


    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void addMarkers(GoogleMap googleMap, ArrayList<ModelParticipant> participants){
        if(googleMap != null){
            if(clusterManager == null){
                clusterManager = new ClusterManager<ClusterMarker>(this.getApplicationContext(), googleMap);
            }

            if(clusterManagerRenderer == null){
                clusterManagerRenderer = new ClusterManagerRenderer(this, googleMap, clusterManager);
                clusterManager.setRenderer(clusterManagerRenderer);
            }

            for(ModelParticipant modelParticipant : participants){
                try{
                    String snippet = "";
                    if(modelParticipant.getUid().equals(""+aut.getCurrentUser().getUid())){
                        snippet = "This is me!";
                    }
                    else{
                        snippet = "Participant "+modelParticipant.getName();
                    }
                    double lat = modelParticipant.getLatlng().getLatitude();
                    double lon = modelParticipant.getLatlng().getLongitude();
                    double speed = modelParticipant.getSpeed();
                    String sve = modelParticipant.getName() + ", " + "Speed: " + speed;
                    ClusterMarker marker = new ClusterMarker(new LatLng(lat, lon), sve,
                            snippet, modelParticipant.getImage(), modelParticipant);

                    clusterManager.addItem(marker);
                    clusterMarkers.add(marker);
                    googleMap.setOnMarkerClickListener(clusterManager);
                }
                catch (NullPointerException e){
                    Log.d("GroupShareActivty", "addMarkers: NullPointerException" + e.getMessage());
                }
            }

            clusterManager.cluster();
        }
    }

    private void startGettingParticipantLocationRunnable(){
        Log.d("GroupShareActivity", "startGettingUserLocationRunnable: starting runnable for retrieving locations");

        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                retrieveParticipantLocation();
                handler.postDelayed(runnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        handler.removeCallbacks(runnable);
    }

    private void retrieveParticipantLocation(){
        Log.d("GroupShareActivity", "retrieveParticipantLocation: retrieving participants location");

        try{
            for(ClusterMarker marker : clusterMarkers){

                DocumentReference participantRef = ref.document(marker.getParticipant().getUid());

                participantRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            final ModelParticipant participant = task.getResult().toObject(ModelParticipant.class);

                            //update location of marker
                            try{
                                LatLng updatedLatLng = new LatLng(
                                        participant.getLatlng().getLatitude(),
                                        participant.getLatlng().getLongitude()
                                );
                                for(int i = 0; i < clusterMarkers.size(); i++){
                                    if(marker.getParticipant().getUid() == clusterMarkers.get(i).getParticipant().getUid()){
                                        clusterMarkers.get(i).setPosition(updatedLatLng);
                                        clusterManagerRenderer.setUpdatedMarker(clusterMarkers.get(i));
                                    }
                                }
                            }
                            catch (NullPointerException e){
                                Toast.makeText(GroupShareActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }
        catch (IllegalStateException e){
            Log.d("GroupShareActivity", "retrieveParticipantLocation: "+e.getMessage());
        }
    }

    @Override
    public boolean onClusterItemClick(ClusterItem item) {
        return false;
    }
}