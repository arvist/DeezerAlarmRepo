package com.cikoapps.deezeralarm.tests.suite;

import com.cikoapps.deezeralarm.tests.activities.AddAlarmActivityTest;
import com.cikoapps.deezeralarm.tests.activities.AlarmScreenActivityDeezerTest;
import com.cikoapps.deezeralarm.tests.activities.AlarmScreenActivityRingtoneTest;
import com.cikoapps.deezeralarm.tests.activities.EditAlarmActivityTest;
import com.cikoapps.deezeralarm.tests.activities.QuoteActivityTest;
import com.cikoapps.deezeralarm.tests.activities.RingtoneActivityTest;
import com.cikoapps.deezeralarm.tests.scenarios.AlarmScenariosTest;
import com.cikoapps.deezeralarm.tests.scenarios.SettingsScenariosMainActivityTest;
import com.cikoapps.deezeralarm.tests.units.HelperClassTest;

import junit.framework.TestSuite;


public class suite extends TestSuite {

    public static TestSuite suite() {
        Class[] testClasses = {AlarmScenariosTest.class,  AddAlarmActivityTest.class, AlarmScreenActivityDeezerTest.class,
                AlarmScreenActivityRingtoneTest.class, EditAlarmActivityTest.class, QuoteActivityTest.class, RingtoneActivityTest.class,
                SettingsActivityTest.class, HelperClassTest.class, QuoteActivityTest.class, SettingsScenariosMainActivityTest.class, AboutActivityTest.class};

        TestSuite testSuite = new TestSuite(testClasses);
        return testSuite;
    }
}
