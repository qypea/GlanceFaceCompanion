package com.qypea.glancefacecompanion;

import android.content.Intent;
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
    private static final String TAG = "GlanceFaceMain";

    public static MainActivity singleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Kick off the service
        Intent intent = new Intent(this, GlanceService.class);
        intent.setType("init");
        startService(intent);
    }

    @Override
    protected void onStop() {
        singleton = null;
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        singleton = this;

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
            GlanceService.singleton.refresh("user");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void redraw() {
        GlanceService glanceService = GlanceService.singleton;

        // Draw current value
        TextView text = (TextView) findViewById(R.id.textView);
        if (text == null) {
            Log.e(TAG, "Unable to get text view to draw");
        } else {
            String value;
            if (glanceService == null) {
                value = "Service not yet running";
            } else {
                value = glanceService.event + '\n'
                        + glanceService.location + "\n\n"
                        + "Refresh trigger:" + glanceService.calendarScraper.refreshReason + "@"
                        + new SimpleDateFormat("HH:mm", Locale.US).format(
                            glanceService.calendarScraper.refreshTime);
            }
            text.setText(value);
        }
    }
}
