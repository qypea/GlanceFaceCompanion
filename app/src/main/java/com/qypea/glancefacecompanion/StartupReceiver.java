package com.qypea.glancefacecompanion;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by q on 8/10/16.
 * Listen for startup indications, start service
 */
public class StartupReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent msg = new Intent(context, GlanceService.class);
        msg.setType("boot");
        context.startService(msg);
    }
}