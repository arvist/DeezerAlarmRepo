package com.cikoapps.deezeralarm.Activities;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cikoapps.deezeralarm.Fragments.DeezerListAlarmFragment;
import com.cikoapps.deezeralarm.Fragments.DeezerRadioAlarmFragment;
import com.cikoapps.deezeralarm.Fragments.RingtoneAlarmFragment;
import com.cikoapps.deezeralarm.R;

import com.cikoapps.deezeralarm.helpers.AlarmDatabaseAccessor;
import com.cikoapps.deezeralarm.helpers.AlarmManagerHelper;
import com.cikoapps.deezeralarm.helpers.HelperClass;
import com.cikoapps.deezeralarm.helpers.Location;
import com.cikoapps.deezeralarm.helpers.WeatherDataAsync;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmScreenActivity extends Activity {

    private static final int WAKELOCK_TIMEOUT = 3 * 60 * 1000;
    private static final String TAG = "AlarmScreen.java";
    private WakeLock mWakeLock;
    private Context context;
    private Location location;
    private ImageButton refreshButton;
    private RelativeLayout mainTopLayout;
    private long alarmid;
    private String name;
    private String tone;
    private int type;
    private boolean wifiBool;
    private WeatherDataAsync weatherDataAsync;
    private Timer timer;
    private AdView adView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Set activity layout */
        this.setContentView(R.layout.alarm_screen_activity);
        mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);
        /* Initialize global variables */
        int databaseId = getIntent().getIntExtra(AlarmManagerHelper.ID, -1);
        name = getIntent().getStringExtra(AlarmManagerHelper.NAME);
        tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);
        type = getIntent().getIntExtra(AlarmManagerHelper.TYPE, 0);
        alarmid = getIntent().getLongExtra(AlarmManagerHelper.ALARM_ID, -1);
        boolean turnOff = getIntent().getBooleanExtra(AlarmManagerHelper.ONE_TIME_ALARM, true);
        this.context = this;
        refreshButton = (ImageButton) mainTopLayout.findViewById(R.id.refreshButton);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        weatherDataAsync = new WeatherDataAsync(mainTopLayout, -1, -1, null, context);
        wifiBool = preferences.getBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);

        /* Enable weather refresh button */
        refreshOnClick();
        weatherDataAsync.setFromSharedPreferences();

        /* Mark alarm as disabled in database */
        if (!turnOff && databaseId != -1) {
            AlarmDatabaseAccessor dataBaseHelper = new AlarmDatabaseAccessor(getApplicationContext());
            AlarmManagerHelper.cancelAlarms(this);
            dataBaseHelper.updateIsEnabled(databaseId, false);
            AlarmManagerHelper.setAlarms(this);
        }
        /* Allow device to lock screen after WAKELOCK_TIMEOUT  */
        releaseWakeLock();

        /*  Update weather layout every minute */
        updateDisplay();
        updateWeatherDataOnRefreshTime();

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


    private void updateDisplay() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weatherDataAsync.setFromSharedPreferences();
                        updateWeatherDataOnRefreshTime();
                    }
                });


            }
        }, 0, 1 * 60 * 1000);
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


    /* Replace fragment by alarm appropriate fragment depending on alarm type */
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if ((type == 1 || type == 2) && alarmid != -1) {
            ft.add(R.id.alarm_bottom_fragment, new DeezerListAlarmFragment(alarmid, type, wifiBool, context, getApplication()));
        } else if ((type == 3 || type == 4) && alarmid != -1) {
            ft.add(R.id.alarm_bottom_fragment, new DeezerRadioAlarmFragment(alarmid, type, wifiBool, context, getApplication()));
        } else {
            ft.add(R.id.alarm_bottom_fragment, new RingtoneAlarmFragment(name, tone, context));
        }
        ft.commit();
    }

    private void updateWeatherDataOnRefreshTime() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int refreshTime = preferences.getInt(SettingsActivity.SELECTED_INTERVAL, 1);
        if (refreshTime != 12) {
            long lastUpdateTimeMillis = preferences.getLong(WeatherDataAsync.TIME_UPDATED, 0);
            boolean onlyWiFiUpdate = preferences.getBoolean(SettingsActivity.ONLY_WIFI_SELECTED, false);
            if ((onlyWiFiUpdate && new HelperClass(context).isWifiConnected()) || !onlyWiFiUpdate) {
                Calendar deleteCalendar = Calendar.getInstance();
                deleteCalendar.setTimeInMillis(lastUpdateTimeMillis);
                int milliSecondsRefreshTime = (refreshTime + 1) * 5 * 60000;
                Calendar calendar = Calendar.getInstance();
                long currentMillis = calendar.getTimeInMillis();
                if ((currentMillis - lastUpdateTimeMillis) > milliSecondsRefreshTime) {
                    location = new Location(this, mainTopLayout, null);
                    location.buildGoogleApiClient();
                }
            }
        }
    }

    private void refreshOnClick() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new HelperClass(context).haveNetworkConnection()) {
                    if (location != null) {
                        location.reconnectGoogleApiClient();
                    } else {
                        location = new Location(context, mainTopLayout, null);
                        location.buildGoogleApiClient();
                    }
                } else {
                    Toast.makeText(context, "No network connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        updateWeatherDataOnRefreshTime();
        updateDisplay();
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
        timer.cancel();
        timer.purge();
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