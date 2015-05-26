package com.cikoapps.deezeralarm.tests.activities;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.cikoapps.deezeralarm.activities.AlarmScreenActivity;
import com.cikoapps.deezeralarm.activities.QuoteActivity;
import com.cikoapps.deezeralarm.helpers.AlarmManagerHelper;
import com.robotium.solo.Solo;


public class AlarmScreenActivityDeezerTest extends ActivityInstrumentationTestCase2<AlarmScreenActivity> {

    private Solo solo;

    public AlarmScreenActivityDeezerTest() {
        super(AlarmScreenActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public AlarmScreenActivity getActivity() {
        Intent intent = new Intent(getInstrumentation()
                .getTargetContext(), AlarmScreenActivity.class);
        String alarmName = "Robotium Test Deezer";
        intent.putExtra(AlarmManagerHelper.NAME, alarmName);
        intent.putExtra(AlarmManagerHelper.TYPE, 2);
        intent.putExtra(AlarmManagerHelper.ALARM_ID, (long) 302127);

        intent.putExtra(AlarmManagerHelper.ONE_TIME_ALARM, false);
        setActivityIntent(intent);
        return super.getActivity();
    }

    public void test0_deezerAlbum() {
        if (solo.waitForActivity(AlarmScreenActivity.class)) {
            solo.clickOnView(solo.getView("stopAlarmButton"));
            solo.waitForActivity(QuoteActivity.class);
        }
    }
}
