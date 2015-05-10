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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.AlarmViewAdapter;
import com.cikoapps.deezeralarm.helpers.AlarmDatabaseAccessor;
import com.cikoapps.deezeralarm.helpers.AlarmManagerHelper;
import com.cikoapps.deezeralarm.helpers.HelperClass;
import com.cikoapps.deezeralarm.helpers.Location;
import com.cikoapps.deezeralarm.helpers.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.helpers.WeatherDataAsync;
import com.cikoapps.deezeralarm.models.Alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity {

    public static final String PART_OF_DAY_PM = "PM";
    public static final String PART_OF_DAY_AM = "AM";
    private static final String TAG = "MainActivity.java";
    public static AlertDialog.Builder builder;
    public static int longClickedItem = -1;
    private Toolbar toolbar;
    private AlarmViewAdapter alarmViewAdapter;
    private Context context;
    private ImageButton refreshButton;
    private WeatherDataAsync weatherDataAsync;
    private RelativeLayout mainTopLayout;
    private Location myLocation;
    private boolean fullTimeClock;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity object = this;
        setContentView(R.layout.main_activity_layout);
        context = this;
        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);
        toolbar = (Toolbar) findViewById(R.id.appBar);
        ((TextView)toolbar.findViewById(R.id.app_bar_title)).setTypeface(robotoRegular);
        setSupportActionBar(toolbar);
        fullTimeClock = DateFormat.is24HourFormat(context);
        weatherDataAsync = new WeatherDataAsync(mainTopLayout, -1, -1, toolbar, context);
        //weatherDataAsync.setFromSharedPreferences();
        initializeAppBarActions();
        AlarmDatabaseAccessor alarmDBHelper = new AlarmDatabaseAccessor((getApplicationContext()));
        alarmDBHelper.createIfNotValid();
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
        refreshWeatherButton();
        updateWeatherData();
        updateDisplay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        weatherDataAsync.setFromSharedPreferences();
        updateWeatherData();
        updateDisplay();
        alarmViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                weatherDataAsync.setFromSharedPreferences();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
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
                    myLocation = new Location(this, mainTopLayout, toolbar);
                    myLocation.buildGoogleApiClient();
                }
            }
        }
    }


    private void refreshWeatherButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new HelperClass(context).haveNetworkConnection()) {
                    if (new HelperClass(context).isLocationEnabled()) {
                        if (myLocation != null) {
                            myLocation.reconnectGoogleApiClient();
                        } else {
                            myLocation = new Location(context, mainTopLayout, toolbar);
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

    void longClickDialog() {
        final String[] longClickArray = {"Turn On/Off", "Edit", "Delete"};
        builder = new AlertDialog.Builder(this);
        builder.setItems(longClickArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Alarm alarm = alarmViewAdapter.turnItem(longClickedItem);
                    AlarmDatabaseAccessor dataBaseHelper = new AlarmDatabaseAccessor(getApplicationContext());
                    AlarmManagerHelper.cancelAlarms(context);
                    dataBaseHelper.updateIsEnabled(alarm.id, alarm.enabled);
                    AlarmManagerHelper.setAlarms(context);
                } else if (which == 1) {
                    editAlarmActivityStart(null);
                } else if (which == 2) {
                    if (longClickedItem > -1) {
                        AlarmManagerHelper.cancelAlarms(context);
                        AlarmDatabaseAccessor dataBaseHelper = new AlarmDatabaseAccessor(getApplicationContext());
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
        intent.putExtra(Alarm.TITLE, alarm.title);
        intent.putExtra(Alarm.TONE_NAME, alarm.alarmToneName);
        intent.putExtra(Alarm.HOUR, alarm.hour);
        intent.putExtra(Alarm.MINUTE, alarm.minute);
        intent.putExtra(Alarm.TYPE, alarm.type);
        intent.putExtra(Alarm.REPEATING_DAYS, alarm.repeatingDays);
        intent.putExtra(Alarm.ALARM_ID, alarm.id);
        intent.putExtra(Alarm.PART_OF_DAY, alarm.partOfDay);
        intent.putExtra(Alarm.ALARM_URI, alarm.alarmTone);
        intent.putExtra(Alarm.DEEZER_RINGTONE_ID, alarm.alarmid);
        intent.putExtra(Alarm.ENABLED, alarm.enabled);
        intent.putExtra(Alarm.ARTIST, alarm.artist);
        finish();
        startActivity(intent);
    }


    void initializeAppBarActions() {
        ImageButton settingsButton = (ImageButton) findViewById(R.id.app_bar_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivityForResult(intent, 2);
            }
        });
    }


    List<Alarm> getAlarmList() {
        List<Alarm> testAlarms = new ArrayList<>();
        AlarmDatabaseAccessor dataBaseHelper = new AlarmDatabaseAccessor(getApplicationContext());
        Cursor cursor = dataBaseHelper.getAlarmsCursor();
        if (cursor.moveToFirst()) {
            do {
                int _id = cursor.getInt(cursor.getColumnIndex(AlarmDatabaseAccessor._ID));
                String title = cursor.getString(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_NAME));
                int hour = cursor.getInt(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_TIME_HOUR));
                int minute = cursor.getInt(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_TIME_MINUTE));
                String days = cursor.getString(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_REPEAT_DAYS));
                boolean weekly = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_REPEAT_WEEKLY)));
                String alarmToneName = cursor.getString(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_TONE_NAME));
                String tone = cursor.getString(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_TONE));
                long alarmid = Long.parseLong("" + cursor.getInt(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ID)));
                int type = cursor.getInt(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_TYPE));
                boolean isEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ALARM_ENABLED)));
                String artist = cursor.getString(cursor.getColumnIndex(AlarmDatabaseAccessor.COLUMN_NAME_ARTIST));
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
                        alarm.partOfDay = PART_OF_DAY_AM;
                        if (alarm.hour == 0) {
                            alarm.hour = 12;
                            alarm.partOfDay = PART_OF_DAY_AM;
                        }
                    } else if (alarm.hour == 12) {
                        alarm.partOfDay = PART_OF_DAY_PM;
                    } else {
                        alarm.partOfDay = PART_OF_DAY_PM;
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
                    if (lhs.partOfDay.equalsIgnoreCase(PART_OF_DAY_AM) && rhs.partOfDay.equalsIgnoreCase(PART_OF_DAY_PM))
                        return -1;
                    else if (lhs.partOfDay.equalsIgnoreCase(PART_OF_DAY_PM) && rhs.partOfDay.equalsIgnoreCase(PART_OF_DAY_AM))
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