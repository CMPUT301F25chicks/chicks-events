package com.example.chicksevent;

import java.util.ArrayList;

public class User {

    private ArrayList<Event> eventList;
    private FirebaseService userService;

    User() {
        userService = new FirebaseService("User");
    }

    public Boolean isAdmin() {
        return false;
    }

    public Boolean isOrganizer() {
        return false;
    }
}
