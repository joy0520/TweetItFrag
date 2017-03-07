package com.joy.tweetit.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.joy.tweetit.R;
import com.joy.tweetit.activity.HomeTimelineActivity;
import com.joy.tweetit.dialog.ComposeDialog;
import com.joy.tweetit.dialog.DetailDialog;
import com.joy.tweetit.model.Tweet;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by joy0520 on 2017/3/3.
 */

public class TweetsAdapter extends RecyclerView.Adapter {
    static class TweetHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name, userName, text, timeStamp;
        private CardView cardView;

        public TweetHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            text = (TextView) itemView.findViewById(R.id.text);
            timeStamp = (TextView) itemView.findViewById(R.id.time_stamp);
            cardView = (CardView) itemView.findViewById(R.id.card);
        }
    }

    public abstract static class EndlessScrollListener extends RecyclerView.OnScrollListener {
        // True if we are still waiting for the last set of data to load.
        private boolean loading = false;

        int lastVisibleItem, visibleItemCount, totalItemCount;

        private LinearLayoutManager mManager;

        public EndlessScrollListener(LinearLayoutManager manager) {
            mManager = manager;
        }

        public abstract void onLoadMore();

        public void finishLoading() {
            this.loading = false;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //check for scroll down
            if (dy > 0) {
                visibleItemCount = mManager.getChildCount();
                totalItemCount = mManager.getItemCount();
                lastVisibleItem = mManager.findLastVisibleItemPosition();
                if (HomeTimelineActivity.DEBUG) {
                    Log.i("onScrolled()", "visibleItemCount=" + visibleItemCount
                            + ", totalItemCount=" + totalItemCount
                            + ", lastVisibleItem=" + lastVisibleItem);
                }

                if (!loading) {
                    if ((visibleItemCount + lastVisibleItem) >= totalItemCount) {
                        loading = true;
                        //Do pagination.. i.e. fetch new data
                        onLoadMore();
                    }
                }
            }
        }
    }

    public interface Callback {
        void onOpenDetailDialog(Tweet tweet);
    }

    private List<Tweet> mList;
    private Context mContext;

    private Callback mCallback;

    public TweetsAdapter(Context context) {
        super();
        mContext = context;
        mList = new ArrayList<>();
    }

    public void postTweeting(Tweet tweet) {
        mList.add(0, tweet);
        saveTweets(mList);
        notifyItemInserted(0);
    }

    public void addTweets(List<Tweet> tweets) {
        mList.addAll(tweets);
        saveTweets(mList);
        notifyDataSetChanged();
    }

    public void clearAll() {
        mList.clear();
        deleteSavedTweets(mList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_tweet, parent, false);
        return new TweetHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Tweet tweet = mList.get(position);
        if (holder instanceof TweetHolder) {
            TweetHolder tweetHolder = (TweetHolder) holder;
            // Setup view content
            Glide.with(mContext)
                    .load(tweet.getProfileImageUrl())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(tweetHolder.image);

            tweetHolder.name.setText(tweet.getName());
            tweetHolder.userName.setText(String.format("@%s", tweet.getScreenName()));
            tweetHolder.text.setText(tweet.getText());
            tweetHolder.timeStamp.setText(tweet.getCreatedAtFormatString());
            // Setup click event
            tweetHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onOpenDetailDialog(tweet);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    // TODO do this on another thread
    private void saveTweets(final List<Tweet> tweets) {
        for (Tweet tweet : tweets) {
            tweet.save();
        }
    }

    private void deleteSavedTweets(final List<Tweet> tweets) {
        for (Tweet tweet : tweets) {
            tweet.delete();
        }
    }

    private List<Tweet> loadTweetsFromDB() {
        return SQLite.select().from(Tweet.class).queryList();
    }

    public void applyLocalTweets() {
        mList.clear();
        mList.addAll(loadTweetsFromDB());
        Collections.sort(mList);
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

}
