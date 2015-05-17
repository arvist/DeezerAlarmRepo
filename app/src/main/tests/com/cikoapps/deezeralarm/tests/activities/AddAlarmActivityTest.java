package com.cikoapps.deezeralarm.tests.activities;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.RadioButton;
import android.widget.TimePicker;

import com.cikoapps.deezeralarm.activities.AddAlarmActivity;
import com.cikoapps.deezeralarm.activities.RingtoneActivity;
import com.cikoapps.deezeralarm.activities.SettingsActivity;
import com.robotium.solo.Solo;

/**
 * Created by Arvis on 5/16/2015.
 */
public class AddAlarmActivityTest extends ActivityInstrumentationTestCase2<AddAlarmActivity> {

    Intent intent;
    private Solo solo;
    String alarmName = "Robotium Test Deezer";

    public AddAlarmActivityTest() {
        super(AddAlarmActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void test0_appBarActions() {
        if (solo.waitForActivity(RingtoneActivity.class)) {
            solo.clickOnView(solo.getView("app_bar_settings"));
            if (solo.waitForActivity(SettingsActivity.class)) {
                solo.clickOnView(solo.getView("app_bar_back_btn"));
                if (solo.waitForActivity(AddAlarmActivity.class)) {
                    solo.clickOnView(solo.getView("app_bar_settings"));
                }
            }
        }
    }

    public void test4_buttons() {
        solo.clickOnView(solo.getView("radioButtonYes"));
        if (solo.waitForDialogToOpen()) {
            solo.clickOnText("Cancel");
        }
        solo.clickOnView(solo.getView("radioButtonNo"));
        solo.clickOnView(solo.getView("editRingtoneButton"));
        if (solo.waitForActivity(RingtoneActivity.class)) {
            solo.clickOnView(solo.getView("app_bar_back_btn"));
            if (solo.waitForActivity(AddAlarmActivity.class)) {
                solo.clickOnView(solo.getView("cancelAlarmAdd"));
            }
        }
    }

    public void test1_repeatedDayDialog() {
        RadioButton repeatYes = (RadioButton) solo.getView("radioButtonYes");
        solo.clickOnView(repeatYes);
        if (solo.waitForDialogToOpen()) {
            solo.clickInList(0);
            solo.clickOnText("Cancel");
            solo.waitForDialogToClose(1000);

            assertFalse(repeatYes.isChecked());
            solo.clickOnView(repeatYes);
            if (solo.waitForDialogToOpen()) {
                solo.clickInList(0);
                solo.clickInList(1);
                solo.clickOnText("Done");
                assertTrue(repeatYes.isChecked());
            }
        }
    }

}