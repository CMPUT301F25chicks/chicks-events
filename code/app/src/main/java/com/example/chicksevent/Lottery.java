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

/**
 * Handles selection of entrants for an event lottery.
 * <p>
 * Reads the entrant limit and current WAITING list from Firebase, then assigns entrants to
 * {@code INVITED} or {@code UNINVITED} and removes them from {@code WAITING}. The status
 * transitions are written atomically to Firebase to ensure consistency and minimize UI churn.
 * </p>
 *
 * <p><b>Firebase paths used:</b></p>
 * <ul>
 *   <li>{@code Event/{eventId}/entrantLimit}</li>
 *   <li>{@code WaitingList/{eventId}/WAITING/{uid}}</li>
 *   <li>{@code WaitingList/{eventId}/INVITED/{uid}}</li>
 *   <li>{@code WaitingList/{eventId}/UNINVITED/{uid}}</li>
 * </ul>
 *
 * <p><b>Note:</b> Authorization is not enforced here; callers should ensure only authorized
 * users can run the lottery for a given event.</p>
 *
 * @author Jinn Kasai
 */
public class Lottery {

    private static final String TAG = "Lottery";

    private static final String WAITING_NODE   = "WAITING";
    private static final String INVITED_NODE   = "INVITED";
    private static final String UNINVITED_NODE = "UNINVITED"; // or "UNSELECTED"

    private final FirebaseService waitingListService;
    private final FirebaseService eventService;
    private final String eventId;

    /**
     * Creates a lottery instance for the given event.
     *
     * @param eventId the event identifier whose waiting list will be processed
     */
    public Lottery(String eventId) {
        this.waitingListService = new FirebaseService("WaitingList");
        this.eventService = new FirebaseService("Event");
        this.eventId = eventId;
    }

    /**
     * Executes the lottery and applies status updates in a single atomic write.
     * <p>
     * On completion, eligible entrants are moved from {@code WAITING} to {@code INVITED},
     * and the remainder (if any) are moved to {@code UNINVITED}. If there are no
     * waiting entrants, no updates are performed. Errors are logged.
     * </p>
     */
    public void runLottery() {
        Log.i(TAG, "Running lottery (one-shot) for eventId=" + eventId);

        // Read entrantLimit once
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

                        // Read waiting list once
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

                                        // Decide invited/uninvited
                                        List<String> invited;
                                        List<String> uninvited = new ArrayList<>();

                                        if (waiting.size() <= limit) {
                                            invited = new ArrayList<>(waiting);
                                        } else {
                                            Collections.shuffle(waiting);
                                            invited   = new ArrayList<>(waiting.subList(0, limit));
                                            uninvited = new ArrayList<>(waiting.subList(limit, waiting.size()));
                                        }

                                        // Build atomic multi-location update under WaitingList root
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

                                        if (updates.isEmpty()) {
                                            Log.i(TAG, "No WAITING entrants; nothing to update.");
                                            return;
                                        }

                                        DatabaseReference waitingRoot = waitingListService.getReference();
                                        final List<String> invitedFinal = invited;
                                        final List<String> uninvitedFinal = uninvited;
                                        final Map<String, Object> updatesFinal = updates;

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