package com.qypea.glancefacecompanion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private CalendarScraper calendarScraper;
    private PebbleBinding pebbleBinding;

    private static final String TAG = "GlanceFace Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        calendarScraper = new CalendarScraper();
        pebbleBinding = new PebbleBinding(getApplicationContext());
        refresh("init");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update the state
        redraw();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refresh("user");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh(final String reason) {
        // Refresh the calendar state
        calendarScraper.refresh(getApplicationContext(), reason);

        // Update our view
        redraw();
    }

    private void redraw() {
        // Draw current value
        TextView text = (TextView) findViewById(R.id.textView);
        if (text == null) {
            Log.e(TAG, "Unable to get text view to draw");
        } else {
            String value;
            if (calendarScraper.title != null) {
                value = new SimpleDateFormat("HH:mm", Locale.US).format(calendarScraper.beginTime);
                value += " - " + calendarScraper.title + '\n';
                value += calendarScraper.location;
            } else {
                value = "No event";
            }
            value += "\n\nRefresh trigger:" + calendarScraper.refreshReason + "@"
                + new SimpleDateFormat("HH:mm", Locale.US).format(calendarScraper.refreshTime);
            text.setText(value);
        }

        // TODO: Make calendarScraper signal to UI, pebbleBinding when it refreshes itself too
        pebbleBinding.update(getApplicationContext(),
                new SimpleDateFormat("HH:mm", Locale.US).format(calendarScraper.beginTime)
                        + " - " + calendarScraper.title,
                calendarScraper.location);
    }
}