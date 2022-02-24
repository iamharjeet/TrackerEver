package com.harjeet.trackerever.Firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.R;
import com.harjeet.trackerever.RequestsActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    String title,message,type;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);
            title=remoteMessage.getData().get("Title");
            message=remoteMessage.getData().get("Message");
            type=remoteMessage.getData().get("Type");
            notification(message);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void makeNotificationChannel(String id, String name, int importance) {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setShowBadge(true); // set false to disable badges, Oreo exclusive
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    void notification(String message) {
        // make the channel. The method has been discussed before.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel("CHANNEL_1", "Example channel", NotificationManager.IMPORTANCE_DEFAULT);
        }
        // the check ensures that the channel will only be made
        // if the device is running Android 8+
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_1");
        // the second parameter is the channel id.
        // it should be the same as passed to the makeNotificationChannel() method
        if(type.equalsIgnoreCase(AppConstants.NOTI_REQUEST_TYPE)) {
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, RequestsActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(contentIntent);
        }

        notification
                .setSmallIcon(R.drawable.ic_location) // can use any other icon
                .setContentTitle(title)
                .setContentText(message)
                .setNumber(1); // this shows a number in the notification dots

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(1, notification.build());
        // it is better to not use 0 as notification id, so used 1.
    }

}
