package com.joy.tweetitdeluxe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.google.gson.Gson;
import com.joy.tweetitdeluxe.NetworkCheck;
import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.TweetItApplication;
import com.joy.tweetitdeluxe.TweetItUtil;
import com.joy.tweetitdeluxe.TwitterClient;
import com.joy.tweetitdeluxe.dialog.ComposeDialog;
import com.joy.tweetitdeluxe.dialog.DetailDialog;
import com.joy.tweetitdeluxe.fragment.HomeTimelineFragment;
import com.joy.tweetitdeluxe.fragment.MentionsTimelineFragment;
import com.joy.tweetitdeluxe.fragment.TimelineFragment;
import com.joy.tweetitdeluxe.model.Tweet;
import com.joy.tweetitdeluxe.model.User;
import com.loopj.android.http.TextHttpResponseHandler;

import org.parceler.Parcel;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

/**
 * Created by joy0520 on 2017/3/7.
 */

public class HomeActivity extends AppCompatActivity implements TimelineFragment.Callback, ComposeDialog.Callback {
    private static final String TAG = "HomeActivity.";
    private static final int INTERVAL_CHECK_NET_MS = 10000;
    public static final int INTERVAL_AUTO_COLLAPSE_APPBAR_MS = 5000;

    public static final boolean DEBUG = true;

    private AppBarLayout mAppbarLayout;
    private Toolbar mToolbar;
    private MenuItem mToolbarProgress;
    private TextView mNoNetwork;
    private ProgressBar mProgressBottom;
    private FloatingActionButton mFloatingButton;
    private TimelineFragment mHomeTimelineFragment, mMentionsTimelineFragment;

    private Handler mHandler;

    private Runnable mCheckNetRunnable = new Runnable() {
        @Override
        public void run() {
            if (NetworkCheck.isNetworkAvailable(getBaseContext())) {
                mNoNetwork.setVisibility(View.GONE);
            } else {
                mNoNetwork.setVisibility(View.VISIBLE);
            }
            mHandler.postDelayed(mCheckNetRunnable, INTERVAL_CHECK_NET_MS);
        }
    };

    /**
     * Return the order of the fragments in the view pager
     */
    private class HomeFragmentPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[]{"Home", "Mentions"};

        public HomeFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return HomeTimelineFragment.newInstance(0);
            } else if (position == 1) {
                return MentionsTimelineFragment.newInstance(1);
            } else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }

        // Here we can finally safely save a reference to the created
        // Fragment, no matter where it came from (either getItem() or
        // FragmentManger). Simply save the returned Fragment from
        // super.instantiateItem() into an appropriate reference depending
        // on the ViewPager position.
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    mHomeTimelineFragment = (TimelineFragment) createdFragment;
                    break;
                case 1:
                    mMentionsTimelineFragment = (TimelineFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_deluxe);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new HomeFragmentPagerAdapter(getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tab_layout);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

        // Init views
        mAppbarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNoNetwork = (TextView) findViewById(R.id.no_network);
        mProgressBottom = (ProgressBar) findViewById(R.id.progrss_bottom);
        mFloatingButton = (FloatingActionButton) findViewById(R.id.floating_action_button);

        // Setup toolbar
        setSupportActionBar(mToolbar);

        mHandler = new Handler();

        // Floating Action Button
        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showComposeDialog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.post(mCheckNetRunnable);
        setupCurrentScreenNameInPrefs();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_compose:
                showComposeDialog();
                return true;
            case R.id.toolbar_profile:
                launchProfileActivity();
                return true;
            case R.id.toolbar_logout:
                // Log out
                TweetItUtil.clearTweetDraft(this);
                TweetItUtil.clearCurrentScreenName(this);
                TweetItApplication.getRestClient().clearAccessToken();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialog dialog = ComposeDialog.newInstance(getString(R.string.compose_dialog), TweetItUtil.getTweetDraft(this));
        dialog.setCallback(this);
        dialog.show(fm, "fragment_compose_dialog");
    }

    private void launchProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("screen_name", TweetItUtil.getCurrentScreenName(this));
        startActivity(intent);
    }

    //
    // Interface for ComposeDialog
    //

    @Override
    public void onPostNewTweet(String newTweetBody) {
        if (TweetItApplication.DEBUG) Log.i("onPostNewTweet()", "newTweetBody=" + newTweetBody);
        if (newTweetBody == null || newTweetBody.isEmpty()) return;
        TweetItApplication.getRestClient().postTweet(newTweetBody,
                new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.i(TAG + "onFailure()", "" + responseString);
                        if (mHomeTimelineFragment != null) {
                            mHomeTimelineFragment.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Log.i(TAG + "onSuccess()", "" + responseString);
                        Gson gson = new Gson();
                        Tweet tweet = gson.fromJson(responseString, Tweet.class);
                        if (mHomeTimelineFragment != null) {
                            mHomeTimelineFragment.setRefreshing(false);
                            mHomeTimelineFragment.onPostANewTweet(tweet);
                            // Move current watching position to the top most.
                            mHomeTimelineFragment.moveToMostTopPosition();
                        }
                        // Clear draft
                        TweetItUtil.clearTweetDraft(HomeActivity.this);
                    }
                });
    }

    @Override
    public void onCancelNewTweet(String newTweet) {
        TweetItUtil.saveTweetDraft(this, newTweet);
    }

    //
    // Interface for TimelineFragment
    //
    @Override
    public void onItemClicked(Tweet tweet) {
        FragmentManager fm = getSupportFragmentManager();
        DetailDialog dialog = DetailDialog.newInstance(tweet);
        dialog.show(fm, "fragment_detail_dialog");
    }

    @Override
    public void notifyAppBarCollapse() {
        if (mAppbarLayout != null) {
            mAppbarLayout.setExpanded(false);
        }
    }

    @Override
    public void setProgressVisible(boolean visible) {
        Log.i("setProgressVisible", "visible=" + visible);
        mProgressBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        showToolbarProgress(visible);
    }

    @Override
    public void setNoNetworkVisible(boolean visible) {
        mNoNetwork.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setupCurrentScreenNameInPrefs() {
        TwitterClient client = TweetItApplication.getRestClient();

        client.getUserInfo(new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (TweetItApplication.DEBUG)
                    Log.d(TAG, "setupCurrentScreenNameInPrefs() failed to get current user info: " + responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Gson gson = new Gson();
                User user = gson.fromJson(responseString, User.class);
                if (user != null) {
                    TweetItUtil.saveCurrentScreenName(HomeActivity.this, user.getScreen_name());
                }
            }
        });
    }

    private void showToolbarProgress(boolean visible) {
        if (mToolbarProgress != null) mToolbarProgress.setVisible(visible);
    }
}
