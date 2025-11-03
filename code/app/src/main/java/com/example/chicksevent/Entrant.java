package com.example.chicksevent;
import java.util.HashMap;
import java.util.Map;


public class Entrant extends User {

    private FirebaseService entrantService;
    private Map<String, Participation> participationsMap;

    Entrant() {
        super();
        entrantService = new FirebaseService("Entrant");
        participationsMap = new HashMap<>();
    }
    public void joinWaitingList(String eventId) {
        Participation p = participationsMap.get(eventId);

        // Not in map yet so create new participation with status Waiting
        if (p == null) {
            p = new Participation(eventId, this.getUserId());
            participationsMap.put(eventId, p);
        }

        p.joinWaitingList(entrantService);
    }

    public void leaveWaitingList() {

    }

    public void acceptInvitation() {

    }

    public void declineInvitation() {

    }


}
