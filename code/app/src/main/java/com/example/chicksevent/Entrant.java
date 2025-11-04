package com.example.chicksevent;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Entrant extends User {

    private FirebaseService entrantService;
    private FirebaseService eventService;
    private FirebaseService waitingListService;
    private String eventId;       // The event this participation refers to
    private String entrantId;     // The entrant participating
    private EntrantStatus status; // Current state of this participation
    public int _waitingListSize = -1;

    Entrant(String id, String eventId) {
        super(id);
        eventService = new FirebaseService("Event");
        entrantService = new FirebaseService("Entrant");
        waitingListService = new FirebaseService("WaitingList");
        this.eventId = eventId;
        this.entrantId = id;
        this.status = EntrantStatus.WAITING; // default when joining waiting list
    }
    // Getter methods
    public String getEventId() { return eventId; }
    public String getEntrantId() { return entrantId; }
    public EntrantStatus getStatus() { return status; }

    public Task<Integer> updateWaitingListSize(EntrantStatus status) {
        Log.i(TAG, "what");
//        Log.i(TAG, "e" + eventService);
        final int[] count = new int[1];
        count[0] = -2;
        Log.i("RTD8", eventId);
//        eventService.getReference().get().continueWith
        return waitingListService.getReference().child(eventId).child(status.toString()).get().continueWith(task -> {
            Log.d(TAG, "=== All Children at Root ===");



            Log.d(TAG, "Total children: " + task.getResult().getChildrenCount());
//                count[0] = (int) dataSnapshot.getChildrenCount();
            _waitingListSize =  (int) task.getResult().getChildrenCount();
            return _waitingListSize;
        });

//        return count[0];
    }

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
        waitingListService.updateSubCollectionEntry(eventId, status.toString(), entrantId, data);
//        service.updateSubCollectionEntry(entrantId, "participation", eventId, data);
    }

    public void leaveWaitingList(EntrantStatus status) {
        Log.i("FirestoreTest", "hi");

        // WaitingList/event-name/list-type/entrant-id
        HashMap<String, Object> data = new HashMap<>();
        this.status = null;
//        data.put("bruh", "moment");

//        Log.i("FirestoreTest", entrantId == null ? "what" : entrantId);
        waitingListService.deleteSubCollectionEntry(eventId, status.toString(), entrantId);
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
