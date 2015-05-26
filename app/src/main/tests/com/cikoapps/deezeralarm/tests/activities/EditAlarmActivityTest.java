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

    private Solo solo;
    private final String title = "test";
    private final String alarmToneName = "test";
    private final int hour = 12;
    private final int minute = 12;
    private final boolean[] repeatingDays = {false,false,false,true,true,true,true};
    private final String partOfDay = "AM";

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
        Intent intent = new Intent(getInstrumentation()
                .getTargetContext(), EditAlarmActivity.class);
        intent.putExtra(Alarm.TITLE, title);
        intent.putExtra(Alarm.TONE_NAME, alarmToneName);
        intent.putExtra(Alarm.HOUR, hour);
        intent.putExtra(Alarm.MINUTE, minute);
        int type = 2;
        intent.putExtra(Alarm.TYPE, type);
        intent.putExtra(Alarm.REPEATING_DAYS, repeatingDays);
        int id = 1;
        intent.putExtra(Alarm.ALARM_ID, id);
        intent.putExtra(Alarm.PART_OF_DAY, partOfDay);
        String alarmTone = "null";
        intent.putExtra(Alarm.ALARM_URI, alarmTone);
        long alarmid = 123;
        intent.putExtra(Alarm.DEEZER_RINGTONE_ID, alarmid);
        intent.putExtra(Alarm.ENABLED, true);
        String artist = "test";
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
