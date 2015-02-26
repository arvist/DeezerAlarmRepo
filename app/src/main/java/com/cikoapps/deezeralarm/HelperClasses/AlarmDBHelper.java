package com.cikoapps.deezeralarm.HelperClasses;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cikoapps.deezeralarm.models.Alarm;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlarmDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "AlarmDBHelper.java";
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
            myDataBase.execSQL(SQL_CREATE_WEATHER);
            Log.e(TAG, SQL_CREATE_WEATHER);
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
    private static final String SQL_CREATE_WEATHER =
            "CREATE TABLE `weather` (\n" +
                    "\t`_id`\tINTEGER,\n" +
                    "\t`timeStamp`\tTEXT,\n" +
                    "\t`city`\tTEXT,\n" +
                    "\t`tempC`\tREAL,\n" +
                    "\t`tempF`\tREAL,\n" +
                    "\t`windK`\tREAL,\n" +
                    "\t`windM`\tREAL,\n" +
                    "\t`summary`\tTEXT,\n" +
                    "\t`icon_id`\tINTEGER,\n" +
                    "\tPRIMARY KEY(_id)\n" +
                    ");";
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
                    AlarmContract.COLUMN_NAME_ID + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_TYPE + "\"" + " INT," + "\"" +
                    AlarmContract.COLUMN_NAME_ALARM_ENABLED + "\"" + " BOOLEAN" + " )";

    public void insertWeather(String timeStamp, String city, double tempC, double tempF,
                              double windK, double windM, String summary, int icon_id ){
        String insertQuery = "Insert into weather (timeStamp, city, tempC, tempF, windK, windM, summary, icon_Id) " +
                "values(\""+timeStamp+"\",\""+city+"\", "+tempC+", "+tempF+", "+windK+", "+windM+", \""+summary+"\", "+icon_id+");";
        try {
            openDataBase();
            myDataBase.execSQL(insertQuery);
            Log.e("DEBUG", insertQuery);
            close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void insertAlarm(String title, int hour, int minute, String repeatDays, boolean repeatWeekly, String alarmToneName,
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
                    alamrid + " , " +
                    type + " , " +
                    "\"" + enabled + "\"" + " ); ";

            myDataBase.execSQL(insertQuery);
            Log.e("DEBUG", insertQuery);
            close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static final String SQL_DELETE_ALARM =
            "DROP TABLE IF EXISTS " + AlarmContract.TABLE_NAME;

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
            Log.e(TAG, updateQuery);
            close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //DELETE FROM alarm WHERE _id=1;
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
                    int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String title = cursor.getString(cursor.getColumnIndex("title"));
                    int hour = cursor.getInt(cursor.getColumnIndex("hour"));
                    int minute = cursor.getInt(cursor.getColumnIndex("minute"));
                    String days = cursor.getString(cursor.getColumnIndex("days"));
                    boolean weekly = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("weekly")));
                    String alarmToneName = cursor.getString(cursor.getColumnIndex("alarmToneName"));
                    String tone = cursor.getString(cursor.getColumnIndex("tone"));
                    long alarmid = cursor.getInt(cursor.getColumnIndex("alarmid"));
                    int type = cursor.getInt(cursor.getColumnIndex("type"));
                    boolean isEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isEnabled")));

                    String[] repeatingDaysStrings = days.split(",");
                    boolean[] repeatingDays = new boolean[repeatingDaysStrings.length];
                    int i = 0;
                    for (String day : repeatingDaysStrings) {
                        boolean value = Boolean.parseBoolean(day);
                        repeatingDays[i] = value;
                        i++;
                    }
                    alarmList.add(new Alarm(title, _id, hour, minute, isEnabled, repeatingDays, weekly, tone, alarmid, type, alarmToneName));
                } while (cursor.moveToNext());
            }
            close();

            return alarmList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alarmList;
    }
}
