package com.joy.tweetitdeluxe.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.fragment.FavoritesTimelineFragment;
import com.joy.tweetitdeluxe.fragment.TimelineFragment;
import com.joy.tweetitdeluxe.model.Tweet;

/**
 * Created by joy0520 on 2017/3/12.
 */

public class FavoritesActivity extends AppCompatActivity implements TimelineFragment.Callback {
    public static final String EXTRA_SCREEN_NAME = "screen_name";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        String screenName = getIntent().getExtras().getString(EXTRA_SCREEN_NAME);
        if (savedInstanceState == null) {
            setupFragment(screenName);
        }

    }

    private void setupFragment(String screenName) {
        FavoritesTimelineFragment favoritesTimelineFragment = FavoritesTimelineFragment.newInstance(screenName);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.flUserListHolder, favoritesTimelineFragment);
        ft.commit();
    }

    @Override
    public void onItemClicked(Tweet tweet) {

    }

    @Override
    public void notifyAppBarCollapse() {

    }

    @Override
    public void setProgressVisible(boolean visible) {

    }

    @Override
    public void setNoNetworkVisible(boolean visible) {

    }
}
