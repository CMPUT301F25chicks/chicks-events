package com.example.chicksevent;

import java.util.HashMap;
import java.util.Map;

public class Entrant extends User {

    FirebaseService entrantService;
    Map<String, Participation> participationsMap; // eventId → Participation
    Entrant() {
        super();
        entrantService = new FirebaseService("Entrant");
        participationsMap = new HashMap<>();
    }

    // Constructor for unit testing with mock
    public Entrant(FirebaseService service, Map<String, Participation> participation) {
        super();
        this.entrantService = service;
        this.participationsMap = participation;
    }

    public void joinWaitingList() {

    }

    public void leaveWaitingList() {

    }

    public void acceptInvitation(String eventId) {
        Participation p = participationsMap.get(eventId);
        if (p != null) {
            p.acceptInvitation(entrantService);
        }
    }

    public void declineInvitation(String eventId) {
        Participation p = participationsMap.get(eventId);
        if (p != null) {
            p.declineInvitation(entrantService);
        }
    }

    public void rejoinWaitingList(String eventId) {
        Participation p = participationsMap.get(eventId);
        if (p != null) {
            p.rejoinWaitingList(entrantService);
        }
    }

}
