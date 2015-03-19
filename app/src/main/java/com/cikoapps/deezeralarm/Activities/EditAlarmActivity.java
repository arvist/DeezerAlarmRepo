package com.cikoapps.deezeralarm.Activities;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cikoapps.deezeralarm.HelperClasses.AlarmDBHelper;
import com.cikoapps.deezeralarm.HelperClasses.AlarmManagerHelper;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.models.Alarm;

public class EditAlarmActivity extends DeezerBase {
    private final String[] elements = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private String title;
    private String alarmToneName;
    private int hour;
    private int minute;
    private int id;
    private TextView alarmTitleTextView;
    private TextView timeTextView;
    private TextView setRingtoneTextView;
    private boolean fullTimeClock;
    private String partOfDay;
    private int minuteOfDay;
    private int hourOfDay;
    private String TAG = "EditAlarmActivity";
    private LayoutInflater layoutInflater;
    private int type;
    private String artist;
    private ImageButton editRingtoneImageButton;
    private String uri;
    private long deezerRingtoneId;
    private boolean[] selected;
    private boolean enabled;
    private boolean[] tempSelection;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.edit_alarm_activity);
        layoutInflater = LayoutInflater.from(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        fullTimeClock = DateFormat.is24HourFormat(this);
        title = getIntent().getStringExtra(Alarm.TITLE);
        alarmToneName = getIntent().getStringExtra(Alarm.TONE_NAME);
        id = getIntent().getIntExtra(Alarm.ALARM_ID, -1);
        deezerRingtoneId = getIntent().getLongExtra(Alarm.DEEZER_RINGTONE_ID, -1);
        uri = getIntent().getStringExtra(Alarm.ALARM_URI);
        hour = getIntent().getIntExtra(Alarm.HOUR, -1);
        minute = getIntent().getIntExtra(Alarm.MINUTE, -1);
        selected = getIntent().getBooleanArrayExtra(Alarm.REPEATING_DAYS);
        partOfDay = getIntent().getStringExtra(Alarm.PART_OF_DAY);
        artist = getIntent().getStringExtra(Alarm.ARTIST);
        type = getIntent().getIntExtra(Alarm.TYPE, -1);
        enabled = getIntent().getBooleanExtra(Alarm.ENABLED, false);
        if (partOfDay == null) {
            partOfDay = "";
        }
        initViews();
        setViewValues();
        timePickerClick();
        titleEditClick();
        ringtoneEditClick();
        repeatNoButtonClick();
        repeatYesButtonClick();
        cancelAddingAlarm();
        addAlarm();
        appBarActions();
    }

    void ringtoneEditClick() {
        editRingtoneImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditAlarmActivity.this, RingtoneActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // RequestCode is 1 for RingtoneActivity
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                try {
                    if (data.getStringExtra(AddAlarmActivity.RESTART_ACTIVITY).equalsIgnoreCase("true")) {
                        Intent intent = new Intent(EditAlarmActivity.this, RingtoneActivity.class);
                        startActivityForResult(intent, 1);
                    }
                } catch (NullPointerException ignored) {
                }
                type = data.getIntExtra(RingtoneActivity.RINGTONE_TYPE, -1);
                uri = data.getStringExtra(RingtoneActivity.RINGTONE_URI);
                deezerRingtoneId = data.getLongExtra(RingtoneActivity.RINGTONE_ID_STRING, -1);
                alarmToneName = data.getStringExtra(RingtoneActivity.RINGTONE_NAME);
                artist = data.getStringExtra(RingtoneActivity.RINGTONE_ARTIST);
                if (artist != null) {
                    if (artist.equalsIgnoreCase("null")) {
                        artist = "";
                    }
                }
                setRingtoneName(alarmToneName);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void setRingtoneName(String alarmToneName) {
        String ringtoneName = alarmToneName;
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
        if (alarmToneName.length() < 1) ringtoneName = "Default ringtone";
        setRingtoneTextView.setText(ringtoneName);
    }

    private void setViewValues() {
        setRingtoneName(alarmToneName);
        String timeString;
        if (partOfDay.length() < 1) {
            hourOfDay = hour;
            minuteOfDay = minute;
            alarmTitleTextView.setText(title);
            if (hour < 10) {
                timeString = "0" + hour;
            } else {
                timeString = hour + "";
            }
            if (minute < 10) {
                timeString = timeString.concat(" : " + "0" + minute);
            } else {
                timeString = timeString.concat(" : " + minute);
            }
        } else {
            minuteOfDay = minute;
            if (partOfDay.equalsIgnoreCase(MainActivity.PART_OF_DAY_PM)) {
                if (hour == 12) {
                    hourOfDay = 12;
                } else {
                    hourOfDay = hour + 12;
                }
            } else if (partOfDay.equalsIgnoreCase(MainActivity.PART_OF_DAY_AM)) {
                if (hour == 12) {
                    hourOfDay = 0;
                } else {
                    hourOfDay = hour;
                }
            }
            timeString = hour + "";
            if (minute < 10) {
                timeString = timeString.concat(" : " + "0" + minute);
            } else {
                timeString = timeString.concat(" : " + minute);
            }
            timeString = timeString.concat(" " + partOfDay);
        }
        timeTextView.setText(timeString);
        if (new HelperClass(this).oneOrMoreTrue(selected)) {
            ((RadioButton) findViewById(R.id.radioButtonYes)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioButtonNo)).setChecked(false);
        } else {
            ((RadioButton) findViewById(R.id.radioButtonYes)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioButtonNo)).setChecked(true);
        }
    }

    private void setTimeView() {
        String timeString;
        if (fullTimeClock) {
            if (hourOfDay < 10) {
                timeString = "0" + hourOfDay;
            } else {
                timeString = "" + hourOfDay;
            }
            if (minuteOfDay < 10) {
                timeString = timeString.concat(" : 0" + minuteOfDay);
            } else {
                timeString = timeString.concat(" : " + minuteOfDay);
            }
        } else {
            String partOfDay;
            int hour;
            int minute;
            if (hourOfDay < 12) {
                hour = hourOfDay;
                partOfDay = MainActivity.PART_OF_DAY_AM;
                if (hourOfDay == 0) {
                    hour = 12;
                    partOfDay = MainActivity.PART_OF_DAY_AM;
                }
            } else if (hourOfDay == 12) {
                partOfDay = MainActivity.PART_OF_DAY_PM;
                hour = hourOfDay;
            } else {
                partOfDay = MainActivity.PART_OF_DAY_PM;
                hour = hourOfDay - 12;
            }
            minute = minuteOfDay;
            if (minute > 9) {
                timeString = hour + " : " + minute + " " + partOfDay;
            } else {
                timeString = hour + " : 0" + minute + " " + partOfDay;
            }
        }
        timeTextView.setText(timeString);
    }

    private void initViews() {
        alarmTitleTextView = (TextView) findViewById(R.id.alarmTitleTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        setRingtoneTextView = (TextView) findViewById(R.id.setRingtoneTextView);
        editRingtoneImageButton = (ImageButton) findViewById(R.id.editRingtone);
    }

    private void titleEditClick() {
        alarmTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View promptView = layoutInflater.inflate(R.layout.title_input_dialog, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditAlarmActivity.this);
                alertDialogBuilder.setView(promptView);
                final EditText editText = (EditText) promptView.findViewById(R.id.titleEditText);
                editText.setText(title);
                // setup a dialog window
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                alarmTitleTextView.setText("" + editText.getText());
                                title = "" + editText.getText();
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });
    }

    private void timePickerClick() {
        timeTextView.setOnClickListener(new View.OnClickListener() {
            int hour;
            int minutes;

            @Override
            public void onClick(View v) {
                final TimePickerDialog timePickerDialog = new TimePickerDialog(EditAlarmActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        hour = hourOfDay;
                        minutes = minute;
                    }
                }, hourOfDay, minuteOfDay, fullTimeClock);
                timePickerDialog.setTitle("Edit alarm time");
                timePickerDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        timePickerDialog.cancel();
                        hourOfDay = hour;
                        minuteOfDay = minutes;
                        Log.e(TAG, hourOfDay + " : " + minuteOfDay);
                        setTimeView();
                    }
                });
                timePickerDialog.show();
            }
        });
    }

    void repeatNoButtonClick() {
        findViewById(R.id.radioButtonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < selected.length; i++) {
                    selected[i] = false;
                }
            }
        });
    }

    void repeatYesButtonClick() {
        findViewById(R.id.radioButtonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((RadioButton) v).isChecked()) {
                    AlertDialog.Builder repeatingDaysDialog = new AlertDialog.Builder(EditAlarmActivity.this);
                    tempSelection = selected.clone();
                    repeatingDaysDialog.setMultiChoiceItems(elements, tempSelection, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        }
                    });
                    repeatingDaysDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selected = tempSelection.clone();
                            if (new HelperClass(getApplicationContext()).allFalse(selected)) {
                                ((RadioButton) findViewById(R.id.radioButtonNo)).setChecked(true);
                                ((RadioButton) findViewById(R.id.radioButtonYes)).setChecked(false);
                            }
                        }
                    });
                    repeatingDaysDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tempSelection = selected.clone();
                            dialog.cancel();
                        }
                    });
                    repeatingDaysDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (new HelperClass(getApplicationContext()).allFalse(selected)) {
                                ((RadioButton) findViewById(R.id.radioButtonNo)).setChecked(true);
                                ((RadioButton) findViewById(R.id.radioButtonYes)).setChecked(false);
                            }
                        }
                    });
                    AlertDialog dialog = repeatingDaysDialog.create();
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
            }
        });
    }

    void cancelAddingAlarm() {
        findViewById(R.id.cancelAlarmAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    void addAlarm() {
        findViewById(R.id.confirmAlarmAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelperClass helperClass = new HelperClass(getApplicationContext());
                int hour = hourOfDay;
                int minute = minuteOfDay;
                String alarmTitleString = title;
                boolean repeatWeekly = helperClass.oneOrMoreTrue(selected);
                if (alarmTitleString.trim().length() < 1) {
                    alarmTitleString = "My Alarm";
                }
                if (artist == null) artist = "";
                Alarm alarm = new Alarm(alarmTitleString, hour, minute, enabled, selected,
                        repeatWeekly, uri, deezerRingtoneId, type, alarmToneName, artist);
                Log.e(TAG, title + " " + enabled + " " + repeatWeekly + " " + deezerRingtoneId + " " + hour + " : " + minute + " by " + artist);
                AlarmManagerHelper.cancelAlarms(getApplicationContext());
                AlarmDBHelper alarmDBHelper = new AlarmDBHelper(getApplicationContext());
                alarmDBHelper.deleteAlarm(id);
                alarm.insertIntoDataBase(getApplicationContext());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                AlarmManagerHelper.setAlarms(getApplicationContext());
                startActivity(intent);
            }
        });
    }

    void appBarActions() {
        ImageButton settingsButton = (ImageButton) findViewById(R.id.app_bar_settings);
        ImageButton backButton = (ImageButton) findViewById(R.id.app_bar_back_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent, 2);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}