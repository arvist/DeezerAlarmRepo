package com.cikoapps.deezeralarm.models;

import android.content.Context;

import com.cikoapps.deezeralarm.HelperClasses.AlarmDBHelper;

public class Alarm {
    public static final String TITLE = "title";
    public static final String TONE_NAME = "alarmToneName";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String TYPE = "type";
    public static final String REPEATING_DAYS = "repeatingDays";
    public static final String ALARM_ID = "id";
    public static final String PART_OF_DAY = "partOfDay";
    public static final String ALARM_URI = "uri";
    public static final String DEEZER_RINGTONE_ID = "deezerRingtoneId";
    public static final String ENABLED = "enabled";
    public static final String ARTIST = "artist";
    public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;
    public final boolean[] repeatingDays;
    public String title;
    public int id = 0;
    public int hour;
    public int minute;
    public boolean enabled;
    public boolean repeatWeekly;
    public String alarmTone;
    public long alarmid = 0;
    public int type;
    public String artist;
    public boolean usClock;
    public String partOfDay = ""; //AM/FM
    public String alarmToneName;

    public Alarm() {
        repeatingDays = new boolean[7];
    }

    public Alarm(String title, int id, int hour, int minute, boolean enabled, boolean[] repeatingDays,
                 boolean repeatWeekly, String alarmTone, String artist, long alarmid, int type, String alarmToneName) {
        this.title = title;
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.repeatingDays = repeatingDays;
        this.repeatWeekly = repeatWeekly;
        this.alarmTone = alarmTone;
        this.alarmid = alarmid;
        this.artist = artist;
        this.type = type;
        this.alarmToneName = alarmToneName;
    }


    public Alarm(String title, int hour, int minute, boolean enabled, boolean[] repeatingDays,
                 boolean repeatWeekly, String alarmTone, long alarmid, int type, String alarmToneName, String artist) {
        this.title = title;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.repeatingDays = repeatingDays;
        this.repeatWeekly = repeatWeekly;
        this.alarmTone = alarmTone;
        this.artist = artist;
        this.alarmid = alarmid;
        this.type = type;
        this.alarmToneName = alarmToneName;
    }

    public boolean getRepeatingDay(int dayOfWeek) {
        return repeatingDays[dayOfWeek];
    }

    public void insertIntoDataBase(Context context) {
        AlarmDBHelper alarmDBHelper = new AlarmDBHelper(context);
        String repeatDays = repeatingDaysToString();
        alarmDBHelper.insertAlarm(title, hour, minute, repeatDays, repeatWeekly, alarmToneName, artist, alarmTone,
                alarmid, type, enabled);
        alarmDBHelper.close();

    }

    private String repeatingDaysToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (boolean b : repeatingDays) {
            stringBuilder.append(b);
            stringBuilder.append(",");
        }
        return stringBuilder.toString();

    }
}

