package com.joy.tweetitdeluxe.model;

import android.support.annotation.NonNull;

import com.joy.tweetitdeluxe.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by joy0520 on 2017/3/3.
 */

@Parcel
@Table(database = MyDatabase.class)
public class Tweet extends BaseModel implements Comparable<Tweet> {

    @PrimaryKey
    @Column
    private long id;
    @Column
    private String created_at;
    @Column
    private String text;
    @ForeignKey(saveForeignKeyModel = true)
    @Column
    private User user;
    @Column
    private boolean mentionsMe;
    @Column
    private boolean favorited;

    private UserMentions[] user_mentions;

    public String getText() {
        return text;
    }

    public String getCreatedAtFormatString() {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        Date date;
        try {
            date = format.parse(created_at);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        GregorianCalendar current = new GregorianCalendar();
        GregorianCalendar created = new GregorianCalendar();
        created.setTime(date);

        DateTime currentDT = new DateTime(current.getTimeInMillis());
        DateTime createdDT = new DateTime(created.getTimeInMillis());
        Duration diff = new Duration(createdDT, currentDT);


        return String.format(Locale.US, "%d h, %d m",
                diff.getStandardHours(),
                diff.getStandardMinutes() % 60);
    }

    public int getCreatedAgoMinutes() {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        Date date;
        try {
            date = format.parse(created_at);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        GregorianCalendar current = new GregorianCalendar();
        GregorianCalendar created = new GregorianCalendar();
        created.setTime(date);

        DateTime currentDT = new DateTime(current.getTimeInMillis());
        DateTime createdDT = new DateTime(created.getTimeInMillis());
        return new Duration(createdDT, currentDT).toStandardMinutes().getMinutes();
    }

    public String getCreated_at() {
        return created_at;
    }

    public User getUser() {
        return user;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return user.name;
    }

    public String getScreenName() {
        return user.screen_name;
    }

    public String getProfileImageUrl() {
        return user.profile_image_url_https;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isMentionsMe() {
        return mentionsMe;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMentionsMe(boolean mentionsMe) {
        this.mentionsMe = mentionsMe;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public Tweet checkMentionsUser(String screenName) {
        this.mentionsMe = getMentionsScreenNames().contains(screenName);
        return this;
    }

    public List<String> getMentionsScreenNames() {
        ArrayList<String> screenNames = new ArrayList<>();
        for (UserMentions mentions : user_mentions) {
            screenNames.add(mentions.screen_name);
        }
        return screenNames;
    }

    @Override
    public int compareTo(@NonNull Tweet o) {
        return getCreatedAgoMinutes() - o.getCreatedAgoMinutes();
    }
}
