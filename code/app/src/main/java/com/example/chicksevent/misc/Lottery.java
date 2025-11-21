package com.example.chicksevent.misc;

import android.util.Log;

import androidx.annotation.NonNull;

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

    private static final String WAITING = "WAITING";
    private static final String INVITED = "INVITED";
    private static final String UNINVITED = "UNINVITED";

    private final FirebaseService waitingListService;
    private final FirebaseService eventService;
    private final String eventId;


    public Lottery(String eventId) {
        this.eventId = eventId;
        this.waitingListService = new FirebaseService("WaitingList");
        this.eventService = new FirebaseService("Event");
    }

    /* -------------------------------------------------------
     *  Helper: Check if initial lottery already ran
     * ------------------------------------------------------- */
    private void hasInitialLotteryRun(Callback<Boolean> callback) {
        waitingListService.getReference()
                .child(eventId)
                .child(INVITED)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snap) {
                        callback.onResult(snap.exists());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Check failed", error.toException());
                        callback.onResult(false);
                    }
                });
    }

    /* -------------------------------------------------------
     *  MAIN ENTRY: draw entrants (smart behavior)
     * ------------------------------------------------------- */
    public void drawOrPool() {
        hasInitialLotteryRun(alreadyRan -> {
            if (!alreadyRan) {
                Log.i(TAG, "Initial lottery hasn't run yet → Running full lottery.");
                runLottery();
            } else {
                Log.i(TAG, "Initial lottery already ran → Running pool replacement.");
                poolReplacementAuto();
            }
        });
    }

    /* -------------------------------------------------------
     *  INITIAL LOTTERY (run once)
     * ------------------------------------------------------- */
    public void runLottery() {
        Log.i(TAG, "RunLottery() start: eventId=" + eventId);

        // Read limit
        eventService.getReference().child(eventId).child("entrantLimit")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot limitSnap) {

                        Integer limit = limitSnap.getValue(Integer.class);
                        if (limit == null) {
                            Log.e(TAG, "No entrantLimit for eventId " + eventId);
                            return;
                        }

                        // Read WAITING list
                        waitingListService.getReference()
                                .child(eventId)
                                .child(WAITING)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot waitSnap) {

                                        List<String> waiting = new ArrayList<>();
                                        for (DataSnapshot child : waitSnap.getChildren()) {
                                            waiting.add(child.getKey());
                                        }

                                        if (waiting.isEmpty()) {
                                            Log.i(TAG, "WAITING empty — nothing to run.");
                                            return;
                                        }

                                        if (limit == 0) {
                                            Log.w(TAG, "Limit is 0 → all become UNINVITED.");
                                            markAllUninvited(waiting);
                                            return;
                                        }

                                        Collections.shuffle(waiting);

                                        List<String> invited = waiting.subList(0, Math.min(limit, waiting.size()));
                                        List<String> uninvited = waiting.subList(invited.size(), waiting.size());

                                        applyStatus(invited, uninvited);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        Log.e(TAG, "Waiting read failed: " + error.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Limit read failed: " + error.getMessage());
                    }
                });
    }

    /* -------------------------------------------------------
     *  AUTOMATIC POOL: invite until full
     * ------------------------------------------------------- */
    public void poolReplacementAuto() {
        getCounts((invitedCount, limit, waitingCount) -> {

            if (limit == 0) {
                Log.w(TAG, "Cannot pool: entrantLimit = 0");
                return;
            }
            if (invitedCount >= limit) {
                Log.i(TAG, "Event already full → no pooling.");
                return;
            }
            if (waitingCount == 0) {
                Log.i(TAG, "No waiting entrants → no pooling.");
                return;
            }

            int toPool = limit - invitedCount;
            poolReplacement(toPool);
        });
    }

    /* -------------------------------------------------------
     *  REPLACEMENT POOL (add N new invited)
     * ------------------------------------------------------- */
    public void poolReplacement(int numReplacements) {

        waitingListService.getReference()
                .child(eventId)
                .child(WAITING)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot waitSnap) {

                        List<String> waiting = new ArrayList<>();
                        for (DataSnapshot ch : waitSnap.getChildren()) waiting.add(ch.getKey());

                        if (waiting.isEmpty()) {
                            Log.i(TAG, "No WAITING entries to pool from.");
                            return;
                        }

                        Collections.shuffle(waiting);

                        int actual = Math.min(numReplacements, waiting.size());
                        List<String> invited = waiting.subList(0, actual);
                        List<String> uninvited = waiting.subList(actual, waiting.size());

                        applyStatus(invited, uninvited);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Waiting load failed", error.toException());
                    }
                });
    }

    /* -------------------------------------------------------
     *  Helper: Apply invited/uninvited atomically
     * ------------------------------------------------------- */
    private void applyStatus(List<String> invited, List<String> uninvited) {
        DatabaseReference root = waitingListService.getReference();

        Map<String, Object> update = new HashMap<>();
        String base = eventId + "/";

        for (String id : invited) {
            update.put(base + INVITED + "/" + id, true);
            update.put(base + WAITING + "/" + id, null);
        }
        for (String id : uninvited) {
            update.put(base + UNINVITED + "/" + id, true);
            update.put(base + WAITING + "/" + id, null);
        }

        root.updateChildren(update, (err, ref) -> {
            if (err != null) {
                Log.e(TAG, "Update failed: " + err.getMessage());
            } else {
                Log.i(TAG, "Updated → Invited=" + invited.size() + " Uninvited=" + uninvited.size());
            }
        });
    }

    /* -------------------------------------------------------
     *  Helper: mark all uninvited
     * ------------------------------------------------------- */
    private void markAllUninvited(List<String> waiting) {
        Map<String, Object> update = new HashMap<>();
        String base = eventId + "/";

        for (String id : waiting) {
            update.put(base + UNINVITED + "/" + id, true);
            update.put(base + WAITING + "/" + id, null);
        }

        waitingListService.getReference().updateChildren(update);
    }

    /* -------------------------------------------------------
     *  Helper: get invitedCount, limit, waitingCount
     * ------------------------------------------------------- */
    private void getCounts(CountCallback cb) {
        eventService.getReference().child(eventId).child("entrantLimit")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot limitSnap) {
                        Integer limitValue = limitSnap.getValue(Integer.class);
                        // If null or zero → unlimited
                        if (limitValue == null || limitValue <= 0) {
                            limitValue = Integer.MAX_VALUE; // represent "no limit"
                        }

                        final int finalLimit = limitValue;  // <-- FIX: final copy

                        waitingListService.getReference()
                                .child(eventId)
                                .child(INVITED)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot invSnap) {
                                        int invitedCount = (int) invSnap.getChildrenCount();

                                        waitingListService.getReference()
                                                .child(eventId)
                                                .child(WAITING)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot waitSnap) {
                                                        int waitingCount = (int) waitSnap.getChildrenCount();

                                                        // Use finalLimit here
                                                        cb.onCounts(invitedCount, finalLimit, waitingCount);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError error) {}
                                                });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {}
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }


    /* -------------------------------------------------------
     *  Small callback interfaces
     * ------------------------------------------------------- */
    public interface Callback<T> {
        void onResult(T value);
    }

    public interface CountCallback {
        void onCounts(int invited, int limit, int waiting);
    }
}
