package com.example.chicksevent;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class Organizer extends User {
    private FirebaseService organizerService;
    private FirebaseService eventService;
    private FirebaseService waitingListService;
    private String organizerId;
    private String eventId;

    Organizer(String id, String eventId) {
        super(id);
        organizerId = id;
        this.eventId = eventId;
        waitingListService = new FirebaseService("WaitingList");
        organizerService = new FirebaseService("Organizer");
        eventService = new FirebaseService("Event");
    }

    public String getOrganizerId() {
        return organizerId;
    }

//    public Task<String> getMatchingEvent(EntrantStatus status) {
//        Log.i(TAG, "org id: " + organizerId);
//
////        Log.i(TAG, "e" + eventService);
////        w
//
//        return eventService.getReference().get().continueWith(task -> {
//            Log.d(TAG, "=== All Events listing entrant ===");
//
//            // Iterate through all children
//            for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
//                String key = childSnapshot.getKey();
//                Map<String,String> obj = (Map<String,String>) childSnapshot.getValue();
//                String value = obj.get("organizer");
//
//                Log.d(TAG, "Key ev: " + key);
//                Log.d(TAG, "Value ev: " + value);
//                Log.d(TAG, "---");
//
//                if (value.compareTo(organizerId) == 0) {
//
//                    Log.d(TAG, "---" + key);
//                    Log.d(TAG, "status: " + status.toString());
////                        return key;
//
//
//                    return key;
//                }
//            }
//
////                Log.d(TAG, "Total children: " + task.getResult().getChildrenCount());
//            return null;
//        });
//
//    }

    public void listEntrants() {
        listEntrants(EntrantStatus.WAITING);
    }
    public void listEntrants(EntrantStatus status) {
        Log.i(TAG, "in here " + eventId + " " + status);
        waitingListService.getReference().child(eventId).child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "IN HERE bef");
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            Log.i(TAG, "child key: " + childSnap.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    }
                });
    }

    public void sendSelectedNotification() {

    }

    // us 02.07.01
    public void sendWaitingListNotification() {

    }

    public void cancelDidNotSignUp() {

    }

    public void createEvent() {
    }

    public void rerollAttendees() {

    }

    public void listCancelledEntrants() {

    }

    public Boolean isOrganizer() {
        return true;
    }

    public Boolean isAdmin() {
        return false;
    }

}
