package com.qypea.glancefacecompanion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by q on 7/6/16.
 * Monitors calendars, provides hooks to pull in 'next event'
 */

public class CalendarScraper {
    private static final String TAG = "CalendarScraper";
    private static final long cooldownTime = 1000 * 60 * 10; // Time to show running event
    private static final long lookaheadTime = 1000 * 60 * 60 * 16;

    public String title;
    public String location;
    public long beginTime; // When the event begins
    public String refreshReason; // Why did we last refresh?
    public long refreshTime; // What time did we last refresh?

    private final Context context;

    public CalendarScraper(Context _context) {
        Log.d(TAG, "constructed");
        title = null;
        location = "";
        beginTime = 0;
        context = _context;

        Uri uri = Uri.parse("content://com.android.calendar/calendars/");
        ContentResolver monitor = context.getContentResolver();
        monitor.registerContentObserver(uri, true, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                refresh("change");
            }
        });
    }

    public void refresh(final String reason) {
        refreshTime = new Date().getTime();
        refreshReason = reason;

        ContentResolver cr = context.getContentResolver();

        Log.d(TAG, "refresh:"
                + new SimpleDateFormat("HH:mm", Locale.US).format(refreshTime));

        // Clear out previous event info
        title = null;
        location = null;
        beginTime = 0;

        // Get next valid event
        Uri.Builder builder
                = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
        ContentUris.appendId(builder, refreshTime );
        ContentUris.appendId(builder, refreshTime + lookaheadTime);
        Cursor eventCursor = cr.query(builder.build(),
                new String[] { "event_id", "begin", "end", "allDay",
                               "selfAttendeeStatus", "hasAlarm" },
                null, null, "startDay ASC, startMinute ASC");
                // For a full list of available columns see http://tinyurl.com/yfbg76w

        // Schedule a timer long in the future for if we don't find any events this time
        scheduleUpdate(refreshTime + (lookaheadTime / 2));

        Log.d(TAG, "Event table:");
        assert eventCursor != null;
        while (eventCursor.moveToNext()) {
            String uid = eventCursor.getString(eventCursor.getColumnIndex("event_id"));
            Log.d(TAG, "event:" + uid);
            long begin = eventCursor.getLong(eventCursor.getColumnIndex("begin"));
            Log.d(TAG, " begin:" + new SimpleDateFormat("HH:mm", Locale.US).format(begin));
            long end = eventCursor.getLong(eventCursor.getColumnIndex("end"));
            Log.d(TAG, " end:" + new SimpleDateFormat("HH:mm", Locale.US).format(end));
            String allDay = eventCursor.getString(eventCursor.getColumnIndex("allDay"));
            Log.d(TAG, " allDay:" + allDay);
            int status = eventCursor.getInt(eventCursor.getColumnIndex("selfAttendeeStatus"));
            Log.d(TAG, " status:" + status);
            int alarm = eventCursor.getInt(eventCursor.getColumnIndex("hasAlarm"));
            Log.d(TAG, " alarm:" + alarm);

            // Determine if this is the one we want
            //noinspection StringEquality
            if (begin >= (refreshTime - cooldownTime) // in the future or cooldown
                && (allDay.equals("0")) // not all day
                && (status == 1 || alarm == 1))  {// has alarm or accepted

                // Get extra fields about it
                Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events/" + uid);
                Cursor c = cr.query(CALENDAR_URI,
                                    new String[] { "title", "eventLocation" },
                                    null, null, null);
                assert c != null;
                c.moveToFirst();

                if (c.isNull(c.getColumnIndex("title"))) {
                    title = "null";
                } else {
                    title = c.getString(c.getColumnIndex("title"));
                }

                if (c.isNull(c.getColumnIndex("eventLocation"))) {
                    location = "";
                } else {
                    location = c.getString(c.getColumnIndex("eventLocation"));
                }

                c.close();

                beginTime = begin;
                scheduleUpdate(beginTime + cooldownTime);

                break;
            }
        }
        eventCursor.close();
        GlanceService.singleton.refreshed();
    }

    private void scheduleUpdate(long time) {
        // Schedule alarm when event is elapsed
        Intent intent = new Intent(context, GlanceService.class);
        intent.setType("timer");

        PendingIntent sender = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Remove alarm if already present
        am.cancel(sender);

        // Set new alarm
        am.set(AlarmManager.RTC_WAKEUP, time, sender);
    }
}
