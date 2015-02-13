package com.cikoapps.deezeralarm;

import android.provider.BaseColumns;

/**
 * Created by arvis.taurenis on 2/13/2015.
 */
public final class AlarmContract {


    public static final String TABLE_NAME = "alarm";
    public static final String COLUMN_NAME_ALARM_NAME = "title";
    public static final String COLUMN_NAME_ALARM_TIME_HOUR = "hour";
    public static final String COLUMN_NAME_ALARM_TIME_MINUTE = "minute";
    public static final String COLUMN_NAME_ALARM_REPEAT_DAYS = "days";
    public static final String COLUMN_NAME_ALARM_REPEAT_WEEKLY = "weekly";
    public static final String COLUMN_NAME_ALARM_TONE = "tone";
    public static final String COLUMN_NAME_ALARM_ENABLED = "isEnabled";
    public static final String _ID = "_id";
    public static final String COLUMN_NAME_SONG_ID = "sonngId" ;
    public static final String COLUMN_NAME_PLAYLIST_ID = "playlistId";
    public static final String COLUMN_NAME_RADIO_ID = "radioId";
    public static final String COLUMN_NAME_ALBUM_ID = "albumId";
    public static final String COLUMN_NAME_ARTIST_ID = "artistId";
    public static final String COLUMN_NAME_ALARM_TONE_NAME = "alarmToneName";
}
