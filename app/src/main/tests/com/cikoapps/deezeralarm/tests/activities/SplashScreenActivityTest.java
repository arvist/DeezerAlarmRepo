package com.cikoapps.deezeralarm.tests.activities;

import android.test.ActivityInstrumentationTestCase2;

import com.cikoapps.deezeralarm.activities.MainActivity;
import com.cikoapps.deezeralarm.activities.SplashScreenActivity;
import com.robotium.solo.Solo;

public class SplashScreenActivityTest extends ActivityInstrumentationTestCase2<SplashScreenActivity> {
    private Solo solo;

    public SplashScreenActivityTest() {
        super(SplashScreenActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }
    public void test0_openMainActivity(){
        solo.waitForActivity(MainActivity.class,3*1000);
    }
}
