package com.cikoapps.deezeralarm;

import android.content.Context;
import android.net.Uri;

/**
 * Created by arvis.taurenis on 2/10/2015.
 */
public class Alarm {
    String title;
    public long id = 0;
    public int hour;
    public int minute;
    public boolean enabled;
    public boolean[] repeatingDays;
    public boolean repeatWeekly;
    public Uri alarmTone;
    public int playlistId = 0;
    public int songId = 0;
    public int albumId = 0;
    public int artistId = 0;
    public int radioId = 0;

    String repeatDays;
    String alarmToneName;

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
/*
(String title, int hour, int minute, boolean enabled, boolean[] repeatingDays,
                 boolean repeatWeekly, Uri alarmTone, int playlistId, int songId, int albumId, int artistId,
                 int radioId, String alarmToneName)
 */

    public void insertIntoDataBase(Context context) {
        AlarmDBHelper alarmDBHelper = new AlarmDBHelper(context);
        this.repeatDays = repeatingDaysToString();
        alarmDBHelper.insertAlarm(title, hour, minute, repeatDays, repeatWeekly, alarmToneName, alarmTone.toString(),
                playlistId, songId, radioId, albumId, artistId, enabled);
        alarmDBHelper.close();

    }

    public Alarm(String title, long id, int hour, int minute, boolean enabled, boolean[] repeatingDays,
                 boolean repeatWeekly, Uri alarmTone, int playlistId, int songId, int albumId, int artistId,
                 int radioId, String alarmToneName) {
        this.title = title;
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.repeatingDays = repeatingDays;
        this.repeatWeekly = repeatWeekly;
        this.alarmTone = alarmTone;
        this.playlistId = playlistId;
        this.songId = songId;
        this.albumId = albumId;
        this.artistId = artistId;
        this.radioId = radioId;
        this.alarmToneName = alarmToneName;
    }
    public Alarm(String title, int hour, int minute, boolean enabled, boolean[] repeatingDays,
                 boolean repeatWeekly, Uri alarmTone, int playlistId, int songId, int albumId, int artistId,
                 int radioId, String alarmToneName) {
        this.title = title;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.repeatingDays = repeatingDays;
        this.repeatWeekly = repeatWeekly;
        this.alarmTone = alarmTone;
        this.playlistId = playlistId;
        this.songId = songId;
        this.albumId = albumId;
        this.artistId = artistId;
        this.radioId = radioId;
        this.alarmToneName = alarmToneName;
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

