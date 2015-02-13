package com.cikoapps.deezeralarm;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by arvis.taurenis on 2/13/2015.
 */
public class RepeatingDaysSelector extends Activity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeating_days_select_layout);
        //toolbar = (Toolbar) findViewById(R.id.appBar);
        //setSupportActionBar(toolbar);
    }
}
