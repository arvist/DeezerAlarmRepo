package com.cikoapps.deezeralarm.tests.activities;

import android.test.ActivityInstrumentationTestCase2;


import com.cikoapps.deezeralarm.activities.AddAlarmActivity;
import com.cikoapps.deezeralarm.activities.EditAlarmActivity;
import com.cikoapps.deezeralarm.activities.MainActivity;
import com.cikoapps.deezeralarm.activities.SettingsActivity;
import com.robotium.solo.Solo;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public MainActivityTest() {
        super(MainActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void test1_buttons(){
        solo.clickOnView(solo.getView("app_bar_settings"));
        if(solo.waitForActivity(SettingsActivity.class)){
            solo.goBack();
            if(solo.waitForActivity(MainActivity.class)){
                solo.clickOnView(solo.getView("floatingActionButtonView"));
                solo.waitForActivity(AddAlarmActivity.class);
            }
        }
    }
    public void test0_alarmOptions() throws InterruptedException {
        if (solo.waitForView(solo.getView("floatingActionButtonView"))) {
            solo.clickOnView(solo.getView("floatingActionButtonView"));
        }
        if (solo.waitForActivity(AddAlarmActivity.class)) {
            solo.typeText(solo.getEditText(0), "Alarm");
            solo.clickOnView(solo.getView("confirmAlarmAdd"));
            if (solo.waitForActivity(MainActivity.class)) {
                solo.clickLongOnView(solo.getView("alarmRow"));
                if (solo.waitForDialogToOpen()) {
                    solo.clickInList(0);
                    if (solo.waitForDialogToClose()) {
                        solo.clickLongOnView(solo.getView("alarmRow"));
                        if (solo.waitForDialogToOpen()) {
                            solo.clickOnText("Edit");
                            if(solo.waitForActivity(EditAlarmActivity.class)){
                                solo.goBack();
                                solo.clickLongOnView(solo.getView("alarmRow"));
                                if (solo.waitForDialogToOpen()) {
                                    solo.clickOnText("Delete");

                                }
                            }
                        }
                    }
                }
             }
        }
    }
}
