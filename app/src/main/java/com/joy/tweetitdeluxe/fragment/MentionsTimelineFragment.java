package com.joy.tweetitdeluxe.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joy.tweetitdeluxe.R;

/**
 * Created by joy0520 on 2017/3/10.
 */

public class MentionsTimelineFragment extends TimelineFragment {



    public static TimelineFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        TimelineFragment fragment = new MentionsTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Populate mentions timeline
    }

    @Override
    void onSwipeRefresh() {

    }

    @Override
    void onScroll() {

    }

    @Override
    void onLoadMore() {

    }
}
