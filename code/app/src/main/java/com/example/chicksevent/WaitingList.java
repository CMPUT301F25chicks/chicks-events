package com.example.chicksevent;
import java.util.ArrayList;

public class WaitingList {
    FirebaseService waitingListService;
    private ArrayList<Entrant> entrantList;

    WaitingList() {
        waitingListService = new FirebaseService("WaitingList");
    }
}