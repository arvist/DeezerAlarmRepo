package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cikoapps.deezeralarm.models.Alarm;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlarmDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "AlarmDBHelper.java";
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
                    AlarmContract.COLUMN_NAME_ARTIST + "\"" + " TEXT," + "\"" +
                    AlarmContract.COLUMN_NAME_ID + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_TYPE + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_ENABLED + "\"" + " BOOLEAN" + " )";
    private static String DB_NAME = "deezerAlarmClock";
    private final Context myContext;
    private SQLiteDatabase myDataBase;

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

    boolean checkDataBase() {
        File dbFile = myContext.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    void openDataBase() throws SQLException {
        String DB_PATH = "/data/data/com.cikoapps.deezeralarm/databases/";
        String myPath = DB_PATH + DB_NAME;
        myDataBase = myContext.openOrCreateDatabase(myPath, SQLiteDatabase.OPEN_READONLY, null);
    }

    public void insertAlarm(String title, int hour, int minute, String repeatDays, boolean repeatWeekly, String alarmToneName, String artist,
                            String alarmTone, long alamrid, int type, boolean enabled) {

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
                    AlarmContract.COLUMN_NAME_ARTIST + " , " +
                    AlarmContract.COLUMN_NAME_ID + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_TYPE + " , " +
                    AlarmContract.COLUMN_NAME_ALARM_ENABLED + " ) values ( " +
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Cursor getAlarms() {
        try {
            openDataBase();
            return myDataBase.rawQuery("select * from alarm", null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateIsEnabled(int id, boolean isEnabled) {
        try {
            openDataBase();
            String updateQuery = "UPDATE alarm SET isEnabled=\"" + isEnabled + "\" WHERE _id=" + id + ";";
            myDataBase.execSQL(updateQuery);
            close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAlarm(int id) {
        try {
            openDataBase();
            myDataBase.delete("alarm", "_id" + "=" + id, null);
            close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Alarm> getAlarmList() {
        List<Alarm> alarmList = new ArrayList<>();
        try {
            openDataBase();
            Cursor cursor = myDataBase.rawQuery("select * from alarm", null);

            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(cursor.getColumnIndex(AlarmContract._ID));
                    String title = cursor.getString(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_NAME));
                    int hour = cursor.getInt(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_TIME_HOUR));
                    int minute = cursor.getInt(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_TIME_MINUTE));
                    String days = cursor.getString(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_REPEAT_DAYS));
                    boolean weekly = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_REPEAT_WEEKLY)));
                    String alarmToneName = cursor.getString(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_TONE_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ARTIST));
                    String tone = cursor.getString(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_TONE));
                    long alarmid = cursor.getInt(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ID));
                    int type = cursor.getInt(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_TYPE));
                    boolean isEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(AlarmContract.COLUMN_NAME_ALARM_ENABLED)));

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alarmList;
    }
}
