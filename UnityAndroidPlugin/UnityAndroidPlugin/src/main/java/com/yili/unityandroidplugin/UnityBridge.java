package com.yili.unityandroidplugin;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.unity3d.player.UnityPlayer;
import com.yili.unityandroidplugin.sdk.GAdMgr;
import com.yili.unityandroidplugin.sdk.GameCenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class UnityBridge {
    static String UnityObjName;
    static String UnityMethodName;

    public UnityBridge() {
        GameCenter.Instance();
    }
    public static void InitSDK() {
        PlayGamesSdk.initialize(UnityPlayer.currentActivity);


        String country = Locale.getDefault().getCountry().toLowerCase();
        String language = Locale.getDefault().getLanguage().toLowerCase();
        try {
            JSONObject json = new JSONObject();
            json.put("msgType", MsgType.ReturnLocation);
            json.put("localtion", language+"_"+country);
            Java2Unity(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void Java2Unity(String json) {
        UnityPlayer.UnitySendMessage(UnityObjName, UnityMethodName, json);
    }

    public static void CSharpMessage(String json) {
        JSONObject data;
        try {
            data = new JSONObject(json);
            UnityObjName = data.getString("gameObjName");
            UnityMethodName=data.getString("OCMessageMethodName");
            int msgType = data.getInt("msgType");
            if (msgType == MsgType.InitGoogleAd) {
                String banner_unit = data.getString("banner_unit");
                String interstitial_unit = data.getString("interstitial_unit");
                String reward_unit = data.getString("reward_unit");
                GAdMgr.Instance().Init(banner_unit, interstitial_unit, reward_unit);

                InitSDK();
            } else if (msgType == MsgType.SaveEventPoint) {
                String event_name = data.getString("event_name");
                String event_params = data.getString("event_params");
            } else if (msgType == MsgType.Restore) {

            } else if (msgType == MsgType.RateGame) {
                String url = data.getString("url");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                UnityPlayer.currentActivity.startActivity(intent);
            } else if (msgType == MsgType.ShowGameCenter) {
                String leaderboard_id = data.getString("id");
                GameCenter.ShowGameCenter(leaderboard_id);
            } else if (msgType == MsgType.SubmitScore) {
                String leaderboard_id = data.getString("id");
                int score = data.getInt("score");
                GameCenter.SubmitScore(leaderboard_id, score);
            } else if (msgType == MsgType.HideBanner) {
                GAdMgr.Instance().HideBanner();
            } else if (msgType == MsgType.PlayBanner) {
                String banner_pos = data.getString("banner_pos");
                if(!banner_pos.isEmpty()&&banner_pos.equals("top"))
                    GAdMgr.Instance().ShowBanner("top");
                else
                    GAdMgr.Instance().ShowBanner("bottom");
            } else if (msgType == MsgType.PlayInterstitial) {
                GAdMgr.Instance().ShowInterstitial();
            } else if (msgType == MsgType.PlayReward) {
                GAdMgr.Instance().ShowReward();
            }else if(msgType==MsgType.Purchase){

            }else if(msgType==MsgType.RegisterNoti){
                String zh_cn = data.getString("zh_cn");
                String en = data.getString("en");
                String country = Locale.getDefault().getCountry().toLowerCase();
                String language = Locale.getDefault().getLanguage().toLowerCase();
                if(country=="cn"&&language=="zh"){
                    GameCenter.RegisterNoti(zh_cn);
                }else{
                    GameCenter.RegisterNoti(en);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}