package com.cikoapps.deezeralarm.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.HelperClasses.MyLocation;
import com.cikoapps.deezeralarm.HelperClasses.Quotes;
import com.cikoapps.deezeralarm.HelperClasses.WeatherDataAsync;
import com.cikoapps.deezeralarm.R;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class QuoteActivity extends Activity {
    private static final String TAG = "QuoteActivity";
    private RelativeLayout mainTopLayout;
    private Context context;
    private ImageButton refreshButton;
    private MyLocation myLocation;
    private WeatherDataAsync weatherDataAsync;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.quote_activity_layout);
        mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);
        TextView quoteTextView = (TextView) findViewById(R.id.quoteTextView);
        TextView authorTextView = (TextView) findViewById(R.id.authorTextView);
        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        Typeface robotoItalic = Typeface.createFromAsset(getAssets(), "Roboto-Italic.ttf");
        Quotes quote = Quotes.getQuote();
        quoteTextView.setText(quote.quote);
        authorTextView.setText(quote.author);
        quoteTextView.setTypeface(robotoItalic);
        authorTextView.setTypeface(robotoRegular);
        this.context = this;
        refreshButton = (ImageButton) mainTopLayout.findViewById(R.id.refreshButton);
        refreshWeatherButton();
        weatherDataAsync = new WeatherDataAsync(mainTopLayout, -1, -1, null, context);
        weatherDataAsync.setFromSharedPreferences();
        updateWeatherData();
        updateDisplay();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
        System.exit(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
    }

    private void updateWeatherData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int refreshTime = preferences.getInt(SettingsActivity.SELECTED_INTERVAL, 1);
        if (refreshTime != 12) {
            long lastUpdateTimeMillis = preferences.getLong(WeatherDataAsync.TIME_UPDATED, 0);
            boolean onlyWiFiUpdate = preferences.getBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);
            if ((onlyWiFiUpdate && new HelperClass(context).isWifiConnected()) || !onlyWiFiUpdate) {
                Calendar deleteCalendar = Calendar.getInstance();
                deleteCalendar.setTimeInMillis(lastUpdateTimeMillis);
                int milliSecondsRefreshTime = (refreshTime + 1) * 5 * 60000;
                Calendar calendar = Calendar.getInstance();
                long currentMillis = calendar.getTimeInMillis();
                if ((currentMillis - lastUpdateTimeMillis) > milliSecondsRefreshTime) {
                    myLocation = new MyLocation(this, mainTopLayout, null);
                    myLocation.buildGoogleApiClient();
                }
            }
        }
    }

    private void updateDisplay() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weatherDataAsync.setFromSharedPreferences();
                        updateWeatherData();
                    }
                });


            }
        }, 0, 1 * 60 * 1000);
    }

    private void refreshWeatherButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new HelperClass(context).haveNetworkConnection()) {
                    if (myLocation != null) {
                        myLocation.reconnectGoogleApiClient();
                    } else {
                        myLocation = new MyLocation(context, mainTopLayout, null);
                        myLocation.buildGoogleApiClient();
                    }
                } else {
                    Toast.makeText(context, "No network connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
