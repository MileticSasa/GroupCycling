package com.example.groupcycling.NotificationPack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.groupcycling.R;
import com.example.groupcycling.activity.GroupActivity;
import com.example.groupcycling.activity.GroupChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String content, title;
    private NotificationChannel channel;
    private String channelId = "my_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        title = message.getData().get("title");
        content = message.getData().get("body");

        Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String name = "Kanal";
            String description = "Opis";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            manager.createNotificationChannel(channel);

            int notificationId = new Random().nextInt();

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setContentText(content)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();

            manager.notify(notificationId, notification);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}
