package com.yili.unityandroidplugin.sdk;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.unity3d.player.UnityPlayer;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RemindersManager {
    private static int REMINDER_NOTIFICATION_REQUEST_CODE = 123;

    public static void StartReminder(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] time_arr = "19:00".split(":");
        Intent intent = new Intent(context, AlarmReceiver.class);
//        intent.setPackage("com.yili.unityandroidplugin.sdk");
        intent.setAction("Alarm_Noti");

        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, 123, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time_arr[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time_arr[1]));

        Calendar newCalendar = Calendar.getInstance(Locale.ENGLISH);
        newCalendar.add(Calendar.MINUTE, 1);

        if (newCalendar.getTimeInMillis() - calendar.getTimeInMillis() > 0) {
            calendar.add(Calendar.DATE, 1);
        }

//        newCalendar.add(Calendar.SECOND, 10);

        alarmMgr.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent), pendingIntent);
//        alarmMgr.setAlarmClock(new AlarmManager.AlarmClockInfo(newCalendar.getTimeInMillis(), pendingIntent), pendingIntent);

        Log.d("reminder", calendar.toString());

//        //                定时器延时
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                try {
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        timer.schedule(task, 5000);

    }
    public static void StopReminder(Context context,int reminderId){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context,AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),reminderId,intent,0);
        alarmManager.cancel(pendingIntent);
    }

    public static int GetAppIcon(){
        int icon=0;
        try{
            PackageManager packageManager = UnityPlayer.currentActivity.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    UnityPlayer.currentActivity.getPackageName(),0
            );
            icon = packageInfo.applicationInfo.icon;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return icon;
    }
}
