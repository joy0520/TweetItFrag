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

public class HomeTimelineFragment extends TimelineFragment {
    private static final String TAG = "HomeTimelineFragment";

    public static TimelineFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        TimelineFragment fragment = new HomeTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Populate home timeline
        populateHomeTimeline();
    }


    @Override
    void onSwipeRefresh() {
        populateHomeTimeline();
    }

    @Override
    void onScroll() {
    }

    @Override
    public void onLoadMore() {
        populateHomeTimeline(mCurrentMaxTimelinePage + 1);
    }

    private void populateHomeTimeline() {
        // Reset
        mCurrentMaxTimelinePage = 0;
        clearAllTweets();
        if (NetworkCheck.isOnlineAndAvailable(getContext())) {
            mCallback.setProgressVisible(true);
            populateHomeTimeline(0);
        } else {
            mCallback.setNoNetworkVisible(true);
            setRefreshing(false);
            applyLocalTweets();
        }
    }

    private void populateHomeTimeline(final int page) {
        // DEBUG prevent load too much!!
        if (TweetItApplication.DEBUG && page >= 2) {
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

        TweetItApplication.getRestClient().getHomeTimeline(page, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                mCallback.setProgressVisible(false);
                Toast.makeText(getActivity(), "failed to load home timeline:\n" + responseString,
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
