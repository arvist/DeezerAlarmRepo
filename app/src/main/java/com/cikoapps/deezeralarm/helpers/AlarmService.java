package com.cikoapps.deezeralarm.helpers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cikoapps.deezeralarm.activities.AlarmScreenActivity;


public class AlarmService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start AlarmScreenActivity
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmScreenActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(intent);
        getApplication().startActivity(alarmIntent);
        AlarmManagerHelper.setAlarms(this);
        return super.onStartCommand(intent, flags, startId);
    }
}