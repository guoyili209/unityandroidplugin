package com.yili.unityandroidplugin.sdk;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import com.yili.unityandroidplugin.R;

public class AlarmReceiver extends BroadcastReceiver {
    public static String title="";
    public static String content="";
    private int NOTIFICATION_ID=1;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("alarm","xxx");
        NotificationManager notificationManager = ContextCompat.getSystemService(context,NotificationManager.class);
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Intent contentIntent = new Intent(context, UnityPlayer.currentActivity.getClass());

        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 1, contentIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setContentTitle(AlarmReceiver.title)
                .setContentText(AlarmReceiver.content)
                .setSmallIcon(GetAppIcon())
                .setStyle(
                        new NotificationCompat.BigTextStyle().bigText(AlarmReceiver.content)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());

        RemindersManager.StartReminder(context.getApplicationContext());
    }

    public int GetAppIcon(){
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
