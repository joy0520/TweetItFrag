package com.joy.tweetitdeluxe;

import com.facebook.stetho.Stetho;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     TwitterClient client = TweetItApplication.getRestClient();
 *     // use client to send requests to API
 *
 */
public class TweetItApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);

        FlowManager.init(new FlowConfig.Builder(this).build());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);

        TweetItApplication.context = this;
    }

    public static TwitterClient getRestClient() {
        TwitterClient client =  (TwitterClient) TwitterClient.getInstance(TwitterClient.class, TweetItApplication.context);
                client.setRequestIntentFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        return client;
    }
}