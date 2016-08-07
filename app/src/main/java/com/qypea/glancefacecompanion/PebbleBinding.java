package com.qypea.glancefacecompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/**
 * Created by q on 8/6/16.
 * Handle the connection to my pebble app, send events to it
 */
public class PebbleBinding extends BroadcastReceiver {
    private final static int AppKeyEvent = 1;
    private final static int AppKeyLocation = 2;
    private final static UUID appUuid = UUID.fromString("c70a54ab-dffb-473d-bfa0-2575ff43a9e9");

    private String currentEvent = null;
    private String currentLocation = null;

    public PebbleBinding(Context context) {
        // Register for watch started
        PebbleKit.registerPebbleConnectedReceiver(context, this);

        // Start the app if not yet running
        PebbleKit.startAppOnPebble(context, appUuid);
    }

    public void update(Context context, final String event, final String location) {
        // Update the currently displayed event
        currentEvent = event;
        currentLocation = location;

        sync(context);
    }

    private void sync(Context context) {
        // Sync the current state to the watch face
        // Called on update or when watch face connects

        if (PebbleKit.isWatchConnected(context)) {
            // Send data to it
            PebbleDictionary dict = new PebbleDictionary();
            if (currentEvent != null) {
                dict.addString(AppKeyEvent, currentEvent);
            } else {
                dict.addString(AppKeyEvent, "None");
            }
            if (currentLocation != null) {
                dict.addString(AppKeyLocation, currentLocation);
            } else {
                dict.addString(AppKeyLocation, "");
            }

            PebbleKit.sendDataToPebble(context, appUuid, dict);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        sync(context);
    }
}