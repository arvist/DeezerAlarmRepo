package com.cikoapps.deezeralarm.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.AlarmViewAdapter;
import com.cikoapps.deezeralarm.fragments.WeatherFragment;
import com.cikoapps.deezeralarm.helpers.AlarmDatabaseAccessor;
import com.cikoapps.deezeralarm.helpers.AlarmManagerHelper;
import com.cikoapps.deezeralarm.helpers.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.models.Alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    public static final String PART_OF_DAY_PM = "PM";
    public static final String PART_OF_DAY_AM = "AM";
    private static final String TAG = "MainActivity.java";
    public static AlertDialog.Builder builder;
    public static int longClickedItem = -1;
    private Toolbar toolbar;
    private AlarmViewAdapter alarmViewAdapter;
    private Context context;
    private boolean fullTimeClock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity object = this;
        setContentView(R.layout.main_activity_layout);
        context = this;
        Typeface robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        toolbar = (Toolbar) findViewById(R.id.appBar);
        ((TextView) toolbar.findViewById(R.id.app_bar_title)).setTypeface(robotoRegular);
        setSupportActionBar(toolbar);
        fullTimeClock = DateFormat.is24HourFormat(context);
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
        findViewById(R.id.floatingActionButtonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddAlarmActivity.class);
                finish();
                startActivity(intent);
            }
        });
        longClickDialog();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.weather_fragment, new WeatherFragment(0, 0, toolbar, context));
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmViewAdapter.notifyDataSetChanged();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
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