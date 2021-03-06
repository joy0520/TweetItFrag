package com.joy.tweetitdeluxe.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.activity.HomeTimelineActivity;
import com.joy.tweetitdeluxe.activity.ProfileActivity;
import com.joy.tweetitdeluxe.model.Tweet;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.Collections;
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
            image = (ImageView) itemView.findViewById(R.id.photo);
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
        void onItemClicked(Tweet tweet);
    }

    private List<Tweet> mTweets;
    private Context mContext;

    private Callback mCallback;

    public TweetsAdapter(Context context) {
        super();
        mContext = context;
        mTweets = new ArrayList<>();
    }

    public void postANewTweet(Tweet tweet) {
        mTweets.add(0, tweet);
        saveTweets(mTweets);
        notifyItemInserted(0);
    }

    public void updateTweetFavorited(long id, boolean favortied) {
        int position = -1;
        for (Tweet tweet : mTweets) {
            if (tweet.getId() == id) {
                tweet.setFavorited(favortied);
                tweet.save();
                position = mTweets.indexOf(tweet);
            }
        }
        if (position != -1) notifyItemChanged(position);
    }

    public void addTweets(List<Tweet> tweets) {
        mTweets.addAll(tweets);
        saveTweets(mTweets);
        notifyDataSetChanged();
    }

    public void clearAll() {
        mTweets.clear();
        deleteSavedTweets(mTweets);
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
        final Tweet tweet = mTweets.get(position);
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
                        mCallback.onItemClicked(tweet);
                    }
                }
            });
            tweetHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra("screen_name", tweet.getScreenName());
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
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

    public List<Tweet> loadTweetsFromDB() {
        return SQLite.select().from(Tweet.class).queryList();
    }

    public void applyLocalTweets() {
        mTweets.clear();
        mTweets.addAll(loadTweetsFromDB());
        Collections.sort(mTweets);
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

}
