package com.cikoapps.deezeralarm.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
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

    private static final String TAG = "AddAlarmActivity";
    Toolbar toolbar;
    Typeface robotoRegular;
    Context context;
    boolean fullTimeClock = true;
    final boolean selected[] = {false, false, false, false, false, false, false};
    final String elements[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    int type;
    String uri = null;
    long deezerRingtoneId;
    String ringtoneName = "";
    String artist = "";
    HelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate AddAlarmActivity.java");
        context = getApplicationContext();
        setContentView(R.layout.add_alarm_layout);
        robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        repeatNoButtonClick();
        repeatYesButtonClick();
        cancelAddingAlarm();
        addAlarm();
        ringtoneEdit();
        appBarActions();
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        fullTimeClock = DateFormat.is24HourFormat(context);
        timePicker.setIs24HourView(fullTimeClock);
        helperClass = new HelperClass(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
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
                EditText title = (EditText) findViewById(R.id.alarmTitleTextView);
                String alarmTitleString = title.getText().toString();
                boolean repeatWeekly = helperClass.oneOrMoreTrue(selected);
                if (alarmTitleString.trim().length() < 1) {
                    alarmTitleString = "My Alarm";
                }
                if (artist == null) artist = "";
                Alarm alarm = new Alarm(alarmTitleString, hour, minute, true, selected,
                        repeatWeekly, uri, deezerRingtoneId, type, ringtoneName, artist);
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
                new CountDownTimer(350, 80) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        Intent intent = new Intent(AddAlarmActivity.this, RingtoneActivity.class);
                        startActivityForResult(intent, 1);
                    }
                }.start();

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
                } catch (NullPointerException ignored) {
                }
                type = data.getIntExtra("type", -1);
                uri = data.getStringExtra("uri");
                deezerRingtoneId = data.getLongExtra("id", -1);
                ringtoneName = data.getStringExtra("name");
                artist = data.getStringExtra("artist");
                if (artist == null) {
                    artist = "";
                }
                if (ringtoneName != null) {
                    if (ringtoneName.length() < 2) {
                        ringtoneName = "Default Ringtone";
                    } else {
                        if (type == 0) {
                            ringtoneName = ringtoneName.concat(" Ringtone");
                        } else if (type == 1) {
                            ringtoneName = ringtoneName.concat(" Playlist");
                        } else if (type == 3) {
                            ringtoneName = ringtoneName.concat(" Artist Radio");
                        } else if (type == 4) {
                            ringtoneName = ringtoneName.concat(" Radio");
                        } else if (type == 2) {
                            if (artist != null)
                                ringtoneName = ringtoneName.concat(" by " + artist);
                        }
                    }
                    ((TextView) findViewById(R.id.ringtone)).setText(ringtoneName);
                }
            }
        }
    }
}

