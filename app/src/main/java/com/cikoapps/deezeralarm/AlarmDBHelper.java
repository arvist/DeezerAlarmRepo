package com.cikoapps.deezeralarm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.sql.SQLException;

public class AlarmDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    private static String DB_PATH = "/data/data/com.cikoapps.deezeralarm/databases/";
    private static String DB_NAME = "deezerAlarmClock";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    public AlarmDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    public boolean checkForData() {
        boolean isDataPresent = checkDataBase();
        if (isDataPresent) {
            return true;
        } else {
            createDataBase();
        }
        return true;
    }

    private void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
            myDataBase = myContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
            myDataBase.execSQL(SQL_CREATE_ALARM);
            Log.e(" ", SQL_CREATE_ALARM);
        }
        close();
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    boolean checkDataBase() {
        File dbFile = myContext.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = myContext.openOrCreateDatabase(myPath, SQLiteDatabase.OPEN_READONLY, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static final String SQL_CREATE_ALARM =
            "CREATE TABLE " + AlarmContract.TABLE_NAME + " (" + "\"" +
                    AlarmContract._ID + "\"" + " INTEGER PRIMARY KEY AUTOINCREMENT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_NAME + "\"" + " TEXT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_TIME_HOUR + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_TIME_MINUTE + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_REPEAT_DAYS + "\"" + " TEXT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_REPEAT_WEEKLY + "\"" + " BOOLEAN," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_TONE_NAME + "\"" + " TEXT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_TONE + "\"" + " TEXT," + "\"" +
                    AlarmContract.COLUMN_NAME_PLAYLIST_ID + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_SONG_ID + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_RADIO_ID + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALBUM_ID + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ARTIST_ID + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_ENABLED + "\"" + " BOOLEAN" + " )";


    public void insertAlarm(String title, int hour, int minute, String repeatDays, boolean repeatWeekly, String alarmToneName,
                            String alarmTone, int playlistId, int songId, int radioId, int albumId, int artistId, boolean enabled) {
        try {
            openDataBase();

            String insertQuery = "Insert into " + AlarmContract.TABLE_NAME + " ( " +
                    AlarmContract.COLUMN_NAME_ALARM_NAME + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_TIME_HOUR + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_TIME_MINUTE + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_REPEAT_DAYS + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_REPEAT_WEEKLY + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_TONE_NAME + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_TONE + " , " +
                    AlarmContract.COLUMN_NAME_PLAYLIST_ID + " , " +
                    AlarmContract.COLUMN_NAME_SONG_ID + " , " +
                    AlarmContract.COLUMN_NAME_RADIO_ID + " , " +
                    AlarmContract.COLUMN_NAME_ALBUM_ID + " , " +
                    AlarmContract.COLUMN_NAME_ARTIST_ID + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_ENABLED + " ) values ( " +
                    "\"" + title + "\"" + " , " +
                    hour + " , " +
                    minute + " , " +
                    "\"" + repeatDays + "\"" + " , " +
                    "\"" + repeatWeekly + "\"" + " , " +
                    "\"" + alarmToneName + "\"" + " , " +
                    "\"" + alarmTone + "\"" + " , " +
                    playlistId + " , " +
                    songId + " , " +
                    radioId + " , " +
                    albumId + " , " +
                    artistId + " , " +
                    "\"" + enabled + "\"" + " ); ";
            Log.e(" ", insertQuery);

            myDataBase.execSQL(insertQuery);
            close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static final String SQL_DELETE_ALARM =
            "DROP TABLE IF EXISTS " + AlarmContract.TABLE_NAME;


}
