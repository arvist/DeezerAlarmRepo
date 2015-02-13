package com.cikoapps.deezeralarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Created by arvis.taurenis on 2/13/2015.
 */
public class AddAlarmActivity extends ActionBarActivity {

    Toolbar toolbar;
    Typeface notoRegular;
    Context context;
    boolean fullTimeClock = true;
    final boolean selected[] = {false, false, false, false, false, false, false};
    final String elements[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};


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
        AlarmDBHelper alarmDBHelper = new AlarmDBHelper((getApplicationContext()));
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(fullTimeClock);


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
                finish();
            }
        });
    }
    public void addAlarm(){
        findViewById(R.id.confirmAlarmAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
                timePicker.clearFocus();
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                EditText title = (EditText) findViewById(R.id.alarmTitle);
                String alarmTitleString = title.getText().toString();
                StringBuilder sb = new StringBuilder();
                for(boolean b : selected){
                    sb.append(b);
                    sb.append(",");
                }
                Alarm alarm =  new Alarm(alarmTitleString,hour,minute, true,selected,
                true, Uri.EMPTY , 0, 0, 0, 0, 0, "alarmToneName");
                alarm.insertIntoDataBase(context);
            }
        });
    }
    public void ringtoneEdit(){
        findViewById(R.id.editRingtoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddAlarmActivity.this, RingtoneActivity.class);
                startActivity(intent);
            }
        });
    }
}

