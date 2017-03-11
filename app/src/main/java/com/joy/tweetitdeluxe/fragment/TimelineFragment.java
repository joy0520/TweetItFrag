package com.joy.tweetitdeluxe.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.activity.HomeActivity;
import com.joy.tweetitdeluxe.adapter.TweetsAdapter;
import com.joy.tweetitdeluxe.model.Tweet;

import java.util.List;

/**
 * Created by joy0520 on 2017/3/8.
 */

public abstract class TimelineFragment extends Fragment implements TweetsAdapter.Callback {
    public interface Callback {
        void onItemClicked(Tweet tweet);

        void notifyAppBarCollapse();

        void setProgressVisible(boolean visible);

        void setNoNetworkVisible(boolean visible);
    }

    abstract void onSwipeRefresh();

    abstract void onScroll();

    abstract void onLoadMore();

    private static final String TAG = "TimelineFragment.";
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mList;

    private TweetsAdapter mAdapter;
    private LinearLayoutManager mManager;
    private TweetsAdapter.EndlessScrollListener mScrollListener;
    private Runnable mCollapseAppbarRunnable = new Runnable() {
        @Override
        public void run() {
            mCallback.notifyAppBarCollapse();
        }
    };

    Callback mCallback;
    private Handler mHandler;
    int mCurrentMaxTimelinePage = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach() context " + (context instanceof Callback ? "is" : "is not") + " a Callback");
        if (context instanceof Callback) {
            mCallback = (Callback) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
        //Handler
        mHandler = new Handler();
    }

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        // Init views
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mList = (RecyclerView) view.findViewById(R.id.list);

        // Setup SwipeRefreshLayout
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onSwipeRefresh();
            }
        });

        // Configure the refreshing colors
        mSwipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Adapter
        mAdapter = createRecyclerViewAdapter(getContext());
        mAdapter.setCallback(this);
        mManager = new LinearLayoutManager(getContext());
        mScrollListener = new TweetsAdapter.EndlessScrollListener(mManager) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mHandler.removeCallbacks(mCollapseAppbarRunnable);
                onScroll();
//                if (mManager != null && mManager.findFirstCompletelyVisibleItemPosition() == 0) {
//                    // On top most of the list, hide app bar immediately.
//                    mHandler.post(mCollapseAppbarRunnable);
//                } else {
                    mHandler.postDelayed(mCollapseAppbarRunnable,
                            HomeActivity.INTERVAL_AUTO_COLLAPSE_APPBAR_MS);
//                }
            }

            @Override
            public void onLoadMore() {
                Log.i("onLoadMore", "load more!");
                TimelineFragment.this.onLoadMore();
            }
        };

        // Setup RecyclerView
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(mManager);
        mList.addOnScrollListener(mScrollListener);

        return view;
    }

    @Override
    public void onItemClicked(Tweet tweet) {
        mCallback.onItemClicked(tweet);
    }

    TweetsAdapter createRecyclerViewAdapter(Context context) {
        return new TweetsAdapter(context);
    }

    public void addTweets(List<Tweet> tweets) {
        mAdapter.addTweets(tweets);
    }

    public void onPostANewTweet(Tweet tweet) {
        mAdapter.postANewTweet(tweet);
    }

    public void clearAllTweets() {
        mAdapter.clearAll();
    }

    public void applyLocalTweets() {
        mAdapter.applyLocalTweets();
    }

    public void setRefreshing(boolean refreshing) {
        mSwipeRefresh.setRefreshing(refreshing);
    }

    public void finishLoading() {
        mScrollListener.finishLoading();
    }

    public void moveToMostTopPosition() {
        mList.scrollToPosition(0);
    }

}
