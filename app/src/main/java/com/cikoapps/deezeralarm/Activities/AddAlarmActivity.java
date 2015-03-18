
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

        import com.cikoapps.deezeralarm.DeezerBase;
        import com.cikoapps.deezeralarm.HelperClasses.AlarmManagerHelper;
        import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
        import com.cikoapps.deezeralarm.R;
        import com.cikoapps.deezeralarm.models.Alarm;

public class AddAlarmActivity extends DeezerBase {

    public static final String RESTART_ACTIVITY = "restartActivity";
    private static final String TAG = "AddAlarmActivity";
    private final String elements[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    boolean[] selected = {false, false, false, false, false, false, false};
    private Context context;
    private int type;
    private String uri = null;
    private long deezerRingtoneId;
    private String ringtoneName = "";
    private String artist = "";
    private HelperClass helperClass;
    private boolean[] tempSelection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.add_alarm_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        repeatNoButtonClick();
        repeatYesButtonClick();
        cancelAddingAlarm();
        addAlarm();
        ringtoneEdit();
        appBarActions();
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        boolean fullTimeClock = DateFormat.is24HourFormat(context);
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

    void appBarActions() {
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
                    AlertDialog.Builder repeatingDaysDialog = new AlertDialog.Builder(AddAlarmActivity.this);
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
                Log.e(TAG, alarmTitleString + " " + hour + ":" + minute + " " + deezerRingtoneId + " " + type + " " + ringtoneName);
                AlarmManagerHelper.cancelAlarms(context);
                alarm.insertIntoDataBase(context);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                AlarmManagerHelper.setAlarms(context);
                startActivity(intent);
            }
        });
    }

    void ringtoneEdit() {
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
                    if (data.getStringExtra(RESTART_ACTIVITY).equalsIgnoreCase("true")) {
                        Intent intent = new Intent(AddAlarmActivity.this, RingtoneActivity.class);
                        startActivityForResult(intent, 1);
                    }
                } catch (NullPointerException ignored) {
                }
                type = data.getIntExtra(RingtoneActivity.RINGTONE_TYPE, -1);
                uri = data.getStringExtra(RingtoneActivity.RINGTONE_URI);
                deezerRingtoneId = data.getLongExtra(RingtoneActivity.RINGTONE_ID_STRING, -1);
                ringtoneName = data.getStringExtra(RingtoneActivity.RINGTONE_NAME);
                artist = data.getStringExtra(RingtoneActivity.RINGTONE_ARTIST);
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
                    ((TextView) findViewById(R.id.ringtone)).setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf"));
                }
            }
        }
    }
}


