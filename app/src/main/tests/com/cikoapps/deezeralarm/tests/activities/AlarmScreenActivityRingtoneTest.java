package com.cikoapps.deezeralarm.tests.activities;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.cikoapps.deezeralarm.activities.AlarmScreenActivity;
import com.cikoapps.deezeralarm.activities.MainActivity;
import com.cikoapps.deezeralarm.helpers.AlarmManagerHelper;
import com.robotium.solo.Solo;

/**
 * Created by Arvis on 5/11/2015.
 */
public class AlarmScreenActivityRingtoneTest extends ActivityInstrumentationTestCase2<AlarmScreenActivity> {

    Intent intent;
    private Solo solo;
    String alarmName = "Robotium Test";

    public AlarmScreenActivityRingtoneTest() {
        super(AlarmScreenActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public AlarmScreenActivity getActivity() {
        intent = new Intent(getInstrumentation()
                .getTargetContext(), AlarmScreenActivity.class);
        intent.putExtra(AlarmManagerHelper.NAME, alarmName);
        intent.putExtra(AlarmManagerHelper.TONE, "");
        // Type = 0, modinātājs ar signālu no ierīces
        intent.putExtra(AlarmManagerHelper.TYPE, 0);
        // ID = -1, lai aktivitāte nemēģina datubāzē atzīmēt modinātāju kā izslēgtu pēc ID.
        intent.putExtra(AlarmManagerHelper.ID, -1);
        intent.putExtra(AlarmManagerHelper.ONE_TIME_ALARM, false);
        // Nav svarīgs, jo attiecas uz ID deezer servisā.
        intent.putExtra(AlarmManagerHelper.ALARM_ID, -1);
        setActivityIntent(intent);
        return super.getActivity();
    }

    public void test0_DeviceRingtoneTest() {
        if (solo.waitForActivity(AlarmScreenActivity.class)) {
            assertTrue(((TextView) solo.getView("alarmTitleTextView")).getText().toString().equalsIgnoreCase(alarmName));
            assertNotNull(((TextView) solo.getView("quoteTextView")).getText().toString());
            assertNotNull(((TextView) solo.getView("quoteAuthorTextView")).getText().toString());
            solo.clickOnView(solo.getView("stopButton"));
            solo.waitForActivity(MainActivity.class);
        }
    }
}
