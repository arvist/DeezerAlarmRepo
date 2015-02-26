package com.cikoapps.deezeralarm.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.cikoapps.deezeralarm.Fragments.DeezerListAlarmFragment;
import com.cikoapps.deezeralarm.Fragments.DeezerRadioAlarmFragment;
import com.cikoapps.deezeralarm.Fragments.RingtoneAlarmFragment;
import com.cikoapps.deezeralarm.HelperClasses.AlarmDBHelper;
import com.cikoapps.deezeralarm.HelperClasses.AlarmManagerHelper;
import com.cikoapps.deezeralarm.HelperClasses.DeezerBase;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.HelperClasses.WeatherDataAsync;
import com.cikoapps.deezeralarm.R;

public class AlarmScreenActivity extends DeezerBase {


    private WakeLock mWakeLock;
    private static final int WAKELOCK_TIMEOUT = 60 * 1000;
    Typeface robotoRegular;
    Typeface robotoItalic;
    boolean metricSystem = true;
    public static final String TAG = "AlarmScreen.java";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginDeezer();
        this.setContentView(R.layout.alarm_screen);
        RelativeLayout mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);
        /* Initialize local variables needed by fragments*/
        int databaseId = getIntent().getIntExtra(AlarmManagerHelper.ID, -1);
        String name = getIntent().getStringExtra(AlarmManagerHelper.NAME);
        String tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);
        int type = getIntent().getIntExtra(AlarmManagerHelper.TYPE, 0);
        long alarmid = getIntent().getLongExtra(AlarmManagerHelper.ALARM_ID, -1);
        boolean turnOff = getIntent().getBooleanExtra(AlarmManagerHelper.ONE_TIME_ALARM, true);
        robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        robotoItalic = Typeface.createFromAsset(getAssets(), "Roboto-Italic.ttf");

        /* Turn off not Repeating alarm in Database */
        Log.e(TAG, tone + " repeat weekley " + turnOff);
        if (!turnOff && databaseId != -1) {
            Log.e(TAG, "Turning off alarm " + tone);
            AlarmDBHelper dataBaseHelper = new AlarmDBHelper(getApplicationContext());
            AlarmManagerHelper.cancelAlarms(this);
            dataBaseHelper.updateIsEnabled(databaseId, false);
            AlarmManagerHelper.setAlarms(this);
        }

        /* Replace fragment by alarm appropriate fragment depending on alarm type */
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        loginDeezer();
        if ((type == 1 || type == 2) && alarmid != -1) {
            ft.add(R.id.alarm_bottom_fragment, new DeezerListAlarmFragment(alarmid, type));
        } else if ((type == 3 || type == 4) && alarmid != -1) {
            ft.add(R.id.alarm_bottom_fragment, new DeezerRadioAlarmFragment(alarmid, type));
        } else {
            ft.add(R.id.alarm_bottom_fragment, new RingtoneAlarmFragment(name, tone));
        }
        ft.commit();

        /* Ensure wakelock release */
        Runnable releaseWakelock = new Runnable() {

            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        };
        new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);

        /* Fill Calendar data and download current weather data  */
        HelperClass helperClass = new HelperClass(this);
        double[] coordinates = helperClass.getGPS();
        double latitude = coordinates[0];
        double longitude = coordinates[1];

        if (helperClass.haveNetworkConnection()) {
            new WeatherDataAsync(mainTopLayout, true, true, latitude, longitude, this).execute();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        // Set the window to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Acquire wakelock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.i(TAG, "Wakelock aquired!!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }


}