package com.example.chicksevent;

import java.util.HashMap;
import java.util.Map;

public class Entrant extends User {

    private FirebaseService entrantService;
    private Map<String, Participation> participationsMap; // eventId → Participation
    Entrant() {
        super();
        entrantService = new FirebaseService("Entrant");
        participationsMap = new HashMap<>();
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
