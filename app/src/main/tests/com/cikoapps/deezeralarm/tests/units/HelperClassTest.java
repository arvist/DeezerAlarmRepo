package com.cikoapps.deezeralarm.tests.units;

import android.test.ActivityInstrumentationTestCase2;

import com.cikoapps.deezeralarm.activities.MainActivity;
import com.cikoapps.deezeralarm.helpers.HelperClass;

public class HelperClassTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private HelperClass helperClass;

    public HelperClassTest() {
        super(MainActivity.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MainActivity activity = getActivity();
        helperClass = new HelperClass(activity.getApplicationContext());
    }

    public void test_getMonthFromInt() {
        assertTrue("January".equalsIgnoreCase(HelperClass.getMonthFromInt(0)));
        assertTrue("December".equalsIgnoreCase(HelperClass.getMonthFromInt(11)));
        assertNull(HelperClass.getMonthFromInt(-1));
        assertNull(HelperClass.getMonthFromInt(12));
    }

    public void test_round() {
        assertEquals(3.41,HelperClass.round(3.4051,2));
        assertEquals(3.40,HelperClass.round(3.4049,2));
    }

    public void test_timeConversion() {
        assertTrue("1 min 0 sec".equalsIgnoreCase(HelperClass.timeConversion(60)));
        assertTrue("1 h 0 min 0 sec".equalsIgnoreCase(HelperClass.timeConversion(3600)));
        assertTrue("1 h 59 min 59 sec".equalsIgnoreCase(HelperClass.timeConversion(7199)));
    }

    public void test_randomInteger() {
        int randomInt = HelperClass.randomInteger(1,10);
        assertTrue( randomInt > 0 && randomInt < 11);
    }

    public void test_isWifiConnected() {
        assertNotNull(helperClass.isWifiConnected());
    }

    public void test_haveNetworkConnection() {
        assertNotNull(helperClass.haveNetworkConnection());
    }

    public void test_allFalse() {
        assertTrue(HelperClass.allFalse(new boolean[]{false,false,false}));
        assertFalse(HelperClass.allFalse(new boolean[]{false,true,false}));
        assertFalse(HelperClass.allFalse(new boolean[]{true}));
    }

    public void test_oneOrMoreTrue() {
        assertTrue(helperClass.oneOrMoreTrue(new boolean[]{true,false}));
        assertTrue(helperClass.oneOrMoreTrue(new boolean[]{true,true}));
        assertFalse(helperClass.oneOrMoreTrue(new boolean[]{false,false}));
    }

    public void test_isLocationEnabled() {
        assertNotNull(helperClass.isLocationEnabled());
    }
}
