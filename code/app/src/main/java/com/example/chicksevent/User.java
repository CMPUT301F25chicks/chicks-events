package com.example.chicksevent;

import java.util.ArrayList;

public class User {

    private ArrayList<Event> eventList;
    private FirebaseService userService;
    private String userId;

    public User() {

        this.userId = "abc";
        userService = new FirebaseService("User");
        eventList = new ArrayList<>();
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
