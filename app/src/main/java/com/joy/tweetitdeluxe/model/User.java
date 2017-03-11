package com.joy.tweetitdeluxe.model;

import com.joy.tweetitdeluxe.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.parceler.Parcel;

/**
 * Created by joy0520 on 2017/3/5.
 */

@Parcel(analyze={User.class})
@Table(database = MyDatabase.class)
public class User extends BaseModel {

    @PrimaryKey
    @Column
    String name;
    @Column
    String screen_name;
    @Column
    String profile_image_url_https;
    @Column
    String description;
    @Column
    int followers_count;
    @Column
    int favourites_count;

    public String getName() {
        return name;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public String getProfile_image_url_https() {
        return profile_image_url_https;
    }

    public String getDescription() {
        return description;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public int getFavourites_count() {
        return favourites_count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public void setProfile_image_url_https(String profile_image_url_https) {
        this.profile_image_url_https = profile_image_url_https;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFollowers_count(int followers_count) {
        this.followers_count = followers_count;
    }

    public void setFavourites_count(int favourites_count) {
        this.favourites_count = favourites_count;
    }
}
