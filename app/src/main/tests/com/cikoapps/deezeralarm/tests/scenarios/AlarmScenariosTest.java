package com.cikoapps.deezeralarm.tests.scenarios;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;


import com.cikoapps.deezeralarm.activities.AddAlarmActivity;
import com.cikoapps.deezeralarm.activities.AlarmScreenActivity;
import com.cikoapps.deezeralarm.activities.EditAlarmActivity;
import com.cikoapps.deezeralarm.activities.MainActivity;
import com.cikoapps.deezeralarm.activities.QuoteActivity;
import com.cikoapps.deezeralarm.activities.RingtoneActivity;
import com.robotium.solo.Solo;

import java.util.Calendar;


public class AlarmScenariosTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;

    public AlarmScenariosTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }


    public void test0_cancelAlarmCreation() {
        solo.clickOnView(solo.getView("floatingActionButtonView"));
        if (solo.waitForActivity(AddAlarmActivity.class)) {
            solo.clickOnView(solo.getView("cancelAlarmAdd"));
            solo.waitForActivity(MainActivity.class);
        }
    }
    public void test1_createAndDeleteAlarm() {
        solo.clickOnView(solo.getView("floatingActionButtonView"));
        if (solo.waitForActivity(AddAlarmActivity.class)) {
            solo.clickOnView(solo.getView("confirmAlarmAdd"));
            if(solo.waitForActivity(MainActivity.class,3*1000)) {
                solo.clickLongOnView(solo.getView("alarmRow"));
                solo.waitForDialogToOpen();
                solo.clickOnText("Delete");
            }
        }
    }
    public void test2_createEditAlarm() {
        if (solo.waitForView(solo.getView("floatingActionButtonView"))) {
            solo.clickOnView(solo.getView("floatingActionButtonView"));
        }
        if (solo.waitForActivity(AddAlarmActivity.class)) {
            solo.setTimePicker(0, 22, 22);
            solo.typeText(solo.getEditText(0), "Alarm at 22:22");
            solo.clickOnView(solo.getView("confirmAlarmAdd"));
            solo.waitForActivity(MainActivity.class);
            solo.clickLongOnView(solo.getView("alarmRow"));
            solo.waitForDialogToOpen();
            solo.clickOnText("Edit");
            if (solo.waitForActivity(EditAlarmActivity.class)) {
                solo.clickOnText("Alarm at 22:22");
                solo.clearEditText(0);
                solo.typeText(0, "Edited Alarm to 11:11");
                solo.clickOnText("Done");
                solo.clickOnView(solo.getView("timeTextView"));
                solo.setTimePicker(0, 11, 11);
                solo.clickOnText("Done");
                solo.clickOnView(solo.getView("confirmAlarmAdd"));
            }
            if (solo.waitForActivity(MainActivity.class)) {
                assertEquals(((TextView) solo.getView("titleTextView")).getText().toString(), "Edited Alarm to 11:11");
                assertEquals(((TextView) solo.getView("timeTextView", 1)).getText().toString(), "11 : 11");
            }
        }
    }



    public void test3_CreateAndLaunchAlarm() throws Exception {

        if (solo.waitForView(solo.getView("floatingActionButtonView"))) {
            solo.clickOnView(solo.getView("floatingActionButtonView"));
        }
        if (solo.waitForActivity(AddAlarmActivity.class)) {
            solo.typeText(solo.getEditText(0), "Alarm");
            Calendar calendar = Calendar.getInstance();
            int minute = 2 + calendar.get(Calendar.MINUTE);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (minute > 57) {
                hour++;
                if (minute == 58) {
                    minute = 0;
                } else {
                    minute = 1;
                }
            }
            solo.setTimePicker(0, hour, minute);
            solo.clickOnView(solo.getView("confirmAlarmAdd"));
            solo.waitForActivity(AlarmScreenActivity.class, 1000 * 120);
            solo.clickOnView(solo.getView("buttonText"));
            solo.waitForActivity(MainActivity.class);
        }
    }

    public void test4_AlarmWithRingtoneSelect() throws InterruptedException {

        solo.clickOnView(solo.getView("floatingActionButtonView"));
        if (solo.waitForActivity(AddAlarmActivity.class)) {

            solo.clickOnView(solo.getView("editRingtoneButton"));
            if (solo.waitForActivity(RingtoneActivity.class)) {
                solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
                solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
                solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
                solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
                Thread.sleep(3500);
                solo.clickInList(3);
                solo.clickOnView(solo.getView("confirmRingtone"));
            }
            TextView ringtoneTextView = (TextView) solo.getView("ringtone");

            boolean defaultRingtoneText = ringtoneTextView.getText().toString().equals("Default ringtone");
            assertFalse(defaultRingtoneText);

            solo.typeText(solo.getEditText(0), "Deezer Radio Alarm Tone");
            Calendar calendar = Calendar.getInstance();
            int minute = 2 + calendar.get(Calendar.MINUTE);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (minute > 57) {
                hour++;
            }
            solo.setTimePicker(0, hour, minute);
            solo.clickOnView(solo.getView("confirmAlarmAdd"));
            solo.waitForActivity(AlarmScreenActivity.class, 1000 * 120);
            solo.clickOnView(solo.getView("stopAlarmButton"));
            solo.waitForActivity(QuoteActivity.class);
        }
    }


}
