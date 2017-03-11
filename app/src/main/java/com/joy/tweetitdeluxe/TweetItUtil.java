package com.joy.tweetitdeluxe;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by joy0520 on 2017/3/11.
 */

public final class TweetItUtil {
    private static final String SHARED_PREFS_TWEET_DRAFT_KEY = "tweet_draft";
    private static final String SHARED_PREFS_CURRENT_SCREEN_NAME = "current_screen_name";

    public static void saveTweetDraft(Context context, String tweetBody) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(SHARED_PREFS_TWEET_DRAFT_KEY, tweetBody);
        edit.apply();
    }

    public static String getTweetDraft(Context context) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(SHARED_PREFS_TWEET_DRAFT_KEY, "");
    }

    public static void clearTweetDraft(Context context) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(SHARED_PREFS_TWEET_DRAFT_KEY, "");
        edit.apply();
    }

    public static void saveCurrentScreenName(Context context, String screenName) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(SHARED_PREFS_CURRENT_SCREEN_NAME, screenName);
        edit.apply();
    }

    public static String getCurrentScreenName(Context context) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(SHARED_PREFS_CURRENT_SCREEN_NAME, "");
    }

    public static void clearCurrentScreenName(Context context) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(SHARED_PREFS_CURRENT_SCREEN_NAME, "");
        edit.apply();
    }
}
