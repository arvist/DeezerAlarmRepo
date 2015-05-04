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
import com.cikoapps.deezeralarm.activities.SettingsActivity;

/**
 * Created by Arvis on 5/4/2015.
 */
public class SettingsActivityTest extends ActivityInstrumentationTestCase2<SettingsActivity> {

    private SettingsActivity activity;
    private RadioButton windRadioButton;
    private RadioButton useOnlyWiFiButton;
    private TextView disconnectDeezerAccountTextView;
    private TextView textTimeSelected;
    private TextView textRingtoneInfo;
    private ImageButton editDefaultRingtoneButton;
    private ImageButton refreshWeatherDataEditButton;
    private RelativeLayout aboutLayout;
    private SeekBar volumeSeekBar;
    private RadioButton tempRadioButton;

    public SettingsActivityTest() {
        super(SettingsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        tempRadioButton = (RadioButton) activity.findViewById(R.id.layoutTemp).findViewById(R.id.radioButtonTemp);
        windRadioButton = (RadioButton) activity.findViewById(R.id.layoutWind).findViewById(R.id.radioButtonWind);
        useOnlyWiFiButton = (RadioButton) activity.findViewById(R.id.layoutWiFi).findViewById(R.id.radioButtonWifi);
        disconnectDeezerAccountTextView = (TextView) activity.findViewById(R.id.layoutAccount).findViewById(R.id.disconnect);
        textTimeSelected = (TextView) activity.findViewById(R.id.layoutRefresh).findViewById(R.id.textTimeSelected);
        textRingtoneInfo = (TextView) activity.findViewById(R.id.layoutRingtone).findViewById(R.id.textRingtoneInfo);
        editDefaultRingtoneButton = (ImageButton) activity.findViewById(R.id.layoutRingtone).findViewById(R.id.buttonRingtone);
        refreshWeatherDataEditButton = (ImageButton) activity.findViewById(R.id.layoutRefresh).findViewById(R.id.buttonRate);
        aboutLayout = (RelativeLayout) activity.findViewById(R.id.about);
        volumeSeekBar = (SeekBar) activity.findViewById(R.id.layoutVolume).findViewById(R.id.volumeSeekBar);
    }

    @SmallTest
    public void testViewsNotNull() {
        assertNotNull(tempRadioButton);
        assertNotNull(windRadioButton);
        assertNotNull(useOnlyWiFiButton);
        assertNotNull(disconnectDeezerAccountTextView);
        assertNotNull(textTimeSelected);
        assertNotNull(textRingtoneInfo);
        assertNotNull(editDefaultRingtoneButton);
        assertNotNull(refreshWeatherDataEditButton);
        assertNotNull(aboutLayout);
        assertNotNull(volumeSeekBar);
    }

    @UiThreadTest
    public void testRadioButtonClicks() {

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
    public void testSaveValuesToSharedPreferences() {

        // Iestata visas radio pogas izslēgtas
        SharedPreferences.Editor sharedPreferencesEditor = activity.getSharedPreferencesEditor();
        sharedPreferencesEditor.putBoolean(SettingsActivity.TEMP_FAHRENHEIT_BOOLEAN, false);
        sharedPreferencesEditor.putBoolean(SettingsActivity.WIND_MILES_BOOLEAN, false);
        sharedPreferencesEditor.putBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);
        sharedPreferencesEditor.putInt(SettingsActivity.MAX_ALARM_VOLUME, 8);

        sharedPreferencesEditor.commit();
        activity.setSavedValuesFromSharedPreferences();

        // Nospiež uz visām raido pogām, lai visas būtu ieslēgtas - TRUE
        tempRadioButton.performClick();
        windRadioButton.performClick();
        useOnlyWiFiButton.performClick();
        sharedPreferencesEditor.putInt(SettingsActivity.MAX_ALARM_VOLUME, 10);

        // Saglabā vērtības
        activity.getSharedPreferencesEditor().commit();

        // Nospiež visas radio pogas vēlreiz, lai izmainītu vērtību uz FALSE
        tempRadioButton.performClick();
        windRadioButton.performClick();
        useOnlyWiFiButton.performClick();
        activity.setSelectedVolume(0);


        // Iestata saglabātās vērtības
        activity.setSavedValuesFromSharedPreferences();

        // Pārbauda vai visas vērtības ir TRUE
        assertTrue(tempRadioButton.isChecked());
        assertTrue(windRadioButton.isChecked());
        assertTrue(useOnlyWiFiButton.isChecked());
        assertEquals(10, activity.getSelectedVolume());
    }


}
