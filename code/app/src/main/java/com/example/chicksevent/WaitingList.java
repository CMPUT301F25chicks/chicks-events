package com.example.chicksevent;

import java.util.ArrayList;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WaitingList {
    private FirebaseService waitingListService;
    private ArrayList<Entrant> entrantList;
    private String eventId; // link to specific event
}
