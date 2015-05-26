package com.cikoapps.deezeralarm.tests.scenarios;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cikoapps.deezeralarm.activities.AddAlarmActivity;
import com.cikoapps.deezeralarm.activities.MainActivity;
import com.cikoapps.deezeralarm.activities.RingtoneActivity;
import com.cikoapps.deezeralarm.activities.SettingsActivity;
import com.robotium.solo.Solo;

public class SettingsScenariosMainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;
    public SettingsScenariosMainActivityTest() {
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

    public void test0_Temperature() {
        TextView tempTextView;
        String tempUnit;
        TextView tempTextViewNew;
        String tempUnitNew;
        solo.waitForActivity(MainActivity.class);
        tempTextView = (TextView) solo.getView("tempTextView");
        tempUnit = tempTextView.getText().toString();
        tempUnit = tempUnit.substring(tempTextView.getText().toString().length() - 1, tempTextView.getText().toString().length());
        solo.clickOnView(solo.getView("app_bar_settings"));
        if (solo.waitForActivity(SettingsActivity.class)) {
            solo.clickOnView(solo.getView("radioButtonTemp"));
            solo.clickOnView(solo.getView("app_bar_save"));
            if (solo.waitForActivity(MainActivity.class)) {
                tempTextViewNew = (TextView) solo.getView("tempTextView");
                tempUnitNew = tempTextViewNew.getText().toString();
                tempUnitNew = tempUnitNew.substring(tempTextViewNew.getText().toString().length() - 1, tempTextViewNew.getText().toString().length());
                assertFalse(tempUnit.equalsIgnoreCase(tempUnitNew));
                assertTrue(tempUnitNew.equalsIgnoreCase("℃") || tempUnitNew.equalsIgnoreCase("℉"));
                assertTrue(tempUnit.equalsIgnoreCase("℃") || tempUnit.equalsIgnoreCase("℉"));
            }
        }
    }

    public void test1_wind() {
        TextView windTextView;
        String windUnit;
        TextView windTextViewNew;
        String windUnitNew;
        solo.waitForActivity(MainActivity.class);
        windTextView = (TextView) solo.getView("windTextView");
        windUnit = windTextView.getText().toString();
        windUnit = windUnit.substring(windTextView.getText().toString().length() - 3, windTextView.getText().toString().length());
        solo.clickOnView(solo.getView("app_bar_settings"));
        if (solo.waitForActivity(SettingsActivity.class)) {
            solo.clickOnView(solo.getView("radioButtonWind"));
            solo.clickOnView(solo.getView("app_bar_save"));
            if (solo.waitForActivity(MainActivity.class)) {
                windTextViewNew = (TextView) solo.getView("windTextView");
                windUnitNew = windTextViewNew.getText().toString();
                windUnitNew = windUnitNew.substring(windTextViewNew.getText().toString().length() - 3, windTextViewNew.getText().toString().length());
                assertFalse(windUnit.equalsIgnoreCase(windUnitNew));
                assertTrue(windUnitNew.equalsIgnoreCase("mph") || windUnitNew.equalsIgnoreCase("kph"));
                assertTrue(windUnit.equalsIgnoreCase("mph") || windUnit.equalsIgnoreCase("kph"));
            }
        }

    }

    public void test2_cellularNetwork() {
        solo.waitForActivity(MainActivity.class);
        solo.clickOnView(solo.getView("app_bar_settings"));
        if (solo.waitForActivity(SettingsActivity.class)) {
            RadioButton radioButtonWifi = (RadioButton) solo.getView("radioButtonWifi");
            if (!radioButtonWifi.isChecked()) {
                solo.clickOnView(radioButtonWifi);
            }
            solo.clickOnView(solo.getView("app_bar_save"));
            if (solo.waitForActivity(MainActivity.class)) {
                solo.clickOnView(solo.getView("floatingActionButtonView"));
                if (solo.waitForActivity(AddAlarmActivity.class)) {
                    solo.clickOnView(solo.getView("editRingtoneButton"));
                    if (solo.waitForActivity(RingtoneActivity.class)) {
                        solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
                        solo.waitForText("Connect to Wi-Fi, or allow app to use cellular network in settings");
                    }
                }
            }
        }
    }

    public void test3_defaultVolume(){
        int volume = 2;
        solo.waitForActivity(MainActivity.class);
        solo.clickOnView(solo.getView("app_bar_settings"));
        if (solo.waitForActivity(SettingsActivity.class)) {
            solo.setProgressBar((ProgressBar)solo.getView("volumeSeekBar"),volume);
            solo.sleep(3000);
            solo.clickOnView(solo.getView("app_bar_save"));
            if(solo.waitForActivity(MainActivity.class)) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                assertEquals(volume, preferences.getInt(SettingsActivity.MAX_ALARM_VOLUME, 8));
            }
        }
    }
    public void test4_weatherRefreshRate(){
        solo.waitForActivity(MainActivity.class);
        solo.clickOnView(solo.getView("app_bar_settings"));
        if (solo.waitForActivity(SettingsActivity.class)) {
            solo.clickOnView(solo.getView("buttonRate"));
            if(solo.waitForDialogToOpen()){
                solo.clickInList(5);
                solo.clickOnText("Done");
                solo.clickOnView(solo.getView("app_bar_save"));
                if(solo.waitForActivity(MainActivity.class)){
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    assertEquals(preferences.getInt(SettingsActivity.SELECTED_INTERVAL,0),5);
                }
            }
        }
    }

    public void test5_defaultRingtone(){
        solo.waitForActivity(MainActivity.class);
        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(SettingsActivity.SELECTED_RINGTONE_URI,null);
        solo.clickOnView(solo.getView("app_bar_settings"));
        if (solo.waitForActivity(SettingsActivity.class)) {
            solo.clickOnView(solo.getView("buttonRingtone"));
            if(solo.waitForDialogToOpen()){
                solo.clickInList(5);
                solo.clickOnText("Done");
                solo.clickOnView(solo.getView("app_bar_save"));
                if(solo.waitForActivity(MainActivity.class)){
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    assertNotNull(preferences.getString(SettingsActivity.SELECTED_RINGTONE_URI, null));
                }
            }
        }
    }

    public void test6_disconnect(){
        String toastMessage = "Your Deezer account is no longer connected to app";
        solo.waitForActivity(MainActivity.class);
        solo.clickOnView(solo.getView("app_bar_settings"));
        if (solo.waitForActivity(SettingsActivity.class)) {
            solo.clickOnView(solo.getView("disconnect"));
            solo.waitForText(toastMessage);
        }
    }

}