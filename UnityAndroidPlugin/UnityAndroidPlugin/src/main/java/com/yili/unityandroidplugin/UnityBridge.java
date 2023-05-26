package com.yili.unityandroidplugin;

import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.unity3d.player.UnityPlayer;
import com.yili.unityandroidplugin.sdk.GameCenter;

public class UnityBridge {
    public UnityBridge() {
        GameCenter.Instance();
    }

    public void InitSDK() {
        PlayGamesSdk.initialize(UnityPlayer.currentActivity);
    }
}
