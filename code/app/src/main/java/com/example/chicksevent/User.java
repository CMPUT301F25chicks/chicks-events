package com.example.chicksevent;

import java.util.ArrayList;

public class User {
    protected ArrayList<Event> eventList;
    protected EntryStore userStore;

    // Production path (real Firebase)
    public User() {
        this(new FirebaseService("User"));
    }

    // Test-friendly path
    protected User(EntryStore store) {
        this.userStore = store;
    }
}
