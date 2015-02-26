package com.cikoapps.deezeralarm.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cikoapps.deezeralarm.HelperClasses.AlarmDBHelper;
import com.cikoapps.deezeralarm.HelperClasses.AlarmManagerHelper;
import com.cikoapps.deezeralarm.HelperClasses.MyLocation;
import com.cikoapps.deezeralarm.HelperClasses.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.HelperClasses.WeatherDataAsync;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.AlarmViewAdapter;
import com.cikoapps.deezeralarm.models.Alarm;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity.java";
    Toolbar toolbar;
    AlarmViewAdapter alarmViewAdapter;
    private RecyclerView alarmRecyclerView;
    Typeface notoRegular;

    boolean metricSystem = true;
    Context context;
    public static AlertDialog.Builder builder;
    public static int longClickedItem = -1;
    ImageButton refreshButton;
    WeatherDataAsync weatherDataAsync;
    RelativeLayout mainTopLayout;
    MyLocation myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appBarActions();
        AlarmDBHelper alarmDBHelper = new AlarmDBHelper((getApplicationContext()));
        if (alarmDBHelper.checkForData()) Log.e("DATABASE", "VALID");
        else Log.e("DATABASE", "INVALID");
        notoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        context = this;

        alarmRecyclerView = (RecyclerView) findViewById(R.id.alarmRecyclerView);
        alarmRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));
        alarmRecyclerView.setHasFixedSize(true);
        alarmViewAdapter = new AlarmViewAdapter(getApplicationContext(), getAlarmList());
        alarmRecyclerView.setAdapter(alarmViewAdapter);

        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        alarmRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);


        refreshButton = (ImageButton) mainTopLayout.findViewById(R.id.refreshButton);


        findViewById(R.id.floatingActionButtonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddAlarmActivity.class);
                finish();
                startActivity(intent);
            }
        });

        longClickDialog();
        refresh();

        myLocation = new MyLocation(this, metricSystem, mainTopLayout);
        myLocation.buildGoogleApiClient();
    }

    private void refresh() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLocation.reconnectGoogleApiClient();
            }
        });
    }

    public void longClickDialog() {
        final String[] longClickArray = {"Turn On/Off", "Edit", "Delete"};
        builder = new AlertDialog.Builder(this);
        builder.setItems(longClickArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Alarm alarm = alarmViewAdapter.turnItem(longClickedItem);
                    AlarmDBHelper dataBaseHelper = new AlarmDBHelper(getApplicationContext());

                    AlarmManagerHelper.cancelAlarms(context);

                    dataBaseHelper.updateIsEnabled(alarm.id, alarm.enabled);

                    AlarmManagerHelper.setAlarms(context);

                } else if (which == 1) {

                    Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show();
                } else if (which == 2) {
                    if (longClickedItem > -1) {
                        AlarmManagerHelper.cancelAlarms(context);
                        AlarmDBHelper dataBaseHelper = new AlarmDBHelper(getApplicationContext());
                        dataBaseHelper.deleteAlarm(alarmViewAdapter.removeItem(longClickedItem));
                        AlarmManagerHelper.setAlarms(context);
                    }
                }
            }
        });
        builder.create();
    }

    @Override
    public View onCreateView(String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    public void appBarActions() {

        ImageButton settingsButton = (ImageButton) findViewById(R.id.app_bar_settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    public List<Alarm> getAlarmList() {
        List<Alarm> testAlarms = new ArrayList<>();
        AlarmDBHelper dataBaseHelper = new AlarmDBHelper(getApplicationContext());
        Cursor cursor = dataBaseHelper.getAlarms();
        if (cursor.moveToFirst()) {
            do {
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int hour = cursor.getInt(cursor.getColumnIndex("hour"));
                int minute = cursor.getInt(cursor.getColumnIndex("minute"));
                String days = cursor.getString(cursor.getColumnIndex("days"));
                boolean weekly = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("weekly")));
                String alarmToneName = cursor.getString(cursor.getColumnIndex("alarmToneName"));
                String tone = cursor.getString(cursor.getColumnIndex("tone"));
                long alarmid = Long.parseLong("" + cursor.getInt(cursor.getColumnIndex("alarmid")));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                boolean isEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isEnabled")));

                String[] repeatingDaysStrings = days.split(",");
                boolean[] repeatingDays = new boolean[repeatingDaysStrings.length];
                int i = 0;
                for (String day : repeatingDaysStrings) {
                    boolean value = Boolean.parseBoolean(day);
                    repeatingDays[i] = value;
                    i++;
                }
                testAlarms.add(new Alarm(title, _id, hour, minute, isEnabled, repeatingDays, weekly, tone, alarmid, type, alarmToneName));
            } while (cursor.moveToNext());
        }
        dataBaseHelper.close();

        return testAlarms;
    }


}