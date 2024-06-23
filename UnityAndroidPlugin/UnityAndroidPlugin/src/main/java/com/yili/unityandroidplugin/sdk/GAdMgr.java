package com.yili.unityandroidplugin.sdk;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdapterResponseInfo;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.unity3d.player.UnityPlayer;
import com.yili.unityandroidplugin.MsgType;
import com.yili.unityandroidplugin.UnityBridge;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GAdMgr {
    private String banner_id;
    private String inters_id;
    private String reward_id;
    private LinearLayout banner_layout;
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private RewardedAd rewardedAd;
    private Boolean get_reward = false;
    private static GAdMgr _instance;

    public GAdMgr() {

    }

    public static GAdMgr Instance() {
        if (_instance == null) {
            _instance = new GAdMgr();
        }
        return _instance;
    }

    public void Init(String banner_id, String inters_id, String reward_id) {
        this.banner_id = banner_id;
        this.inters_id = inters_id;
        this.reward_id = reward_id;
        adView = new AdView(UnityPlayer.currentActivity);
        adView.setAdSize(AdSize.BANNER);
        if (banner_id.isEmpty() == false) {
            adView.setAdUnitId(banner_id);

            banner_layout = new LinearLayout(UnityPlayer.currentActivity.getApplicationContext());
            banner_layout.setOrientation(LinearLayout.VERTICAL);
            banner_layout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UnityPlayer.currentActivity.addContentView(banner_layout, lp);
                    banner_layout.addView(adView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }
            });
            HideBanner();
        }

        MobileAds.initialize(UnityPlayer.currentActivity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }

