package com.qypea.glancefacecompanion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        refresh(intent.getType());
        return START_STICKY;
    }

    public void refresh(final String reason) {
        calendarScraper.refresh(reason);

        if (calendarScraper.title != null) {
            event = new SimpleDateFormat("HH:mm", Locale.US).format(calendarScraper.beginTime)
                    + " - " + calendarScraper.title;
            location = calendarScraper.location;

            scheduleUpdate(calendarScraper.beginTime + CalendarScraper.cooldownTime);
        } else {
            event = "No event";
            location = "";
        }

        pebbleBinding.update(event, location);
    }

    private void scheduleUpdate(long time) {
        // Schedule alarm when event is elapsed
        Intent intent = new Intent("com.qypea.GlanceFaceCompanion.UPDATE_CALENDAR");
        intent.setClassName(this, "GlanceService");
        intent.setType("timer");

        PendingIntent sender = PendingIntent.getService(this, 0, intent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Remove alarm if already present
        am.cancel(sender);

        // Set new alarm
        am.set(AlarmManager.RTC_WAKEUP, time, sender);
    }

    // Don't like it, but I have to include this
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
