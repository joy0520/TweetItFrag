package com.joy.tweetitdeluxe.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.joy.tweetitdeluxe.NetworkCheck;
import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.TweetItApplication;
import com.joy.tweetitdeluxe.TwitterClient;
import com.joy.tweetitdeluxe.adapter.UsersAdapter;
import com.joy.tweetitdeluxe.model.UserListResponse;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

/**
 * Created by joy0520 on 2017/3/12.
 */

public class UserListFragment extends Fragment {
    private static final String TAG = "UserListFragment";
    public static final String ARG_SCREEN_NAME = "ARG_SCREEN_NAME";

    private String mScreenName;
    private RecyclerView mList;

    private UsersAdapter mAdapter;
    private LinearLayoutManager mManager;
    private UsersAdapter.EndlessScrollListener mScrollListener;

    private long mNextCursor = -1;

    public static UserListFragment newInstance(String screenName) {
        Bundle args = new Bundle();
        args.putString(ARG_SCREEN_NAME, screenName);
        UserListFragment fragment = new UserListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScreenName = getArguments().getString(ARG_SCREEN_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_list, container, false);
        // Init views
        mList = (RecyclerView) view.findViewById(R.id.list);

        // Adapter
        mAdapter = new UsersAdapter(getContext());
        mManager = new LinearLayoutManager(getContext());
        mScrollListener = new UsersAdapter.EndlessScrollListener(mManager) {
            @Override
            public void onLoadMore() {
                Log.i(TAG + "onLoadMore", "load more!");
                UserListFragment.this.onLoadMore();
            }
        };

        // Setup RecyclerView
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(mManager);
        mList.addOnScrollListener(mScrollListener);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        populateUsersList();
    }

    public void onLoadMore() {
        Log.i(TAG , "onLoadMore");
        populateUsersList(mNextCursor);
    }

    private void populateUsersList() {
        populateUsersList(mNextCursor);
    }

    private void populateUsersList(final long nextCursor) {
        if (nextCursor == 0 || mScreenName == null) {// Reached the max page
            return;
        }
        // No network hint
        if (!NetworkCheck.isOnlineAndAvailable(getContext())) return;

        TwitterClient client = TweetItApplication.getRestClient();
        client.getFollowersList(nextCursor, mScreenName, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG + "onFailure", "" + responseString);
                Toast.makeText(getActivity(), "failed to load followers list:\n" + responseString,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (TweetItApplication.DEBUG) Log.i(TAG + "onSuccess", "" + responseString);
                Gson gson = new Gson();
                UserListResponse response = gson.fromJson(responseString, UserListResponse.class);
                mNextCursor = response.getNext_cursor();

                // Pass users data to adapter
                mAdapter.addUsers(Arrays.asList(response.getUsers()));
            }
        });
    }
}
