package com.yili.unityandroidplugin.sdk;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.tasks.OnSuccessListener;
import com.unity3d.player.UnityPlayer;
import com.yili.unityandroidplugin.R;

public class GameCenter {
    private static GameCenter _Instance;
    private static Boolean is_login = false;

    public static String _leader_id = "";

    public static GameCenter Instance() {
        if (_Instance == null) {
            _Instance = new GameCenter();
        }
        return _Instance;
    }
    public static void SubmitScore(String leader_id,int nu){
        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(UnityPlayer.currentActivity);
        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());

            if (isAuthenticated) {
                // Continue with Play Games Services
                is_login = true;
                PlayGames.getLeaderboardsClient(UnityPlayer.currentActivity)
                        .submitScore(leader_id, nu);
            } else {
                // Disable your integration with Play Games Services or show a
                // login button to ask  players to sign-in. Clicking it should
                // call GamesSignInClient.signIn().

            }
        });
    }
    public static void ShowGameCenter(String leader_id) {
        _leader_id = leader_id;
//        if (is_login == false) {
            GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(UnityPlayer.currentActivity);
            gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
                Log.d("PlayGames",isAuthenticatedTask.isSuccessful()+"");
                Log.d("PlayGames",isAuthenticatedTask.getResult().isAuthenticated()+"");
                boolean isAuthenticated =
                        (isAuthenticatedTask.isSuccessful() &&
                                isAuthenticatedTask.getResult().isAuthenticated());

                if (isAuthenticated) {
                    // Continue with Play Games Services
                    is_login = true;
                    _DoShowLeaderBoard();
                } else {
                    // Disable your integration with Play Games Services or show a
                    // login button to ask  players to sign-in. Clicking it should
                    // call GamesSignInClient.signIn().
                    Log.e("PlayGames","fail",isAuthenticatedTask.getException());
                }
            });
//        } else {
//            _DoShowLeaderBoard();
//        }
    }

    private static void _DoShowLeaderBoard() {
        PlayGames.getLeaderboardsClient(UnityPlayer.currentActivity)
                .getLeaderboardIntent(_leader_id)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        UnityPlayer.currentActivity.startActivityForResult(intent, 10086);
                    }
                });
    }

//    private static ActivityResultLauncher<String> requestPermissionLauncher =
//            UnityPlayer.currentActivity.startActivityForResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                if (isGranted) {
//                    // Permission is granted. Continue the action or workflow in your
//                    // app.
//                } else {
//                    // Explain to the user that the feature is unavailable because the
//                    // feature requires a permission that the user has denied. At the
//                    // same time, respect the user's decision. Don't link to system
//                    // settings in an effort to convince the user to change their
//                    // decision.
//                }
//            });
    public static void RegisterNoti(String str){
        if(ContextCompat.checkSelfPermission(UnityPlayer.currentActivity, Manifest.permission.POST_NOTIFICATIONS)== PackageManager.PERMISSION_GRANTED){
            String[] str_arr= str.split("-");
            AlarmReceiver.title = str_arr[0];
            int index = (int)(Math.random()*(str_arr.length-2))+2;
            AlarmReceiver.content = str_arr[index];
            CreateNotificationChannel();
            RemindersManager.StartReminder(UnityPlayer.currentActivity.getApplicationContext());
        }
    }

    private static void CreateNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = UnityPlayer.currentActivity.getString(R.string.channel_name);
//            String description = UnityPlayer.currentActivity.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", "channel name", importance);
            channel.setDescription("description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = UnityPlayer.currentActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static void ShowTipsDialog(){
        new AlertDialog.Builder(UnityPlayer.currentActivity)
                .setTitle("提示信息")
                .setMessage("当前应用缺少必要权限，该功能暂时无法使用。如若需要，请单击[确定]按钮前往设置中心进行权限授权。")
                .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){

                    }
                })
                .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        StartAppSettings();
                    }
                }).show();
    }

    private static void StartAppSettings(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:"+UnityPlayer.currentActivity.getPackageName()));
        UnityPlayer.currentActivity.startActivity(intent);
    }
}