//                GameCenter.RegisterNoti();
                _LoadBanner();
                _LoadInterstitial();
                _LoadReward();
            }
        });
    }

    private void _LoadBanner() {
        if (banner_id.isEmpty() == false) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdImpression() {
                    // Code to be executed when an impression is recorded
                    // for an ad.
                }

                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    try {
                        JSONObject json = new JSONObject();
                        json.put("msgType", MsgType.BannerCanShow);
                        UnityBridge.Java2Unity(json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }
            });
        }
    }

    private void _LoadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(UnityPlayer.currentActivity, inters_id, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
//                定时器延时
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            _LoadInterstitial();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                timer.schedule(task, 10000);
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i("ad_interstitial", "onAdLoaded");
                try {
                    JSONObject json = new JSONObject();
                    json.put("msgType", MsgType.InterstitalCanShow);
                    json.put("interstitial_is_load", true);
                    UnityBridge.Java2Unity(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d("ad_interstitial", "Ad was clicked.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d("ad_interstitial", "Ad dismissed fullscreen content.");
                        mInterstitialAd = null;
                        _LoadInterstitial();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.e("ad_interstitial", "Ad failed to show fullscreen content.");
                        mInterstitialAd = null;
                        _LoadInterstitial();
                    }

                    @Override
                    public void onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("ad_interstitial", "Ad recorded an impression.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("ad_interstitial", "Ad showed fullscreen content.");
                    }
                });

                mInterstitialAd.setOnPaidEventListener(new OnPaidEventListener() {
                    @Override
                    public void onPaidEvent(AdValue adValue) {
                        // TODO: Send the impression-level ad revenue information to your
                        //preferred analytics server directly within this callback.

                        // Extract the impression-level ad revenue data.
                        long valueMicros = adValue.getValueMicros();
                        String currencyCode = adValue.getCurrencyCode();
                        int precision = adValue.getPrecisionType();

                        // Get the ad unit ID.
                        String adUnitId = mInterstitialAd.getAdUnitId();

                        AdapterResponseInfo loadedAdapterResponseInfo = mInterstitialAd.getResponseInfo().
                                getLoadedAdapterResponseInfo();
                        String adSourceName = loadedAdapterResponseInfo.getAdSourceName();
                        String adSourceId = loadedAdapterResponseInfo.getAdSourceId();
                        String adSourceInstanceName = loadedAdapterResponseInfo.getAdSourceInstanceName();
                        String adSourceInstanceId = loadedAdapterResponseInfo.getAdSourceInstanceId();

//                        Bundle extras = rewardedAd.getResponseInfo().getResponseExtras();
//                        String mediationGroupName = extras.getString("mediation_group_name");
//                        String mediationABTestName = extras.getString("mediation_ab_test_name");
//                        String mediationABTestVariant = extras.getString("mediation_ab_test_variant");

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob");
                        bundle.putString(FirebaseAnalytics.Param.AD_SOURCE, adSourceName);
                        bundle.putString(FirebaseAnalytics.Param.AD_FORMAT, adSourceId);
                        bundle.putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adSourceInstanceName);
                        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
                        bundle.putDouble(FirebaseAnalytics.Param.VALUE, valueMicros / 1000000.0);
                        FirebaseMgr.LogEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle);
                    }
                });
            }
        });
    }

    private void _LoadReward() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(UnityPlayer.currentActivity, reward_id, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            _LoadReward();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                timer.schedule(task, 10000);
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                Log.d("reward", "Ad was loaded.");
                try {
                    JSONObject json = new JSONObject();
                    json.put("msgType", MsgType.RewardCanShow);
                    json.put("reward_is_load", true);
                    UnityBridge.Java2Unity(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d("reward", "Ad was clicked.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d("reward", "Ad dismissed fullscreen content.");
                        rewardedAd = null;
                        if (get_reward == true) {
                            try {
                                JSONObject json = new JSONObject();
                                json.put("msgType", MsgType.PlayRewardEnd);
                                UnityBridge.Java2Unity(json.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                JSONObject json = new JSONObject();
                                json.put("msgType", MsgType.PlayRewardFail);
                                UnityBridge.Java2Unity(json.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        _LoadReward();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.e("reward", "Ad failed to show fullscreen content.");
                        rewardedAd = null;
                        try {
                            JSONObject json = new JSONObject();
                            json.put("msgType", MsgType.PlayRewardFail);
                            UnityBridge.Java2Unity(json.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        _LoadReward();
                    }

                    @Override
                    public void onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d("reward", "Ad recorded an impression.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("reward", "Ad showed fullscreen content.");
                    }
                });

                rewardedAd.setOnPaidEventListener(new OnPaidEventListener() {
                    @Override
                    public void onPaidEvent(AdValue adValue) {
                        // TODO: Send the impression-level ad revenue information to your
                        //preferred analytics server directly within this callback.

                        // Extract the impression-level ad revenue data.
                        long valueMicros = adValue.getValueMicros();
                        String currencyCode = adValue.getCurrencyCode();
                        int precision = adValue.getPrecisionType();

                        // Get the ad unit ID.
                        String adUnitId = rewardedAd.getAdUnitId();

                        AdapterResponseInfo loadedAdapterResponseInfo = rewardedAd.getResponseInfo().
                                getLoadedAdapterResponseInfo();
                        String adSourceName = loadedAdapterResponseInfo.getAdSourceName();
                        String adSourceId = loadedAdapterResponseInfo.getAdSourceId();
                        String adSourceInstanceName = loadedAdapterResponseInfo.getAdSourceInstanceName();
                        String adSourceInstanceId = loadedAdapterResponseInfo.getAdSourceInstanceId();

//                        Bundle extras = rewardedAd.getResponseInfo().getResponseExtras();
//                        String mediationGroupName = extras.getString("mediation_group_name");
//                        String mediationABTestName = extras.getString("mediation_ab_test_name");
//                        String mediationABTestVariant = extras.getString("mediation_ab_test_variant");

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob");
                        bundle.putString(FirebaseAnalytics.Param.AD_SOURCE, adSourceName);
                        bundle.putString(FirebaseAnalytics.Param.AD_FORMAT, adSourceId);
                        bundle.putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adSourceInstanceName);
                        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
                        bundle.putDouble(FirebaseAnalytics.Param.VALUE, valueMicros / 1000000.0);
                        FirebaseMgr.LogEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle);
                    }
                });
            }
        });
    }

    public void ShowBanner(String pos) {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(pos.equals("top"))
                    banner_layout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                else
                    banner_layout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                adView.resume();
                adView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void HideBanner() {
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.pause();
                adView.setVisibility(View.GONE);
            }
        });
    }

    public void ShowInterstitial() {
        if (mInterstitialAd != null) {
            UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mInterstitialAd.show(UnityPlayer.currentActivity);
                }
            });
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }

    public void ShowReward() {
        get_reward = false;
        if (rewardedAd != null) {
            UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rewardedAd.show(UnityPlayer.currentActivity, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Handle the reward.
                            Log.d("reward", "The user earned the reward.");
                            int rewardAmount = rewardItem.getAmount();
                            String rewardType = rewardItem.getType();
                            get_reward = true;
                        }
                    });
                }
            });
        } else {
            Log.d("reward", "The rewarded ad wasn't ready yet.");
            try {
                JSONObject json = new JSONObject();
                json.put("msgType", MsgType.PlayRewardFail);
                UnityBridge.Java2Unity(json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
