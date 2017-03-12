package com.joy.tweetitdeluxe.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.joy.tweetitdeluxe.NetworkCheck;
import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.TweetItApplication;
import com.joy.tweetitdeluxe.TweetItUtil;
import com.joy.tweetitdeluxe.TwitterClient;
import com.joy.tweetitdeluxe.dialog.ComposeDialog;
import com.joy.tweetitdeluxe.fragment.TimelineFragment;
import com.joy.tweetitdeluxe.fragment.UserProfileHeaderFragment;
import com.joy.tweetitdeluxe.fragment.UserTimelineFragment;
import com.joy.tweetitdeluxe.model.Tweet;
import com.joy.tweetitdeluxe.model.User;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by joy0520 on 2017/3/10.
 */

public class ProfileActivity extends AppCompatActivity implements ComposeDialog.Callback, TimelineFragment.Callback {
    private static final String TAG = "ProfileActivity";

    TwitterClient mClient;

    private Toolbar mToolbar;
    private MenuItem mToolbarProgress;
    private TextView mNoNetwork;
    private String mScreenName;

    private UserProfileCallback mCallback;

    public interface UserProfileCallback {
        void onPostNewTweet(Tweet newTweet);
    }

    private Handler mHandler;

    private Runnable mCheckNetRunnable = new Runnable() {
        @Override
        public void run() {
            if (NetworkCheck.isNetworkAvailable(getBaseContext())) {
                mNoNetwork.setVisibility(View.GONE);
            } else {
                mNoNetwork.setVisibility(View.VISIBLE);
            }
            mHandler.postDelayed(mCheckNetRunnable, TweetItApplication.INTERVAL_CHECK_NET_MS);
        }
    };

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNoNetwork = (TextView) findViewById(R.id.no_network);

        setSupportActionBar(mToolbar);

        // Get screen name
        mScreenName = getIntent().getExtras().getString("screen_name");

        mHandler = new Handler();

        // Get the account info
        mClient = TweetItApplication.getRestClient();
        mClient.getUser(mScreenName, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (TweetItApplication.DEBUG)
                    Log.d(TAG, "failed to get account info for screen name " + mScreenName + " : " + responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Gson gson = new Gson();
                User user = gson.fromJson(responseString, User.class);
                // Set toolbar title as user screen name
                mToolbar.setTitle("@" + user.getScreen_name());

                if (savedInstanceState == null) {
                    setupFragments(user);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.post(mCheckNetRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        mToolbarProgress = menu.findItem(R.id.toolbar_progress);
        // Extract the action-view from the menu item
//        ProgressBar v = (ProgressBar) MenuItemCompat.getActionView(mToolbarProgress);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPostNewTweet(String newTweetBody) {
        if (TweetItApplication.DEBUG) Log.i("onPostNewTweet()", "newTweetBody=" + newTweetBody);
        if (newTweetBody == null || newTweetBody.isEmpty()) return;
        TweetItApplication.getRestClient().postTweet(newTweetBody,
                new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.i(TAG + "onFailure()", "" + responseString);
                        setProgressVisible(false);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Log.i(TAG + "onSuccess()", "" + responseString);
                        setProgressVisible(true);
                        Gson gson = new Gson();
                        Tweet tweet = gson.fromJson(responseString, Tweet.class);

                        if (mCallback != null) {
                            mCallback.onPostNewTweet(tweet);
                        }

                        // Clear draft
                        TweetItUtil.clearTweetDraft(ProfileActivity.this);
                        setProgressVisible(false);
                    }
                });

    }

    @Override
    public void onCancelNewTweet(String newTweet) {
        TweetItUtil.saveTweetDraft(this, newTweet);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_compose:
                showComposeDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(Tweet tweet) {

    }

    @Override
    public void notifyAppBarCollapse() {

    }

    @Override
    public void setProgressVisible(boolean visible) {
        showToolbarProgress(visible);
    }

    @Override
    public void setNoNetworkVisible(boolean visible) {
        mNoNetwork.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialog dialog = ComposeDialog.newInstance(getString(R.string.compose_dialog), TweetItUtil.getTweetDraft(this));
        dialog.setCallback(this);
        dialog.show(fm, "fragment_compose_dialog");
    }

    private void setupFragments(User user) {
        // Create the user header fragment
        UserProfileHeaderFragment fragmentUserHeader = UserProfileHeaderFragment.newInstance(user);
        // Display user header fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flUserHeader, fragmentUserHeader, "user_header_frag");

        // Create the user timeline fragment
        UserTimelineFragment fragmentUserTimeline = UserTimelineFragment.newInstance(user.getScreen_name());
        // Display user fragment within this activity (dynamically)
        ft.replace(R.id.flUserTimeline, fragmentUserTimeline, "user_timeline_frag");

        // Commit it
        ft.commit();
    }

    private void showToolbarProgress(boolean visible) {
        if (mToolbarProgress != null) mToolbarProgress.setVisible(visible);
    }

    public void setCallback(UserProfileCallback callback) {
        mCallback = callback;
    }
}
