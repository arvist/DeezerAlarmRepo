package com.cikoapps.deezeralarm.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cikoapps.deezeralarm.models.Alarm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlarmDatabaseAccessor extends SQLiteOpenHelper {


    public static final String TABLE_NAME = "alarm";
    public static final String COLUMN_NAME_ALARM_NAME = "title";
    public static final String COLUMN_NAME_ALARM_TIME_HOUR = "hour";
    public static final String COLUMN_NAME_ALARM_TIME_MINUTE = "minute";
    public static final String COLUMN_NAME_ALARM_REPEAT_DAYS = "days";
    public static final String COLUMN_NAME_ALARM_REPEAT_WEEKLY = "weekly";
    public static final String COLUMN_NAME_ALARM_TONE = "tone";
    public static final String COLUMN_NAME_ALARM_ENABLED = "isEnabled";
    public static final String _ID = "_id";
    public static final String COLUMN_NAME_ALARM_TONE_NAME = "alarmToneName";
    public static final String COLUMN_NAME_ALARM_TYPE = "type";
    public static final String COLUMN_NAME_ID = "alarmid";
    public static final String COLUMN_NAME_ARTIST = "artist";
    private static final String SQL_CREATE_ALARM =
            "CREATE TABLE " + TABLE_NAME + " (" + "\"" +
                    _ID + "\"" + " INTEGER PRIMARY KEY AUTOINCREMENT," + "\"" +
                    COLUMN_NAME_ALARM_NAME + "\"" + " TEXT," + "\"" +
                    COLUMN_NAME_ALARM_TIME_HOUR + "\"" + " INT," + "\"" +
                    COLUMN_NAME_ALARM_TIME_MINUTE + "\"" + " INT," + "\"" +
                    COLUMN_NAME_ALARM_REPEAT_DAYS + "\"" + " TEXT," + "\"" +
                    COLUMN_NAME_ALARM_REPEAT_WEEKLY + "\"" + " BOOLEAN," + "\"" +
                    COLUMN_NAME_ALARM_TONE_NAME + "\"" + " TEXT," + "\"" +
                    COLUMN_NAME_ALARM_TONE + "\"" + " TEXT," + "\"" +
                    COLUMN_NAME_ARTIST + "\"" + " TEXT," + "\"" +
                    COLUMN_NAME_ID + "\"" + " INT," + "\"" +
                    COLUMN_NAME_ALARM_TYPE + "\"" + " INT," + "\"" +
                    COLUMN_NAME_ALARM_ENABLED + "\"" + " BOOLEAN" + " )";
    private static final String DB_NAME = "deezerAlarmClock";
    private final Context myContext;
    private SQLiteDatabase myDataBase;

    public AlarmDatabaseAccessor(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    public boolean createIfNotValid() {
        boolean isDataPresent = isDatabaseValid();
        if (isDataPresent) {
            return true;
        } else {
            createDataBase();
        }
        return true;
    }

    private void createDataBase() {
        boolean dbExist = isDatabaseValid();
        if (!dbExist) {
            this.getReadableDatabase();
            myDataBase = myContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
            myDataBase.execSQL(SQL_CREATE_ALARM);
        }
        close();
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    boolean isDatabaseValid() {
        File dbFile = myContext.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    void openDataBase() {
        String DB_PATH = "/data/data/com.cikoapps.deezeralarm/databases/";
        String myPath = DB_PATH + DB_NAME;
        myDataBase = myContext.openOrCreateDatabase(myPath, SQLiteDatabase.OPEN_READONLY, null);
    }

    public void insertAlarmIntoDatabase(String title, int hour, int minute, String repeatDays, boolean repeatWeekly, String alarmToneName, String artist,
                                        String alarmTone, long alamrid, int type, boolean enabled) {


        openDataBase();
        String insertQuery = "Insert into " + TABLE_NAME + " ( " +
                COLUMN_NAME_ALARM_NAME + " , " +
                COLUMN_NAME_ALARM_TIME_HOUR + " , " +
                COLUMN_NAME_ALARM_TIME_MINUTE + " , " +
                COLUMN_NAME_ALARM_REPEAT_DAYS + " , " +
                COLUMN_NAME_ALARM_REPEAT_WEEKLY + " , " +
                COLUMN_NAME_ALARM_TONE_NAME + " , " +
                COLUMN_NAME_ALARM_TONE + " , " +
                COLUMN_NAME_ARTIST + " , " +
                COLUMN_NAME_ID + " , " +
                COLUMN_NAME_ALARM_TYPE + " , " +
                COLUMN_NAME_ALARM_ENABLED + " ) values ( " +
                "\"" + title + "\"" + " , " +
                hour + " , " +
                minute + " , " +
                "\"" + repeatDays + "\"" + " , " +
                "\"" + repeatWeekly + "\"" + " , " +
                "\"" + alarmToneName + "\"" + " , " +
                "\"" + alarmTone + "\"" + " , " +
                "\"" + artist + "\"" + " , " +
                alamrid + " , " +
                type + " , " +
                "\"" + enabled + "\"" + " ); ";

        myDataBase.execSQL(insertQuery);
        close();


    }

    public Cursor getAlarmsCursor() {
        openDataBase();
        return myDataBase.rawQuery("select * from alarm", null);
    }

    public void updateIsEnabled(int id, boolean isEnabled) {
        openDataBase();
        String updateQuery = "UPDATE alarm SET isEnabled=\"" + isEnabled + "\" WHERE _id=" + id + ";";
        myDataBase.execSQL(updateQuery);
        close();
    }

    public void deleteAlarm(int id) {
        openDataBase();
        myDataBase.delete("alarm", "_id" + "=" + id, null);
        close();
    }


    public List<Alarm> getAlarmList() {
        List<Alarm> alarmList = new ArrayList<>();
        openDataBase();
        Cursor cursor = myDataBase.rawQuery("select * from alarm", null);

        if (cursor.moveToFirst()) {
            do {
                int _id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ALARM_NAME));
                int hour = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ALARM_TIME_HOUR));
                int minute = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ALARM_TIME_MINUTE));
                String days = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ALARM_REPEAT_DAYS));
                boolean weekly = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ALARM_REPEAT_WEEKLY)));
                String alarmToneName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ALARM_TONE_NAME));
                String artist = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTIST));
                String tone = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ALARM_TONE));
                long alarmid = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID));
                int type = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ALARM_TYPE));
                boolean isEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ALARM_ENABLED)));

                String[] repeatingDaysStrings = days.split(",");
                boolean[] repeatingDays = new boolean[repeatingDaysStrings.length];
                int i = 0;
                for (String day : repeatingDaysStrings) {
                    boolean value = Boolean.parseBoolean(day);
                    repeatingDays[i] = value;
                    i++;
                }
                alarmList.add(new Alarm(title, _id, hour, minute, isEnabled, repeatingDays, weekly, tone, artist, alarmid, type, alarmToneName));
            } while (cursor.moveToNext());
            close();
        }
        return alarmList;
    }
}
