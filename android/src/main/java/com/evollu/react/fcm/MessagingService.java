package com.evollu.react.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Remote message received");

        Intent intent = new Intent("com.evollu.react.fcm.ReceiveNotification");

        intent.putExtra("data", remoteMessage);

        String imageUri = remoteMessage.getData().get("bigImage");

        if (imageUri != null) {
            Bitmap image = getBitmapfromUri(imageUri);
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");

            intent.putExtra("image", true);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Notification.Style style = new Notification.BigPictureStyle().bigPicture(image);

            Notification.Builder notificationBuilder = new Notification.Builder(this)
                    .setSmallIcon(getSmallIcon())
                    .setColor(getColor())
                    .setLargeIcon(getLargeIcon())
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(style)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        } else {
            sendOrderedBroadcast(intent, null);
        }
    }

    public Bitmap getBitmapfromUri(String imageUri) {
        try {
            URL url = new URL(imageUri);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();

            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public int getColor() {
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);

            return applicationInfo.metaData.getInt("com.google.firebase.messaging.default_notification_color", 0);
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    public int getSmallIcon() {
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);

            return applicationInfo.metaData.getInt("com.google.firebase.messaging.default_notification_icon", 0);
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    public Bitmap getLargeIcon() {
        return ((BitmapDrawable) getPackageManager().getApplicationIcon(getApplicationInfo())).getBitmap();
    }
}
