package com.yili.unityandroidplugin.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.unity3d.player.UnityPlayer;

public class BootReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            RemindersManager.StartReminder(UnityPlayer.currentActivity);
        }
    }
}
