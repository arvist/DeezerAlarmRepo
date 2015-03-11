package com.cikoapps.deezeralarm.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import com.cikoapps.deezeralarm.HelperClasses.AlarmDBHelper;
import com.cikoapps.deezeralarm.HelperClasses.AlarmManagerHelper;
import com.cikoapps.deezeralarm.HelperClasses.DeezerBase;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
import com.cikoapps.deezeralarm.HelperClasses.MyLocation;
import com.cikoapps.deezeralarm.HelperClasses.WeatherDataAsync;
import com.cikoapps.deezeralarm.R;

import java.util.Calendar;

public class AlarmScreenActivity extends DeezerBase {

    private WakeLock mWakeLock;
    private static final int WAKELOCK_TIMEOUT = 60 * 1000;
    Typeface robotoRegular;
    Typeface robotoItalic;
    public static final String TAG = "AlarmScreen.java";
    WeatherDataAsync weatherDataAsync;
    Context context;
    MyLocation myLocation;
    private ImageButton refreshButton;
    RelativeLayout mainTopLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.alarm_screen);
        mainTopLayout = (RelativeLayout) findViewById(R.id.mainTopLayout);
        /* Initialize local variables needed by fragments*/
        int databaseId = getIntent().getIntExtra(AlarmManagerHelper.ID, -1);
        String name = getIntent().getStringExtra(AlarmManagerHelper.NAME);
        String tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);
        int type = getIntent().getIntExtra(AlarmManagerHelper.TYPE, 0);
        long alarmid = getIntent().getLongExtra(AlarmManagerHelper.ALARM_ID, -1);
        boolean turnOff = getIntent().getBooleanExtra(AlarmManagerHelper.ONE_TIME_ALARM, true);
        robotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        robotoItalic = Typeface.createFromAsset(getAssets(), "Roboto-Italic.ttf");
        this.context = this;
        refreshButton = (ImageButton) mainTopLayout.findViewById(R.id.refreshButton);
        refresh();
        /**********************************************/
        weatherDataAsync = new WeatherDataAsync(mainTopLayout, -1, -1, null, context);
        weatherDataAsync.setFromSharedPreferences();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wifiBool = preferences.getBoolean("wifiSelected", false);
        /* Turn off not Repeating alarm in Database */
        if (!turnOff && databaseId != -1) {
            AlarmDBHelper dataBaseHelper = new AlarmDBHelper(getApplicationContext());
            AlarmManagerHelper.cancelAlarms(this);
            dataBaseHelper.updateIsEnabled(databaseId, false);
            AlarmManagerHelper.setAlarms(this);
        }
        /* Replace fragment by alarm appropriate fragment depending on alarm type */
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if ((type == 1 || type == 2) && alarmid != -1) {
            ft.add(R.id.alarm_bottom_fragment, new DeezerListAlarmFragment(alarmid, type, wifiBool, context));
        } else if ((type == 3 || type == 4) && alarmid != -1) {
            ft.add(R.id.alarm_bottom_fragment, new DeezerRadioAlarmFragment(alarmid, type, wifiBool, context));
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
        updateWeatherData();
    }

    private void updateWeatherData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int refreshTime = preferences.getInt("selectedInterval", 1);
        if (refreshTime != 12) {
            long lastUpdateTimeMillis = preferences.getLong("time", 0);
            boolean onlyWiFiUpdate = preferences.getBoolean("wifiSelected", false);
            if ((onlyWiFiUpdate && new HelperClass(context).isWifiConnected()) || !onlyWiFiUpdate) {
                Calendar deleteCalendar = Calendar.getInstance();
                deleteCalendar.setTimeInMillis(lastUpdateTimeMillis);
                int milliSecondsRefreshTime = (refreshTime + 1) * 5 * 60000;
                Calendar calendar = Calendar.getInstance();
                long currentMillis = calendar.getTimeInMillis();
                if ((currentMillis - lastUpdateTimeMillis) > milliSecondsRefreshTime) {
                    myLocation = new MyLocation(this, mainTopLayout, null);
                    myLocation.buildGoogleApiClient();
                }
            }
        }
    }

    private void refresh() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new HelperClass(context).haveNetworkConnection()) {
                    if (myLocation != null) {
                        myLocation.reconnectGoogleApiClient();
                    } else {
                        myLocation = new MyLocation(context, mainTopLayout, null);
                        myLocation.buildGoogleApiClient();
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
        updateWeatherData();
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
    }


    public void finishApp() {
        finish();
    }
}