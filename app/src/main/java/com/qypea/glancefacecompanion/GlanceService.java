package com.qypea.glancefacecompanion;

import android.app.Service;
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

        refresh("create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (!intent.getType().equals("init")) {
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
    }

    // Don't like it, but I have to include this
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
