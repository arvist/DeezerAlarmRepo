package com.cikoapps.deezeralarm.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cikoapps.deezeralarm.HelperClasses.AlarmDBHelper;
import com.cikoapps.deezeralarm.HelperClasses.AlarmManagerHelper;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.HelperClasses.MyLocation;
import com.cikoapps.deezeralarm.HelperClasses.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.HelperClasses.WeatherDataAsync;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.AlarmViewAdapter;
import com.cikoapps.deezeralarm.models.Alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity.java";
    Toolbar toolbar;
    AlarmViewAdapter alarmViewAdapter;
    Typeface robotoRegular;
    Context context;
    public static AlertDialog.Builder builder;
    public static int longClickedItem = -1;
    ImageButton refreshButton;
    WeatherDataAsync weatherDataAsync;
    RelativeLayout mainTopLayout;
    MyLocation myLocation;
    HelperClass helperClass;
    private boolean fullTimeClock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity object = this;
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        context = this;
        helperClass = new HelperClass(this);
        mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        fullTimeClock = DateFormat.is24HourFormat(context);

        weatherDataAsync = new WeatherDataAsync(mainTopLayout, -1, -1, toolbar, context);
        //weatherDataAsync.setFromSharedPreferences();
        appBarActions();
        AlarmDBHelper alarmDBHelper = new AlarmDBHelper((getApplicationContext()));
        alarmDBHelper.checkForData();
        robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");

        RecyclerView alarmRecyclerView = (RecyclerView) findViewById(R.id.alarmRecyclerView);
        alarmRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));
        alarmRecyclerView.setHasFixedSize(true);
        alarmViewAdapter = new AlarmViewAdapter(getApplicationContext(), getAlarmList(), object);
        alarmRecyclerView.setAdapter(alarmViewAdapter);

        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        alarmRecyclerView.setItemAnimator(new DefaultItemAnimator());

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
                    myLocation = new MyLocation(this, mainTopLayout, toolbar);
                    myLocation.buildGoogleApiClient();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        weatherDataAsync.setFromSharedPreferences();
        updateWeatherData();
        alarmViewAdapter.notifyDataSetChanged();

    }

    private void refresh() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new HelperClass(context).haveNetworkConnection()) {
                    if (new HelperClass(context).isLocationEnabled()) {
                        if (myLocation != null) {
                            myLocation.reconnectGoogleApiClient();
                        } else {
                            myLocation = new MyLocation(context, mainTopLayout, toolbar);
                            myLocation.buildGoogleApiClient();
                        }
                    } else {
                        Toast.makeText(context, "Couldn't determine current location", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "No network connection", Toast.LENGTH_SHORT).show();
                }
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
                    editAlarmActivityStart(null);
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

    public void editAlarmActivityStart(Alarm alarmClicked) {
        Intent intent = new Intent(getApplicationContext(), EditAlarmActivity.class);
        Alarm alarm;
        if (alarmClicked == null) {
            alarm = alarmViewAdapter.getEditAlarm(longClickedItem);
        } else {
            alarm = alarmClicked;
        }
        intent.putExtra("title", alarm.title);
        intent.putExtra("alarmToneName", alarm.alarmToneName);
        intent.putExtra("hour", alarm.hour);
        intent.putExtra("minute", alarm.minute);
        intent.putExtra("type", alarm.type);
        intent.putExtra("repeatingDays", alarm.repeatingDays);
        intent.putExtra("id", alarm.id);
        intent.putExtra("partOfDay", alarm.partOfDay);
        intent.putExtra("uri", alarm.alarmTone);
        intent.putExtra("deezerRingtoneId", alarm.alarmid);
        intent.putExtra("enabled", alarm.enabled);
        intent.putExtra("artist", alarm.artist);
        finish();
        startActivity(intent);
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
                weatherDataAsync.setFromSharedPreferences();
            }
        }
    }

    public List<Alarm> getAlarmList() {
        Log.e(TAG, "getAlarmList");
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
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                String[] repeatingDaysStrings = days.split(",");
                boolean[] repeatingDays = new boolean[repeatingDaysStrings.length];
                int i = 0;
                for (String day : repeatingDaysStrings) {
                    boolean value = Boolean.parseBoolean(day);
                    repeatingDays[i] = value;
                    i++;
                }
                Alarm alarm = new Alarm(title, _id, hour, minute, isEnabled, repeatingDays, weekly, tone, artist, alarmid, type, alarmToneName);
                testAlarms.add(alarm);
                if (!fullTimeClock) {
                    alarm.usClock = true;
                    if (alarm.hour < 12) {
                        alarm.partOfDay = "AM";
                        if (alarm.hour == 0) {
                            alarm.hour = 12;
                            alarm.partOfDay = "AM";
                        }
                    } else if (alarm.hour == 12) {
                        alarm.partOfDay = "PM";
                    } else {
                        alarm.partOfDay = "PM";
                        alarm.hour = alarm.hour - 12;
                    }
                } else {
                    alarm.usClock = false;
                    alarm.partOfDay = null;
                }

            } while (cursor.moveToNext());
            dataBaseHelper.close();
        }
        Collections.sort(testAlarms, new Comparator<Alarm>() {
            @Override
            public int compare(Alarm lhs, Alarm rhs) {
                //  an integer < 0 if lhs is less than rhs, 0 if they are equal, and > 0 if lhs is greater than rhs.
                if (DateFormat.is24HourFormat(context)) {
                    if (lhs.hour != rhs.hour) {
                        if (lhs.hour < rhs.hour) return -1;
                        if (lhs.hour > rhs.hour) return 1;
                    } else {
                        if (lhs.minute < rhs.minute) return -1;
                        if (lhs.minute > rhs.minute) return 1;
                        if (lhs.minute == rhs.minute) return 0;
                    }
                    return 0;
                } else {
                    Log.e(TAG, "Sorting 12 hour clock values");
                    if (lhs.partOfDay.equalsIgnoreCase("AM") && rhs.partOfDay.equalsIgnoreCase("PM"))
                        return -1;
                    else if (lhs.partOfDay.equalsIgnoreCase("PM") && rhs.partOfDay.equalsIgnoreCase("AM"))
                        return 1;
                    else if (lhs.partOfDay.equalsIgnoreCase(rhs.partOfDay)) {
                        if (lhs.hour == 12 && rhs.hour > 0) return -1;
                        else if (lhs.hour > 0 && rhs.hour == 12) return 1;
                        if (lhs.hour != rhs.hour) {
                            if (lhs.hour < rhs.hour) return -1;
                            if (lhs.hour > rhs.hour) return 1;
                        } else {
                            if (lhs.minute < rhs.minute) return -1;
                            if (lhs.minute > rhs.minute) return 1;
                            if (lhs.minute == rhs.minute) return 0;
                        }
                    }
                }
                return 0;
            }

            @Override
            public boolean equals(Object object) {
                return false;
            }
        });
        return testAlarms;
    }


}