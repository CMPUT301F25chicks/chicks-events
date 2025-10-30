package com.example.chicksevent;

import java.util.HashMap;

public class Participation {
    private String eventId;       // The event this participation refers to
    private String entrantId;     // The entrant participating
    private EntrantStatus status; // Current state of this participation

    public Participation(String eventId, String entrantId) {
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.status = EntrantStatus.WAITING; // default when joining waiting list
    }

    // Getter methods
    public String getEventId() { return eventId; }
    public String getEntrantId() { return entrantId; }
    public EntrantStatus getStatus() { return status; }

    // Accept invitation if the entrant was invited
    public void acceptInvitation(FirebaseService service) {
        if (status == EntrantStatus.INVITED) {
            status = EntrantStatus.ACCEPTED;

            // Update Firebase subcollection
            HashMap<String, Object> update = new HashMap<>();
            update.put("status", "ACCEPTED");
            service.updateSubCollectionEntry(entrantId, "participation", eventId, update);
        }
    }

    public void declineInvitation(FirebaseService service) {
        if (status == EntrantStatus.INVITED) {
            status = EntrantStatus.DECLINED;

            // Update Firebase subcollection
            HashMap<String, Object> update = new HashMap<>();
            update.put("status", "DECLINED");
            service.updateSubCollectionEntry(entrantId, "participation", eventId, update);
        }
    }

    public void rejoinWaitingList(FirebaseService service) {
        if (status == EntrantStatus.UNINVITED) {
            status = EntrantStatus.WAITING;
        }
        // Update Firebase subcollection
        HashMap<String, Object> update = new HashMap<>();
        update.put("status", "WAITING");
        service.updateSubCollectionEntry(entrantId, "participation", eventId, update);
    }
}
