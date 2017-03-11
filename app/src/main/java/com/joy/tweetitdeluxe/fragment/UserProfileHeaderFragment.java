package com.joy.tweetitdeluxe.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.model.User;

import org.parceler.Parcels;

/**
 * Created by joy0520 on 2017/3/11.
 */

public class UserProfileHeaderFragment extends Fragment {
    public static final String ARG_USER = "ARG_USER";

    private ImageView mPhoto;
    private TextView mName, mDescription, mFollower, mFollowing;

    private User mUser;

    public static UserProfileHeaderFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, Parcels.wrap(user));
        UserProfileHeaderFragment fragment = new UserProfileHeaderFragment();
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
            mUser = Parcels.unwrap(getArguments().getParcelable(ARG_USER));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_header, container, false);

        // Init views
        mPhoto = (ImageView) view.findViewById(R.id.photo);
        mName = (TextView) view.findViewById(R.id.name);
        mDescription = (TextView) view.findViewById(R.id.description);
        mFollower = (TextView) view.findViewById(R.id.follower);
        mFollowing = (TextView) view.findViewById(R.id.following);

        // Setup views
        if (mUser != null) {
            Glide.with(getContext())
                    .load(mUser.getProfile_image_url_https())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mPhoto);
            mName.setText(mUser.getName());
            mDescription.setText(mUser.getDescription());
            mFollower.setText(mUser.getFollowers_count() + " followers");
            mFollowing.setText(mUser.getFavourites_count() + " following");
        }

        return view;
    }
}
