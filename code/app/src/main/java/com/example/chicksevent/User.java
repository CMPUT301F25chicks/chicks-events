package com.example.chicksevent;

import java.util.ArrayList;

public class User {

    private ArrayList<Event> eventList;
    private FirebaseService userService;

    private String uid;
    private String name;
    private String email;
    private String phoneNumber;


    User() {
        userService = new FirebaseService("User");
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }





    public Boolean isAdmin() {
        return  false;
    }

    public Boolean isOrganizer() {
        return false;
    }
}
