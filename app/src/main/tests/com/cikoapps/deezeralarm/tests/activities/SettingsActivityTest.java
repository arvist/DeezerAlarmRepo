package com.cikoapps.deezeralarm.tests.activities;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.activities.AboutActivity;
import com.cikoapps.deezeralarm.activities.SettingsActivity;
import com.robotium.solo.Solo;

public class SettingsActivityTest extends ActivityInstrumentationTestCase2<SettingsActivity> {

    private SettingsActivity activity;
    private RadioButton windRadioButton;
    private RadioButton useOnlyWiFiButton;

    private RadioButton tempRadioButton;
    private Solo solo;

    public SettingsActivityTest() {
        super(SettingsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        activity = getActivity();
        tempRadioButton = (RadioButton) activity.findViewById(R.id.layoutTemp).findViewById(R.id.radioButtonTemp);
        windRadioButton = (RadioButton) activity.findViewById(R.id.layoutWind).findViewById(R.id.radioButtonWind);
        useOnlyWiFiButton = (RadioButton) activity.findViewById(R.id.layoutWiFi).findViewById(R.id.radioButtonWifi);
    }



    @UiThreadTest
    public void test0_radioButtonClicks() {

        SharedPreferences.Editor sharedPreferencesEditor = activity.getSharedPreferencesEditor();
        sharedPreferencesEditor.putBoolean(SettingsActivity.TEMP_FAHRENHEIT_BOOLEAN, false);
        sharedPreferencesEditor.putBoolean(SettingsActivity.WIND_MILES_BOOLEAN, false);
        sharedPreferencesEditor.putBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);
        sharedPreferencesEditor.commit();
        activity.setSavedValuesFromSharedPreferences();


        tempRadioButton.performClick();
        windRadioButton.performClick();
        useOnlyWiFiButton.performClick();

        assertTrue(tempRadioButton.isChecked());
        assertTrue(windRadioButton.isChecked());
        assertTrue(useOnlyWiFiButton.isChecked());

        tempRadioButton.performClick();
        windRadioButton.performClick();
        useOnlyWiFiButton.performClick();

        assertFalse(tempRadioButton.isChecked());
        assertFalse(windRadioButton.isChecked());
        assertFalse(useOnlyWiFiButton.isChecked());
    }

    @UiThreadTest
    public void test1_saveValuesToSharedPreferences() {

        SharedPreferences.Editor sharedPreferencesEditor = activity.getSharedPreferencesEditor();
        sharedPreferencesEditor.putBoolean(SettingsActivity.TEMP_FAHRENHEIT_BOOLEAN, false);
        sharedPreferencesEditor.putBoolean(SettingsActivity.WIND_MILES_BOOLEAN, false);
        sharedPreferencesEditor.putBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);
        sharedPreferencesEditor.putInt(SettingsActivity.MAX_ALARM_VOLUME, 8);
        sharedPreferencesEditor.putInt(SettingsActivity.SELECTED_INTERVAL,1);
        sharedPreferencesEditor.putInt(SettingsActivity.SELECTED_RINGTONE,1);

        sharedPreferencesEditor.commit();
        activity.setSavedValuesFromSharedPreferences();

        tempRadioButton.performClick();
        windRadioButton.performClick();
        useOnlyWiFiButton.performClick();
        sharedPreferencesEditor.putInt(SettingsActivity.MAX_ALARM_VOLUME, 10);
        sharedPreferencesEditor.putInt(SettingsActivity.SELECTED_INTERVAL, 2);
        sharedPreferencesEditor.putInt(SettingsActivity.SELECTED_RINGTONE, 2);

        activity.getSharedPreferencesEditor().commit();

        tempRadioButton.performClick();
        windRadioButton.performClick();
        useOnlyWiFiButton.performClick();
        activity.setSelectedVolume(8 );
        activity.setRefreshTime(1)  ;
        activity.setSelectedRingtone(1);
        activity.setSavedValuesFromSharedPreferences();
        assertTrue(tempRadioButton.isChecked());
        assertTrue(windRadioButton.isChecked());
        assertTrue(useOnlyWiFiButton.isChecked());
        assertEquals(10, activity.getSelectedVolume());
        assertEquals(2, activity.getSelectedRingtone());
        assertEquals(2,activity.getRefreshTime());
    }
    public void test2_activityNavigationButtons(){
        solo.clickOnView(solo.getView("about"));
        if(solo.waitForActivity(AboutActivity.class)){
            solo.goBack();
            if(solo.waitForActivity(SettingsActivity.class)){
                solo.clickOnView(solo.getView("app_bar_save"));
            }
        }
    }


}
