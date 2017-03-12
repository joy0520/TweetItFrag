package com.joy.tweetitdeluxe.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joy.tweetitdeluxe.NetworkCheck;
import com.joy.tweetitdeluxe.TweetItApplication;
import com.joy.tweetitdeluxe.model.Tweet;
import com.loopj.android.http.TextHttpResponseHandler;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by joy0520 on 2017/3/12.
 */

public class FavoritesTimelineFragment extends TimelineFragment {
    private static final String TAG = "FavoritesTimelineFragment";
    public static final String ARG_SCREEN_NAME = "screen_name";

    private String mScreenName;

    public static FavoritesTimelineFragment newInstance(String screenName) {
        Bundle args = new Bundle();
        args.putString(ARG_SCREEN_NAME, screenName);
        FavoritesTimelineFragment fragment = new FavoritesTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        populateFavoritesTimeline();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mScreenName = getArguments().getString(ARG_SCREEN_NAME);
        }
    }

    @Override
    void onSwipeRefresh() {
        populateFavoritesTimeline();
    }

    @Override
    void onScroll() {

    }

    @Override
    void onLoadMore() {

    }

    private void populateFavoritesTimeline() {
        // Reset
        mCurrentMaxTimelinePage = 0;
        clearAllTweets();
        if (NetworkCheck.isOnlineAndAvailable(getContext())) {
        } else {
            setRefreshing(false);
            return;
        }


        TweetItApplication.getRestClient().getFavorites(mScreenName, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (TweetItApplication.DEBUG) Log.i(TAG + "onFailure()", "" + responseString);
                Toast.makeText(getActivity(), TAG + " failed to load home timeline:\n" + responseString,
                        Toast.LENGTH_SHORT).show();
                setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                setRefreshing(false);
                Gson gson = new Gson();
                if (TweetItApplication.DEBUG) Log.i(TAG + "onSuccess()", "" + responseString);


                Type listType = new TypeToken<List<Tweet>>() {
                }.getType();
                List<Tweet> tweets = gson.fromJson(responseString, listType);
                addTweets(tweets);

                finishLoading();
            }
        });
    }

}
