package com.joy.tweetitdeluxe.model;

/**
 * Created by joy0520 on 2017/3/12.
 */

public class UserListResponse {
    private User[] users;
    private long next_cursor;

    public User[] getUsers() {
        return users;
    }

    public long getNext_cursor() {
        return next_cursor;
    }
}
