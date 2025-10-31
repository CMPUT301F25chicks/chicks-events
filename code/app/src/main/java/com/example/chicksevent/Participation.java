package com.example.chicksevent;

import java.util.HashMap;

public class Participation {

    private String entrantId;
    private String eventId;
    private EntrantStatus status;

    public Participation(String entrantId, String eventId, EntrantStatus status) {
        this.entrantId = entrantId;
        this.eventId = eventId;
        this.status = status;
    }

    public EntrantStatus getStatus() {
        return status;
    }

    public void acceptInvitation(FirebaseService service) {
        if (status == EntrantStatus.INVITED) {
            status = EntrantStatus.ACCEPTED;

            HashMap<String, Object> update = new HashMap<>();
            update.put("status", "ACCEPTED");
            service.updateSubCollectionEntry(entrantId, "participation", eventId, update);
        }
    }

    public void declineInvitation(FirebaseService service) {
        if (status == EntrantStatus.INVITED) {
            status = EntrantStatus.DECLINED;

            HashMap<String, Object> update = new HashMap<>();
            update.put("status", "DECLINED");
            service.updateSubCollectionEntry(entrantId, "participation", eventId, update);
        }
    }

    public void rejoinWaitingList(FirebaseService service) {
        if (status == EntrantStatus.UNINVITED) {
            status = EntrantStatus.WAITING;

            HashMap<String, Object> update = new HashMap<>();
            update.put("status", "WAITING");
            service.updateSubCollectionEntry(entrantId, "participation", eventId, update);
        }
    }
}
