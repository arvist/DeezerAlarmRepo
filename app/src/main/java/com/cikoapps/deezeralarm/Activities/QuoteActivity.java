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

public class QuoteActivity extends Activity {
    private static final String TAG = "QuoteActivity";
    private RelativeLayout mainTopLayout;
    private Typeface robotoItalic;
    private Typeface robotoRegular;
    private Context context;
    private ImageButton refreshButton;
    private WeatherDataAsync weatherDataAsync;
    private MyLocation myLocation;
    private TextView quoteTextView;
    private TextView authorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.quote_activity_layout);
        mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);
        quoteTextView = (TextView) findViewById(R.id.quoteTextView);
        authorTextView = (TextView) findViewById(R.id.authorTextView);
        robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        robotoItalic = Typeface.createFromAsset(getAssets(), "Roboto-Italic.ttf");
        Quotes quote = Quotes.getQuote();
        quoteTextView.setText(quote.quote);
        authorTextView.setText(quote.author);
        quoteTextView.setTypeface(robotoItalic);
        authorTextView.setTypeface(robotoRegular);
        this.context = this;
        refreshButton = (ImageButton) mainTopLayout.findViewById(R.id.refreshButton);
        refresh();
        weatherDataAsync = new WeatherDataAsync(mainTopLayout, -1, -1, null, context);
        weatherDataAsync.setFromSharedPreferences();
        updateWeatherData();
    }

    private void updateWeatherData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int refreshTime = preferences.getInt("selectedInterval", 1);
        if (refreshTime != 12) {
            long lastUpdateTimeMillis = preferences.getLong("time", 0);
            boolean onlyWiFiUpdate = preferences.getBoolean("wifiSelected", false);
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

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
        System.exit(0);
    }

    private void refresh() {
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
