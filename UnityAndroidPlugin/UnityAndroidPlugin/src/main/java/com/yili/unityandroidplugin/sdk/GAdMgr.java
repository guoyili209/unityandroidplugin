package com.yili.unityandroidplugin.sdk;

import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.unity3d.player.UnityPlayer;

public class GAdMgr {
    AdView adView;

    public GAdMgr() {

    }

    public void Init() {
        adView = new AdView(UnityPlayer.currentActivity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        MobileAds.initialize(UnityPlayer.currentActivity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
            }
        });
    }

    public void ShowBanner() {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);
        adView.resume();
        adView.setVisibility(View.VISIBLE);
    }

    public void HideBanner() {
        adView.pause();
        adView.setVisibility(View.GONE);
    }
}
