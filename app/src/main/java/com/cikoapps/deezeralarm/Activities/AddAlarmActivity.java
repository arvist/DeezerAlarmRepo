package com.cikoapps.deezeralarm.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cikoapps.deezeralarm.HelperClasses.AlarmManagerHelper;
import com.cikoapps.deezeralarm.HelperClasses.DeezerBase;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.Alarm;

public class AddAlarmActivity extends DeezerBase {

    private static final String TAG = "AddAlarmActivity.java";
    Toolbar toolbar;
    Typeface notoRegular;
    Context context;
    boolean fullTimeClock = true;
    final boolean selected[] = {false, false, false, false, false, false, false};
    final String elements[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    int type;
    String uri = null;
    long deezerRingtoneId;
    String ringtoneName = "";
    HelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.add_alarm_layout);
        notoRegular = Typeface.createFromAsset(getAssets(), "NotoSerif-Regular.ttf");
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        repeatNoButtonClick();
        repeatYesButtonClick();
        cancelAddingAlarm();
        addAlarm();
        ringtoneEdit();
        appBarActions();
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(fullTimeClock);
        helperClass = new HelperClass(this);
    }


    public void appBarActions() {
        ImageButton backButton = (ImageButton) findViewById(R.id.app_bar_back_btn);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.app_bar_settings);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivityForResult(intent, 2);
            }
        });
    }

    public void repeatNoButtonClick() {
        findViewById(R.id.radioButtonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < selected.length; i++) {
                    selected[i] = false;
                }
            }
        });
    }

    public void repeatYesButtonClick() {
        findViewById(R.id.radioButtonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((RadioButton) v).isChecked()) {
                    AlertDialog.Builder repeatingDaysDialog = new AlertDialog.Builder(AddAlarmActivity.this);

                    repeatingDaysDialog.setMultiChoiceItems(elements, selected, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                            if (isChecked) {
                                selected[position] = true;
                            }
                        }
                    });
                    repeatingDaysDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (helperClass.allFalse(selected)) {
                                findViewById(R.id.radioButtonYes).setActivated(false);
                                findViewById(R.id.radioButtonNo).setActivated(true);
                            }
                        }
                    });
                    repeatingDaysDialog.show();
                }

            }

        });

    }

    public void cancelAddingAlarm() {
        findViewById(R.id.cancelAlarmAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    public void addAlarm() {
        findViewById(R.id.confirmAlarmAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelperClass helperClass = new HelperClass(getApplicationContext());
                TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
                timePicker.clearFocus();
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                EditText title = (EditText) findViewById(R.id.alarmTitle);
                String alarmTitleString = title.getText().toString();
                StringBuilder sb = new StringBuilder();
                boolean repeatWeekly = helperClass.oneOrMoreTrue(selected);
                for (boolean b : selected) {
                    sb.append(b);
                    sb.append(",");
                }
                if (alarmTitleString.trim().length() < 1) {
                    alarmTitleString = "My Alarm";
                }
                Log.e(TAG, "repeat weekley on adding " + repeatWeekly);
                Alarm alarm = new Alarm(alarmTitleString, hour, minute, true, selected,
                        repeatWeekly, uri, deezerRingtoneId, type, ringtoneName);
                AlarmManagerHelper.cancelAlarms(context);
                alarm.insertIntoDataBase(context);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                AlarmManagerHelper.setAlarms(context);
                startActivity(intent);
            }
        });
    }

    public void ringtoneEdit() {
        findViewById(R.id.editRingtoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddAlarmActivity.this, RingtoneActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // RequestCode is 1 for RingtoneActivity
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                try {
                    if (data.getStringExtra("restartActivity").equalsIgnoreCase("true")) {
                        Intent intent = new Intent(AddAlarmActivity.this, RingtoneActivity.class);
                        startActivityForResult(intent, 1);
                    }

                } catch (NullPointerException e) {
                }
                type = data.getIntExtra("type", -1);
                uri = data.getStringExtra("uri");
                deezerRingtoneId = data.getLongExtra("id", -1);
                ringtoneName = data.getStringExtra("name");
                if (ringtoneName != null) {
                    if (ringtoneName.length() < 2) ringtoneName = "Default Ringtone";
                    ((TextView) findViewById(R.id.ringtone)).setText(ringtoneName);
                }
            } else if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        // RequestCode is 2 for SettingsActivity
        else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }
}

