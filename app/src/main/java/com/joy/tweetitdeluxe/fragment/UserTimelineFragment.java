package com.joy.tweetitdeluxe.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joy.tweetitdeluxe.NetworkCheck;
import com.joy.tweetitdeluxe.TweetItApplication;
import com.joy.tweetitdeluxe.activity.HomeActivity;
import com.joy.tweetitdeluxe.model.Tweet;
import com.loopj.android.http.TextHttpResponseHandler;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by joy0520 on 2017/3/10.
 */

public class UserTimelineFragment extends TimelineFragment {
    public static final String ARG_SCREEN_NAME = "ARG_SCREEN_NAME";
    private static final String TAG = "UserTimelineFragment";

    private String mScreenName;

    public static UserTimelineFragment newInstance(String screenName) {
        Bundle args = new Bundle();
        args.putString(ARG_SCREEN_NAME, screenName);
        UserTimelineFragment fragment = new UserTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScreenName = getArguments().getString(ARG_SCREEN_NAME);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        populateUserTimeline();
    }

    @Override
    void onSwipeRefresh() {
        populateUserTimeline();
    }

    @Override
    void onScroll() {

    }

    @Override
    void onLoadMore() {
        populateUserTimeline(mCurrentMaxTimelinePage + 1);
    }


    private void populateUserTimeline() {
        // Reset
        mCurrentMaxTimelinePage = 0;
        clearAllTweets();
        if (NetworkCheck.isOnlineAndAvailable(getContext())) {
            mCallback.setProgressVisible(true);
            populateUserTimeline(0);
        } else {
            mCallback.setNoNetworkVisible(true);
            setRefreshing(false);
            // Since I cannot solve how to retrieve those tweets data that mention myself,
            // I'll leave it empty when there's no network available.
//            applyLocalTweets();
        }
    }

    private void populateUserTimeline(final int page) {
        // DEBUG prevent load too much!!
        if (HomeActivity.DEBUG && page >= 2) {
            return;
        }
        // No network hint
        if (NetworkCheck.isOnlineAndAvailable(getContext())) {
            mCallback.setNoNetworkVisible(false);
        } else {
            mCallback.setNoNetworkVisible(true);
            return;
        }
        // Show the progress bar
        mCallback.setProgressVisible(true);

        TweetItApplication.getRestClient().getUserTimeline(mScreenName, page, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                mCallback.setProgressVisible(false);
                Toast.makeText(getActivity(), "failed to load mentions timeline:\n" + responseString,
                        Toast.LENGTH_SHORT).show();
                setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                setRefreshing(false);
                Gson gson = new Gson();
                Log.i(TAG + "onSuccess(1)", "" + responseString);

                Type listType = new TypeToken<List<Tweet>>() {
                }.getType();
                List<Tweet> tweets = gson.fromJson(responseString, listType);
                Log.i(TAG + "onSuccess(2)", "" + tweets);
                addTweets(tweets);

                // Hide the progress bar
                mCallback.setProgressVisible(false);
                mCurrentMaxTimelinePage = page;
                finishLoading();
            }
        });
    }
}
