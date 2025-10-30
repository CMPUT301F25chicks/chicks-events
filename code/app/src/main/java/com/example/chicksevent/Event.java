package com.example.chicksevent;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

public class Event {
    private Organizer organizer;
    private FirebaseService eventService;

    private Date registrationStart;
    private Date registrationEnd;
    private int entrantLimit;
    private int entrantCurrentCount;
    private String eventDetails;

    private ArrayList<Entrant> entrantList;
    private WaitingList waitingList;


    Event() {
        eventService = new FirebaseService("Event");
    }
    public void sendNotification() {

    }
}
