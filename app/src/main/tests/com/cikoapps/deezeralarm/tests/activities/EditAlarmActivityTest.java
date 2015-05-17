package com.cikoapps.deezeralarm.tests.activities;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.RadioButton;
import android.widget.TextView;
import com.cikoapps.deezeralarm.activities.EditAlarmActivity;
import com.cikoapps.deezeralarm.activities.RingtoneActivity;
import com.cikoapps.deezeralarm.activities.SettingsActivity;
import com.cikoapps.deezeralarm.models.Alarm;
import com.robotium.solo.Solo;


public class EditAlarmActivityTest extends ActivityInstrumentationTestCase2<EditAlarmActivity> {

    Intent intent;
    private Solo solo;
    private String title = "test";
    private String alarmToneName = "test";
    private int hour = 12;
    private int minute = 12;
    private int type = 2;
    private boolean[] repeatingDays = {false,false,false,true,true,true,true};
    private int id = 1;
    private String partOfDay = "AM";
    private long alarmid = 123;
    private String artist = "test";
    private boolean enabled = true ;
    private String alarmTone = "null";

    public EditAlarmActivityTest() {
        super(EditAlarmActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public EditAlarmActivity getActivity() {
        intent = new Intent(getInstrumentation()
                .getTargetContext(), EditAlarmActivity.class);
        intent.putExtra(Alarm.TITLE, title);
        intent.putExtra(Alarm.TONE_NAME,alarmToneName);
        intent.putExtra(Alarm.HOUR, hour);
        intent.putExtra(Alarm.MINUTE, minute);
        intent.putExtra(Alarm.TYPE, type);
        intent.putExtra(Alarm.REPEATING_DAYS, repeatingDays);
        intent.putExtra(Alarm.ALARM_ID, id);
        intent.putExtra(Alarm.PART_OF_DAY, partOfDay);
        intent.putExtra(Alarm.ALARM_URI, alarmTone);
        intent.putExtra(Alarm.DEEZER_RINGTONE_ID, alarmid);
        intent.putExtra(Alarm.ENABLED, enabled);
        intent.putExtra(Alarm.ARTIST, artist);
        setActivityIntent(intent);
        return super.getActivity();
    }

    public void test0_appBarActions() {
        if (solo.waitForActivity(RingtoneActivity.class)) {
            solo.clickOnView(solo.getView("app_bar_settings"));
            if (solo.waitForActivity(SettingsActivity.class)) {
                solo.clickOnView(solo.getView("app_bar_back_btn"));
                if (solo.waitForActivity(EditAlarmActivity.class)) {
                    solo.clickOnView(solo.getView("app_bar_settings"));
                }
            }
        }
    }
    public void test1_alarmValues(){
        assertTrue((hour + " : " + minute + " " + partOfDay).equalsIgnoreCase(((TextView)solo.getView("timeTextView")).getText().toString()));
        assertTrue(((RadioButton)solo.getView("radioButtonYes")).isChecked());
        assertTrue((alarmToneName + " by " + title).equalsIgnoreCase(((TextView) solo.getView("setRingtoneTextView")).getText().toString()));
    }

    public void test2_timePicker(){
        solo.clickOnView(solo.getView("timeTextView"));
        if(solo.waitForDialogToOpen()){
            int minute = 11;
            int hour = 11;
            solo.setTimePicker(0,11,11);
            solo.clickOnText("Done");
            if(solo.waitForDialogToClose()) {
                assertTrue((hour + " : " + minute).equalsIgnoreCase(((TextView) solo.getView("timeTextView")).getText().toString()));
            }
        }
    }
    public void test3_titleDialog(){
        solo.clickOnView(solo.getView("alarmTitleTextView"));
        if(solo.waitForDialogToOpen()){
            String title = "Alarm";
            solo.clearEditText(0);
            solo.typeText(solo.getEditText(0),title);
            solo.clickOnText("Done");
            if(solo.waitForDialogToClose()){
                assertTrue(title.equalsIgnoreCase(((TextView)solo.getView("alarmTitleTextView")).getText().toString()));
            }
        }
    }
}
