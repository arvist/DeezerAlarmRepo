package com.cikoapps.deezeralarm.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.fragments.DeezerListAlarmFragment;
import com.cikoapps.deezeralarm.fragments.DeezerRadioAlarmFragment;
import com.cikoapps.deezeralarm.fragments.RingtoneAlarmFragment;
import com.cikoapps.deezeralarm.fragments.WeatherFragment;
import com.cikoapps.deezeralarm.helpers.AlarmDatabaseAccessor;
import com.cikoapps.deezeralarm.helpers.AlarmManagerHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AlarmScreenActivity extends Activity {

    private static final int WAKELOCK_TIMEOUT = 3 * 60 * 1000;
    private static final String TAG = "AlarmScreen.java";
    private WakeLock mWakeLock;
    private Context context;
    private long alarmid;
    private String name;
    private String tone;
    private int type;
    private boolean wifiBool;
     private AdView adView;

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Set activity layout */
        this.setContentView(R.layout.alarm_screen_activity);
         /* Initialize global variables */
        int databaseId = getIntent().getIntExtra(AlarmManagerHelper.ID, -1);
        name = getIntent().getStringExtra(AlarmManagerHelper.NAME);
        tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);
        type = getIntent().getIntExtra(AlarmManagerHelper.TYPE, 0);
        alarmid = getIntent().getLongExtra(AlarmManagerHelper.ALARM_ID, -1);
        boolean turnOff = getIntent().getBooleanExtra(AlarmManagerHelper.ONE_TIME_ALARM, true);
        this.context = this;
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
         wifiBool = preferences.getBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);


        /* Mark alarm as disabled in database */
        if (!turnOff && databaseId != -1) {
            AlarmDatabaseAccessor dataBaseHelper = new AlarmDatabaseAccessor(getApplicationContext());
            AlarmManagerHelper.cancelAlarms(this);
            dataBaseHelper.updateIsEnabled(databaseId, false);
            AlarmManagerHelper.setAlarms(this);
        }
        /* Allow device to lock screen after WAKELOCK_TIMEOUT  */
        releaseWakeLock();


        /* Insert either list player, radio player of device ringtone alarm fragment */
        applyAlarmFragment();

        /* Uncomment to enable Google Ads*/
        /*showAds();*/
    }

    private void showAds() {
        adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
/*                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("abc")*/
                .build();
        adView.loadAd(adRequest);
    }




    private void releaseWakeLock() {
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
    }

    private void applyAlarmFragment() {

    /*  Aktivitātē tiek ievietots atbilstošais modinātāja tipa fragments */
        FragmentManager fm = getFragmentManager();
        Log.e("BANANA",type + " Tha TYPE " + alarmid );
        FragmentTransaction ft = fm.beginTransaction();
        if ((type == 1 || type == 2) && alarmid != -1) {

            ft.add(R.id.weather_fragment, new WeatherFragment(0, 0, null, context));
            ft.add(R.id.alarm_bottom_fragment, new DeezerListAlarmFragment(alarmid, type, wifiBool, context, getApplication()));
        } else if ((type == 3 || type == 4) && alarmid != -1) {
            ft.add(R.id.weather_fragment, new WeatherFragment(0, 0, null, context));
            ft.add(R.id.alarm_bottom_fragment, new DeezerRadioAlarmFragment(alarmid, type, wifiBool, context, getApplication()));
        } else {
            ft.add(R.id.weather_fragment, new WeatherFragment(0, 0, null, context));
            ft.add(R.id.alarm_bottom_fragment, new RingtoneAlarmFragment(name, tone, context));

        }
        ft.commit();
    }





    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
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
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    public void finishApp() {
        finish();
    }

}