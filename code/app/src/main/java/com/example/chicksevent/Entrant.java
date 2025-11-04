package com.example.chicksevent;

import android.util.Log;

import java.util.HashMap;

public class Entrant extends User {

    private FirebaseService entrantService;
    private FirebaseService waitingList;
    private String eventId;       // The event this participation refers to
    private String entrantId;     // The entrant participating
    private EntrantStatus status; // Current state of this participation

    Entrant(String id, String eventId) {
        super(id);
        entrantService = new FirebaseService("Entrant");
        waitingList = new FirebaseService("WaitingList");
        this.eventId = eventId;
        this.entrantId = id;
        this.status = EntrantStatus.WAITING; // default when joining waiting list
    }
    // Getter methods
    public String getEventId() { return eventId; }
    public String getEntrantId() { return entrantId; }
    public EntrantStatus getStatus() { return status; }

    public void joinWaitingList() {
        joinWaitingList(EntrantStatus.WAITING);
    }

    public void leaveWaitingList() {
        leaveWaitingList(EntrantStatus.WAITING);
    }
    public void joinWaitingList(EntrantStatus status) {
        Log.i("FirestoreTest", "hi");

        String statusString = status.toString();
        this.status = status;

        // WaitingList/event-name/list-type/entrant-id
        HashMap<String, Object> data = new HashMap<>();
        data.put(" ", "");

//        Log.i("FirestoreTest", entrantId == null ? "what" : entrantId);
        waitingList.updateSubCollectionEntry(eventId, status.toString(), entrantId, data);
//        service.updateSubCollectionEntry(entrantId, "participation", eventId, data);
    }

    public void leaveWaitingList(EntrantStatus status) {
        Log.i("FirestoreTest", "hi");

        // WaitingList/event-name/list-type/entrant-id
        HashMap<String, Object> data = new HashMap<>();
        this.status = null;
//        data.put("bruh", "moment");

//        Log.i("FirestoreTest", entrantId == null ? "what" : entrantId);
        waitingList.deleteSubCollectionEntry(eventId, status.toString(), entrantId);
//        service.updateSubCollectionEntry(entrantId, "participation", eventId, data);
    }

    public void swapStatus(EntrantStatus newStatus) {
        Log.i("RTD8", "output");
        leaveWaitingList(status);
        joinWaitingList(newStatus);
    }

    public void acceptInvitation() {

    }

    public void declineInvitation() {

    }

    public Boolean isOrganizer() {
        return false;
    }

    public Boolean isAdmin() {
        return false;
    }

}
