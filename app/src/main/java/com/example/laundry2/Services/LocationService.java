package com.example.laundry2.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.laundry2.R;
import com.example.laundry2.View.activity_maps;

public class LocationService extends Service {
    private static final String TAG = "Location Service";
    private static final String channelId = "location notification channel";

    private Notification createNotification () {
        Intent resultIntent = new Intent (this, activity_maps.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity (
                getApplicationContext (), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder (getApplicationContext (), channelId);

        builder.setSmallIcon (R.mipmap.ic_launcher).
                setContentTitle ("Location Service").
                setDefaults (NotificationCompat.DEFAULT_ALL).
                setContentText ("Running").
                setContentIntent (pendingIntent)
                .setAutoCancel (false)
                .setOnlyAlertOnce (true)
                .setOngoing (true);
        return builder.build ();
    }

    @SuppressLint("MissingPermission")
    private void startChannel () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel (
                    channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription ("Channel being used to track Courier");
            NotificationManagerCompat.from (getApplicationContext ()).createNotificationChannel (notificationChannel);
        }
        startForeground (R.integer.LOCATION_SERVICE_ID, createNotification ());
    }

    @Nullable
    @Override
    public IBinder onBind (Intent intent) {
        throw new UnsupportedOperationException ("Location Service not Implemented");
    }


    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand (intent, flags, startId);
        startChannel ();
        return super.onStartCommand (intent, flags, startId);
    }
}
