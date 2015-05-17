package com.cikoapps.deezeralarm.tests.activities;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.RadioButton;

import com.cikoapps.deezeralarm.activities.RingtoneActivity;
import com.cikoapps.deezeralarm.activities.SettingsActivity;
import com.robotium.solo.Solo;

public class RingtoneActivityTest extends ActivityInstrumentationTestCase2<RingtoneActivity> {
    private Solo solo;

    public RingtoneActivityTest() {
        super(RingtoneActivity.class);
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

    public void test0_appBarActions() {
        if (solo.waitForActivity(RingtoneActivity.class)) {
            solo.clickOnView(solo.getView("app_bar_settings"));
            if (solo.waitForActivity(SettingsActivity.class)) {
                solo.clickOnView(solo.getView("app_bar_back_btn"));
                if (solo.waitForActivity(RingtoneActivity.class)) {
                    solo.clickOnView(solo.getView("app_bar_settings"));
                }
            }
        }
    }

    public void test1_fragments() {
        if (solo.waitForActivity(RingtoneActivity.class)) {
            solo.waitForView(solo.getView("deezerPlaylists"));
            solo.waitForView(solo.getView("deezerArtists"));
            solo.waitForView(solo.getView("deviceRingtone"));
            solo.waitForView(solo.getView("deezerRadio"));
            solo.waitForView(solo.getView("deezerAlbums"));
        }
    }

    public void test2_buttons() {
        if (solo.waitForActivity(RingtoneActivity.class)) {
            solo.clickOnText("PLAYLISTS");
            solo.clickOnText("DEVICE RINGTONES");
            solo.clickOnText("ALBUMS");
            solo.clickOnText("FAVORITE ARTISTS");
            solo.clickOnText("RADIO");
            solo.clickOnView(solo.getView("confirmRingtone"));
        }
    }

    public void test3_radioButtonsSelect() {
        if (solo.waitForActivity(RingtoneActivity.class)) {
            solo.clickOnView(solo.getView("deviceRingtoneRadioButton", 3));
            solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
            solo.clickOnView(solo.getView("playlistChecked", 3));
            solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.LEFT);
            solo.sleep(2000);
            RadioButton deviceRingtoneItemRadioButton = (RadioButton) solo.getView("deviceRingtoneRadioButton", 3);
            assertFalse(deviceRingtoneItemRadioButton.isChecked());
            solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
            solo.sleep(1000);
            solo.clickOnView(solo.getView("playlistChecked", 3));
            solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
            solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.RIGHT);
            solo.clickOnView(solo.getView("artistRadioButton", 3));
            solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.LEFT);
            solo.scrollViewToSide(solo.getView("ringtone_pager"), Solo.LEFT);
        }
    }
}