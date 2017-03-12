package com.joy.tweetitdeluxe.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.joy.tweetitdeluxe.R;
import com.joy.tweetitdeluxe.TweetItApplication;
import com.joy.tweetitdeluxe.TwitterClient;
import com.joy.tweetitdeluxe.model.Tweet;
import com.loopj.android.http.TextHttpResponseHandler;

import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

/**
 * Created by joy0520 on 2017/3/5.
 */

public class DetailDialog extends DialogFragment {
    private static final String TAG = "DetailDialog";

    private ImageView mImage;
    private TextView mName, mUserName, mBody;
    private CheckBox mFavorite;

    private Tweet mTweet;

    private Callback mCallback;

    private TextHttpResponseHandler mResponseHandler = new TextHttpResponseHandler() {
        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            if (TweetItApplication.DEBUG)
                Log.d(TAG, "failed to get response when update favorite: " + responseString, throwable);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            // Update the tweet.favorited saved
            if (mCallback != null) {
                mCallback.onUpdateTweetFavorite(mTweet.getId(), mFavorite.isChecked());
            }
        }
    };

    public interface Callback {
        void onUpdateTweetFavorite(long id, boolean favorited);
    }

    public static DetailDialog newInstance(Tweet tweet) {
        DetailDialog frag = new DetailDialog();
        frag.setStyle(DialogFragment.STYLE_NORMAL, R.style.SettingDialog);
        Bundle args = new Bundle();
        args.putParcelable("tweet", Parcels.wrap(tweet));
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(getString(R.string.compose_dialog_post),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        // Unwrap tweet object
        mTweet = Parcels.unwrap(getArguments().getParcelable("tweet"));

        // Load the custom dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.dialog_tweet_detail, null, false);
        updateViews(customView);
        builder.setView(customView);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void updateViews(View customView) {
        // Init views
        mImage = (ImageView) customView.findViewById(R.id.photo);
        mName = (TextView) customView.findViewById(R.id.name);
        mUserName = (TextView) customView.findViewById(R.id.user_name);
        mBody = (TextView) customView.findViewById(R.id.body);
        mFavorite = (CheckBox) customView.findViewById(R.id.favorites);
        // Setup views content
        Glide.with(getActivity())
                .load(mTweet.getProfileImageUrl())
                .fitCenter()
                .into(mImage);
        mName.setText(mTweet.getName());
        mUserName.setText(String.format("@%s", mTweet.getScreenName()));
        mBody.setText(mTweet.getText());
        mFavorite.setChecked(mTweet.isFavorited());
        mFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TwitterClient client = TweetItApplication.getRestClient();
                if (isChecked) {
                    client.postFavoriteCreate(mTweet.getId(), mResponseHandler);
                } else {
                    client.postFavoriteDestroy(mTweet.getId(), mResponseHandler);
                }
            }
        });
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }
}
