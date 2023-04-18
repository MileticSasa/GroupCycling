package com.example.groupcycling;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.groupcycling.activity.GroupShareActivity;
import com.example.groupcycling.activity.SingleRideActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class LocationService extends Service {

    public static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    public static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";

    private FusedLocationProviderClient fusedClient;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private CollectionReference ref;
    private FirebaseUser user;

    private LocationCallback locationCallback;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                    ref.document("" + user.getUid()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    updateMyLocationInGrup(geoPoint);
                                }
                            });
                }
            }
        };

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        ref = firestore.collection("Groups")
                .document(GroupShareActivity.getMyGroupName()).collection("Participants");


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("").setContentText("").build();

            startForeground(1, notification);

        }
    }

    private void startLocationService() {
        updateGps();
        getLocation();
    }

    private void stopLocationService(){
        //fusedClient.removeLocationUpdates(locationCallback);
        fusedClient.removeLocationUpdates(locationCallback);

        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("", "onStartCommand: called");

        if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals(ACTION_START_LOCATION_SERVICE)){
                    //startLocationService();
                    updateGps();
                    getLocation();
                    return START_NOT_STICKY;
                }
                else if(action.equals(ACTION_STOP_LOCATION_SERVICE)){
                    stopLocationService();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    private void getLocation() {

        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(SingleRideActivity.DEFAULT_UPDATE_INTERVAL);
        request.setFastestInterval(SingleRideActivity.FAST_UPDATE_INTERVAL);

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            stopSelf();
            return;
        }

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.myLooper());
    }

    private void updateLocation(GeoPoint geoPoint) {

        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users").document(""+user.getUid());

        try{
            reference.update("latLng", geoPoint).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                }
            });
        }
        catch (NullPointerException e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    private void updateMyLocationInGrup(GeoPoint geoPoint) {

        try{
            ref.document(""+user.getUid()).update("latlng", geoPoint)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
        }
        catch (NullPointerException e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    private void updateGps() {
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(SingleRideActivity.DEFAULT_UPDATE_INTERVAL);
        request.setFastestInterval(SingleRideActivity.FAST_UPDATE_INTERVAL);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            fusedClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        Location location = task.getResult();

                        if(location != null){
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                            updateLocation(geoPoint);
                        }
                    }
                }
            });
        }
    }
}
