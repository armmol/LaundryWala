package com.example.laundry2.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.laundry2.R;
import com.example.laundry2.View.activity_start;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationService extends FirebaseMessagingService {
    private static final String TAG = "MyMsgService";
    NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived (@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived (remoteMessage);
        Uri notification = RingtoneManager.getDefaultUri (RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone (getApplicationContext (), notification);
        r.play ();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping (false);
        }

        // vibration
        Vibrator v = (Vibrator) getSystemService (Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 300, 300, 300};
        v.vibrate (pattern, -1);
        int resourceImage = R.drawable.user;
        NotificationCompat.Builder builder = new NotificationCompat.Builder (this, "CHANNEL_ID");
        builder.setSmallIcon (resourceImage);
        Intent intent = new Intent (this, activity_start.class);
        if (remoteMessage.getNotification ().getBody ().trim ().contains ("Customer")) {
            intent.putExtra ("authtype", "Customer").putExtra ("fromNotification", true)
                    .putExtra ("orderId", remoteMessage.getNotification ().getBody ().split ("-")[2])
                    .putExtra ("courierId", remoteMessage.getNotification ().getBody ().split ("-")[3])
                    .putExtra ("type", remoteMessage.getNotification ().getBody ().split ("-")[1]);
            notificationFunction (intent, "Your courier has arrived!", builder, remoteMessage);
        } else if (remoteMessage.getNotification ().getBody ().trim ().contains ("Laundry House")) {
            intent.putExtra ("authtype", "Laundry House").putExtra ("fromNotification", true)
                    .putExtra ("orderId", remoteMessage.getNotification ().getBody ().split ("-")[2])
                    .putExtra ("courierId", remoteMessage.getNotification ().getBody ().split ("-")[3])
                    .putExtra ("type", remoteMessage.getNotification ().getBody ().split ("-")[1]);
            notificationFunction (intent, "Your courier has arrived!", builder, remoteMessage);
        } else if (remoteMessage.getNotification ().getBody ().trim ().contains ("Update")) {
            intent.putExtra ("update", "update")
                    .putExtra ("orderId", remoteMessage.getNotification ().getBody ().split ("-")[1]);
            notificationFunction (intent, "Update on your order!", builder, remoteMessage);
        }
    }

    private void notificationFunction (Intent intent, String type, NotificationCompat.Builder builder, @NonNull RemoteMessage remoteMessage) {
        PendingIntent pendingIntent = PendingIntent.getActivity (this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle (remoteMessage.getNotification ().getTitle ());
        builder.setContentText (remoteMessage.getNotification ().getBody ());
        builder.setContentIntent (pendingIntent);
        builder.setStyle (new NotificationCompat.BigTextStyle ().bigText (remoteMessage.getNotification ().getBody ()));
        builder.setAutoCancel (true);
        builder.setPriority (Notification.PRIORITY_MAX);
        mNotificationManager = (NotificationManager) getApplicationContext ().getSystemService (Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel (
                    channelId, type, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel (channel);
            builder.setChannelId (channelId);
        }
        // notificationId is a unique int for each notification that you must define
        mNotificationManager.notify (100, builder.build ());
    }
}
