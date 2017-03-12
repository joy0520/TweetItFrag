package com.joy.tweetitdeluxe.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.TweetItApplication;
import com.joy.tweetitdeluxe.activity.HomeTimelineActivity;
import com.joy.tweetitdeluxe.activity.ProfileActivity;
import com.joy.tweetitdeluxe.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by joy0520 on 2017/3/12.
 */

public class UsersAdapter extends RecyclerView.Adapter {
    static class UserHolder extends RecyclerView.ViewHolder {
        private ImageView photo;
        private TextView name, screenName, description;

        public UserHolder(View view) {
            super(view);
            photo = (ImageView) view.findViewById(R.id.photo);
            name = (TextView) view.findViewById(R.id.name);
            screenName = (TextView) view.findViewById(R.id.user_name);
            description = (TextView) view.findViewById(R.id.description);
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
                    if (TweetItApplication.DEBUG) {
                        Log.i("onScrolled()", "visibleItemCount=" + visibleItemCount
                                + ", totalItemCount=" + totalItemCount
                                + ", lastVisibleItem=" + lastVisibleItem);
                    }
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
        void onItemClicked(User user);
    }

    private Context mContext;
    private List<User> mUsers;

    public UsersAdapter(Context context) {
        super();
        mContext = context;
        mUsers = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        if (holder instanceof UserHolder) {
            UserHolder userHolder = (UserHolder) holder;
            // Setup view content
            Glide.with(mContext)
                    .load(user.getProfile_image_url_https())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userHolder.photo);
            userHolder.name.setText(user.getName());
            userHolder.screenName.setText(user.getScreen_name());
            userHolder.description.setText(user.getDescription());
            // Setup click event
            userHolder.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra("screen_name", user.getScreen_name());
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void addUsers(List<User> newUsers) {
        mUsers.addAll(newUsers);
        notifyDataSetChanged();
    }

    public void clearUsers() {
        mUsers.clear();
        notifyDataSetChanged();
    }
}
