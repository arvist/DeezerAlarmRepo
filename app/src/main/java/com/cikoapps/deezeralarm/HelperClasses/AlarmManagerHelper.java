package com.cikoapps.deezeralarm.HelperClasses;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cikoapps.deezeralarm.models.Alarm;

import java.util.Calendar;
import java.util.List;


public class AlarmManagerHelper extends BroadcastReceiver {

    public static final String ID = "id";
    public static final String NAME = "name";
    private static final String TIME_HOUR = "hour";
    private static final String TIME_MINUTE = "minute";
    public static final String TONE = "alarmTone";
    public static final String TYPE = "type";
    public static final String ALARM_ID = "alarmid";
    public static final String ONE_TIME_ALARM = "turnOff";

    private static final String TAG = "AlarmManagerHelper.java";

    public static void setAlarms(Context context) {
        cancelAlarms(context);

        AlarmDBHelper dbHelper = new AlarmDBHelper(context);

        List<Alarm> alarms = dbHelper.getAlarmList();
        for (Alarm alarm : alarms) {
            alarm.minute = alarm.minute - 1;
            if (alarm.enabled) {
                PendingIntent pIntent = createPendingIntent(context, alarm);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, alarm.hour);
                calendar.set(Calendar.MINUTE, (alarm.minute));
                calendar.set(Calendar.SECOND, 55);
                //Find next time to set
                final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);
                final int nowSecond = Calendar.getInstance().get(Calendar.SECOND);

                if (allFalse(alarm.repeatingDays)) {
                    Log.e(TAG, alarm.hour + " : " + alarm.minute + " vs " + nowHour + " : " + nowMinute);
                    if ((alarm.hour > nowHour) ||
                            ((alarm.hour == nowHour) && (alarm.minute > nowMinute)) ||
                            ((alarm.hour == nowHour) && (alarm.minute == nowMinute) && (nowSecond < 30))) {
                    } else {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    setAlarm(context, calendar, pIntent);

                } else {
                    boolean alarmSet = false;
                    int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (nowDay == 1) {
                        nowDay = 6;
                    } else {
                        nowDay = nowDay - 2;
                    }
                /*
                    nowDay
                    0 - Monday,
                    1 - Tuesday,
                    2 - Wednesday,
                    3 - Thursday,
                    4 - Friday,
                    5 - Saturday,
                    6 - Sunday
                */
                    //First check if it's later in the week

                    boolean isSunday = false;
                    for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {

                        boolean repeatingDay = alarm.getRepeatingDay(dayOfWeek);
                        if (repeatingDay && dayOfWeek >= nowDay && !(dayOfWeek == nowDay && alarm.hour < nowHour) &&
                                !(dayOfWeek == nowDay && alarm.hour == nowHour && alarm.minute <= nowMinute)) {
                            int day = dayOfWeek;
                            if (day == 6) {
                                day = 1;
                                Log.e(TAG, "Setting Alarm To Sunday");
                                calendar.set(Calendar.DAY_OF_WEEK, 1);
                                calendar.add(Calendar.WEEK_OF_YEAR,1);
                                setAlarm(context, calendar, pIntent);
                                isSunday = true;
                                alarmSet = true;
                            } else {
                                day = day + 2;
                            }
                            if (!isSunday) {
                                Log.e(TAG,"Setting alarm to other days");
                                calendar.set(Calendar.DAY_OF_WEEK, day);
                                setAlarm(context, calendar, pIntent);
                                alarmSet = true;
                            }
                            break;
                        }
                    }

                    //Else check if it's earlier in the week  if(repeatingDay && dayOfWeek >= nowDay && !(dayOfWeek == nowDay && alarm.hour < nowHour) &&
                    if (!alarmSet) {
                        for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                            boolean repeatingDay = alarm.getRepeatingDay(dayOfWeek);
                            if (repeatingDay && dayOfWeek <= nowDay && alarm.repeatWeekly) {
                                int day = dayOfWeek;
                                if (day == 6) {
                                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                                    calendar.set(Calendar.DAY_OF_WEEK, 1);
                                } else {
                                    day = day + 2;
                                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                                    calendar.set(Calendar.DAY_OF_WEEK, day);
                                }
                                setAlarm(context, calendar, pIntent);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean allFalse(boolean[] values) {
        for (boolean value : values) {
            if (value)
                return false;
        }
        return true;
    }

    @SuppressLint("NewApi")
    private static void setAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

    public static void cancelAlarms(Context context) {
        AlarmDBHelper dbHelper = new AlarmDBHelper(context);

        List<Alarm> alarms = dbHelper.getAlarmList();
        if (alarms != null) {
            for (Alarm alarm : alarms) {
                if (alarm.enabled) {
                    PendingIntent pIntent = createPendingIntent(context, alarm);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pIntent);
                }
            }
        }
    }

    private static PendingIntent createPendingIntent(Context context, Alarm alarm) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra(ID, alarm.id);
        intent.putExtra(NAME, alarm.title);
        intent.putExtra(TIME_HOUR, alarm.hour);
        intent.putExtra(TIME_MINUTE, alarm.minute);
        intent.putExtra(TONE, alarm.alarmTone);
        intent.putExtra(TYPE, alarm.type);
        intent.putExtra(ALARM_ID, alarm.alarmid);
        intent.putExtra(ONE_TIME_ALARM, alarm.repeatWeekly);
        return PendingIntent.getService(context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarms(context);
    }
}