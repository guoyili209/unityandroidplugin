package com.yili.unityandroidplugin.sdk;

import android.content.Intent;

import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.tasks.OnSuccessListener;
import com.unity3d.player.UnityPlayer;

public class GameCenter {
    private static GameCenter _Instance;
    private static Boolean is_login = false;

    public static String leader_id = "";

    public static GameCenter Instance() {
        if (_Instance == null) {
            _Instance = new GameCenter();
        }
        return _Instance;
    }
    public static void SubmitScore(int nu){
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
    public static void ShowGameCenter() {
//        if (is_login == false) {
            GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(UnityPlayer.currentActivity);
            gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
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

                }
            });
//        } else {
//            _DoShowLeaderBoard();
//        }
    }

    private static void _DoShowLeaderBoard() {
        PlayGames.getLeaderboardsClient(UnityPlayer.currentActivity)
                .getLeaderboardIntent(leader_id)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        UnityPlayer.currentActivity.startActivityForResult(intent, 10086);
                    }
                });
    }
}
