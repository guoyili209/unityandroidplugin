package com.yili.unityandroidplugin.sdk;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.unity3d.player.UnityPlayer;

public class FirebaseMgr {
    public FirebaseMgr(){

    }
    public static void LogEvent(String event_name,Bundle bundle){
        FirebaseAnalytics.getInstance(UnityPlayer.currentActivity).logEvent(event_name, bundle);
    }
}
