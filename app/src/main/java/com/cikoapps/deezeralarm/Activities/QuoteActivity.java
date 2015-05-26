package com.cikoapps.deezeralarm.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.fragments.WeatherFragment;
import com.cikoapps.deezeralarm.helpers.Quotes;

public class QuoteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.quote_activity_layout);
        TextView quoteTextView = (TextView) findViewById(R.id.quoteTextView);
        TextView authorTextView = (TextView) findViewById(R.id.authorTextView);
        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        Typeface robotoItalic = Typeface.createFromAsset(getAssets(), "Roboto-Italic.ttf");

        Quotes quote = Quotes.getQuote();
        quoteTextView.setText(quote.quote);
        authorTextView.setText(quote.author);
        quoteTextView.setTypeface(robotoItalic);
        authorTextView.setTypeface(robotoRegular);


        // Apply weather fragment if enabled in settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean weatherService = sharedPreferences.getBoolean(SettingsActivity.WEATHER_SERVICE, false);
        if(!weatherService) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.weather_fragment, new WeatherFragment(0, 0, null, this));
            ft.commit();
        }
    }

    // If user leaves activity than close application
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
        System.exit(0);
    }


}
