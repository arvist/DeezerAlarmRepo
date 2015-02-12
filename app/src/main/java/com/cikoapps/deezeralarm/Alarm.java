package com.cikoapps.deezeralarm;

import android.net.Uri;

/**
 * Created by arvis.taurenis on 2/10/2015.
 */
public class Alarm {
    public long id;
    public int timeHour;
    public int timeMinute;
    String title;
    public boolean isEnabled;
    boolean[]  repeatingDays;
    public boolean repeatWeekly;
    String time;
    public Uri alarmTone;

    public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRDIAY = 5;
    public static final int SATURDAY = 6;

    public Alarm() {
        repeatingDays = new boolean[7];
    }

    public void setRepeatingDay(int dayOfWeek, boolean value) {
        repeatingDays[dayOfWeek] = value;
    }

    public boolean getRepeatingDay(int dayOfWeek) {
        return repeatingDays[dayOfWeek];
    }

}

