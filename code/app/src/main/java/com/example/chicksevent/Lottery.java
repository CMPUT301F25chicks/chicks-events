package com.example.chicksevent;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lottery {

    private static final String TAG = "Lottery";

    private static final String WAITING_NODE   = "WAITING";
    private static final String INVITED_NODE   = "INVITED";
    private static final String UNINVITED_NODE = "UNINVITED"; // change to "UNSELECTED" if you prefer

    private final FirebaseService waitingListService; // root at "WaitingList"
    private final FirebaseService eventService;       // root at "Event"
    private final String eventId;

    public Lottery(FirebaseService waitingListService, FirebaseService eventService, String eventId) {
        this.waitingListService = waitingListService;
        this.eventService = eventService;
        this.eventId = eventId;
    }

    /** Live read + decide + WRITE (creates nodes if missing). */
    public void runLottery() {
        Log.i(TAG, "Running lottery for eventId: " + eventId);

        // 1) Read entrantLimit
        eventService.getReference()
                .child(eventId)
                .child("entrantLimit")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot limitSnap) {
                        if (limitSnap == null || !limitSnap.exists()) {
                            Log.e(TAG, "entrantLimit missing for event " + eventId);
                            return;
                        }

                        Integer limit = limitSnap.getValue(Integer.class);
                        if (limit == null) {
                            Log.e(TAG, "entrantLimit is null for event " + eventId);
                            return;
                        }
                        Log.i(TAG, "entrantLimit = " + limit);

                        // 2) Read WAITING user IDs
                        waitingListService.getReference()
                                .child(eventId)
                                .child(WAITING_NODE)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot waitSnap) {
                                        List<String> waiting = new ArrayList<>();
                                        if (waitSnap != null && waitSnap.exists()) {
                                            for (DataSnapshot ch : waitSnap.getChildren()) {
                                                waiting.add(ch.getKey());
                                            }
                                        }
                                        Log.i(TAG, "waitingCount = " + waiting.size());

                                        // 3) Decide winners/losers
                                        List<String> invited;
                                        List<String> uninvited = new ArrayList<>();

                                        if (waiting.size() <= limit) {
                                            invited = new ArrayList<>(waiting);
                                        } else {
                                            Collections.shuffle(waiting);
                                            invited   = new ArrayList<>(waiting.subList(0, limit));
                                            uninvited = new ArrayList<>(waiting.subList(limit, waiting.size()));
                                        }

                                        // 4) WRITE: create/update INVITED/UNINVITED and remove from WAITING
                                        // INVITED
                                        for (String uid : invited) {
                                            waitingListService.getReference()
                                                    .child(eventId)
                                                    .child(INVITED_NODE)
                                                    .child(uid)
                                                    .setValue(Boolean.TRUE);        // creates if missing

                                            waitingListService.getReference()
                                                    .child(eventId)
                                                    .child(WAITING_NODE)
                                                    .child(uid)
                                                    .removeValue();                 // remove from WAITING

                                            Log.i(TAG, "INVITED → " + uid);
                                        }

                                        // UNINVITED
                                        for (String uid : uninvited) {
                                            waitingListService.getReference()
                                                    .child(eventId)
                                                    .child(UNINVITED_NODE)
                                                    .child(uid)
                                                    .setValue(Boolean.TRUE);        // creates if missing

                                            waitingListService.getReference()
                                                    .child(eventId)
                                                    .child(WAITING_NODE)
                                                    .child(uid)
                                                    .removeValue();                 // remove from WAITING

                                            Log.i(TAG, "UNINVITED → " + uid);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        Log.e(TAG, "Error reading WAITING: " + error.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error reading entrantLimit: " + error.getMessage());
                    }
                });
    }
}
