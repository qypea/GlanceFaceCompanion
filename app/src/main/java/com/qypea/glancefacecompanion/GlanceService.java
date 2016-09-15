package com.qypea.glancefacecompanion;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by q on 8/7/16.
 * Coordinates background behaviors for GlanceFaceCompanion
 */
public class GlanceService extends Service {
    private final static String TAG = "GlanceService";

    public static GlanceService singleton;
    public CalendarScraper calendarScraper;
    private PebbleBinding pebbleBinding;

    private final static int notificationID = 101;

    public String event;
    public String location;

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        singleton = null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        if (singleton == null) {
            singleton = this;
        } else {
            throw new RuntimeException("Cant run multiples of GlanceService");
        }

        calendarScraper = new CalendarScraper(this);
        pebbleBinding = new PebbleBinding(this);
        event = "No event";
        location = "";

        refresh("create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        String t;
        if (intent == null) {
            t = null;
        } else {
            t = intent.getType();
        }
        if (t != null && !t.equals("init")) {
            refresh(intent.getType());
        }
        return START_STICKY;
    }

    public void refresh(final String reason) {
        calendarScraper.refresh(reason);
    }

    public void refreshed() {
        if (calendarScraper.title != null) {
            event = new SimpleDateFormat("HH:mm", Locale.US).format(calendarScraper.beginTime)
                    + " - " + calendarScraper.title;
            location = calendarScraper.location;
        } else {
            event = "No event";
            location = "";
        }

        if (location.startsWith("CR - ")) {
            location = location.substring(5);
        }

        pebbleBinding.update(event, location);

        if (MainActivity.singleton != null) {
            MainActivity.singleton.redraw();
        }

        notificationUpdate();
    }

    private void notificationUpdate() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_watch_white_24dp)
                        .setContentTitle("GlanceFace")
                        .setContentText(event + '\n' + location)
                        .setOngoing(true)
                        .setContentIntent(resultPendingIntent)
                        .setWhen(new Date().getTime())
                        .setPriority(Notification.PRIORITY_LOW);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationID, mBuilder.build());
    }

    // Don't like it, but I have to include this
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
