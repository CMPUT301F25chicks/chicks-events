package com.example.chicksevent;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lottery {

    private static final String TAG = "Lottery";

    private static final String WAITING_NODE   = "WAITING";
    private static final String INVITED_NODE   = "INVITED";
    private static final String UNINVITED_NODE = "UNINVITED"; // or "UNSELECTED"

    private final FirebaseService waitingListService; // root: "WaitingList"
    private final FirebaseService eventService;       // root: "Event"
    private final String eventId;

    public Lottery(String eventId) {
        this.waitingListService = new FirebaseService("WaitingList");
        this.eventService = new FirebaseService("Event");
        this.eventId = eventId;
    }

    /** One-shot read -> decide -> single atomic write. */
    public void runLottery() {
        Log.i(TAG, "Running lottery (one-shot) for eventId=" + eventId);

        // 1) Read entrantLimit ONCE
        eventService.getReference()
                .child(eventId)
                .child("entrantLimit")
                .addListenerForSingleValueEvent(new ValueEventListener() {
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

                        // 2) Read WAITING ONCE
                        waitingListService.getReference()
                                .child(eventId)
                                .child(WAITING_NODE)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot waitSnap) {
                                        List<String> waiting = new ArrayList<>();
                                        if (waitSnap != null && waitSnap.exists()) {
                                            for (DataSnapshot ch : waitSnap.getChildren()) {
                                                waiting.add(ch.getKey()); // uid
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

                                        // 4) Build a single multi-location update
                                        //    Keys are paths relative to WaitingList root.
                                        Map<String, Object> updates = new HashMap<>();
                                        final String base = eventId + "/";

                                        for (String uid : invited) {
                                            updates.put(base + INVITED_NODE + "/" + uid, Boolean.TRUE);
                                            updates.put(base + WAITING_NODE + "/" + uid, null); // null = delete
                                        }
                                        for (String uid : uninvited) {
                                            updates.put(base + UNINVITED_NODE + "/" + uid, Boolean.TRUE);
                                            updates.put(base + WAITING_NODE + "/" + uid, null); // null = delete
                                        }

                                        // If no one was waiting, nothing to do
                                        if (updates.isEmpty()) {
                                            Log.i(TAG, "No WAITING entrants; nothing to update.");
                                            return;
                                        }

                                        // 5) Single atomic write -> avoids repeated triggers & "shaking"
                                        DatabaseReference waitingRoot = waitingListService.getReference();

// make final copies for lambda
                                        final List<String> invitedFinal = invited;
                                        final List<String> uninvitedFinal = uninvited;
                                        final Map<String, Object> updatesFinal = updates;

// use the *final* copies below
                                        waitingRoot.updateChildren(updatesFinal, (error, ref) -> {
                                            if (error != null) {
                                                Log.e(TAG, "Lottery update failed: " + error.getMessage());
                                            } else {
                                                Log.i(TAG, "Lottery update succeeded. Invited=" +
                                                        invitedFinal.size() + " Uninvited=" + uninvitedFinal.size());
                                            }
                                        });
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