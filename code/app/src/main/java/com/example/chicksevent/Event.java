package com.example.chicksevent;

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
    private String lotteryGuidelines;

    private ArrayList<Entrant> entrantList;
    private WaitingList waitingList;

    private String eventId;

    private ArrayList<String> tagList;

    Boolean checkTag(String tag) {
        return tagList.contains(tag);
    }



}
