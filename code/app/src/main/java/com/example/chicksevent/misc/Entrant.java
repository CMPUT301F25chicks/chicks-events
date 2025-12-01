package com.example.chicksevent.misc;

import android.util.Log;

import com.example.chicksevent.enums.EntrantStatus;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

/**
 * Represents an entrant (participant) within the ChicksEvent system.
 * <p>
 * An {@code Entrant} corresponds to a user who can join or leave waiting lists for specific events,
 * and whose participation status is tracked via {@link EntrantStatus}. The class also provides helper
 * methods for updating Firebase Realtime Database entries within the "WaitingList" and "Event" roots.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Join or leave a waiting list for a given event.</li>
 *   <li>Swap between waiting list states (e.g., WAITING → INVITED).</li>
 *   <li>Provide identification of the entrant within Firebase.</li>
 * </ul>
 * </p>
 *
 * <p><b>Firebase structure:</b> Data is organized under:
 * <pre>
 * WaitingList/{eventId}/{EntrantStatus}/{entrantId}
 * </pre>
 * </p>
 *
 * @author Jordan Kwan
 */
public class Entrant extends User {

    /** Firebase wrapper for entrant-level operations. */
    private FirebaseService entrantService;

    /** Firebase wrapper for event-level operations. */
    private FirebaseService eventService;

    /** Firebase wrapper for waiting list-level operations. */
    private FirebaseService waitingListService;

    /** The event ID associated with this entrant's participation. */
    private String eventId;

    /** The unique identifier of the entrant (user ID). */
    private String entrantId;

    /** The current participation status of this entrant. */
    private EntrantStatus status;

    /** Reference to the related organizer (optional; may be null). */
    private Organizer organizer;

    /**
     * Constructs an {@code Entrant} object for the specified user and event.
     *
     * @param id the entrant's unique identifier.
     * @param eventId the ID of the event this entrant is associated with.
     */
    public Entrant(String id, String eventId) {
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

    public void setStatus(EntrantStatus status) {this.status = status;}

    /**
     * Add this entrant to the waiting list under default {@link EntrantStatus#DECLINED}.
     */
    public void declineWaitingList() {
        declineWaitingList(EntrantStatus.DECLINED);
    }

    /**
     * Add this entrant to the waiting list under default {@link EntrantStatus#ACCEPTED}.
     */
    public void acceptWaitingList() {
        acceptWaitingList(EntrantStatus.ACCEPTED);
    }

    /**
     * Adds this entrant to a waiting list node in Firebase under a specific status.
     *
     * @param status the {@link EntrantStatus} to register under (e.g., WAITING, INVITED).
     */
    public void acceptWaitingList(EntrantStatus status) {
        Log.i("RTD8", "hi accept " + eventId);

        String statusString = status.toString();
        this.status = status;

        HashMap<String, Object> data = new HashMap<>();
        data.put(" ", "");

        waitingListService.updateSubCollectionEntry(eventId, status.toString(), entrantId, data);
        waitingListService.deleteSubCollectionEntry(eventId, "INVITED", entrantId);
    }

    /**
     * Adds this entrant to a waiting list node in Firebase under a specific status.
     *
     * @param status the {@link EntrantStatus} to register under (e.g., WAITING, INVITED).
     */
    public void declineWaitingList(EntrantStatus status) {
        Log.i("RTD8", "hi decline" + eventId);

        String statusString = status.toString();
        this.status = status;

        HashMap<String, Object> data = new HashMap<>();
        data.put(" ", "");

        waitingListService.updateSubCollectionEntry(eventId, status.toString(), entrantId, data);
        waitingListService.deleteSubCollectionEntry(eventId, "INVITED", entrantId);
    }

    /**
     * Adds this entrant to the waiting list with default status {@link EntrantStatus#WAITING}.
     */
    public void joinWaitingList() {
        joinWaitingList(EntrantStatus.WAITING, null, null);
    }

    /**
     * Adds this entrant to the waiting list with default status {@link EntrantStatus#WAITING} and location.
     *
     * @param latitude the latitude of the entrant's location when joining (nullable).
     * @param longitude the longitude of the entrant's location when joining (nullable).
     */
    public void joinWaitingList(Double latitude, Double longitude) {
        joinWaitingList(EntrantStatus.WAITING, latitude, longitude);
    }

    /**
     * Removes this entrant from the waiting list under default {@link EntrantStatus#WAITING}.
     */
    public void leaveWaitingList() {
        leaveWaitingList(EntrantStatus.WAITING);
    }

    /**
     * Adds this entrant to a waiting list node in Firebase under a specific status.
     *
     * @param status the {@link EntrantStatus} to register under (e.g., WAITING, INVITED).
     */
    public void joinWaitingList(EntrantStatus status) {
        joinWaitingList(status, null, null);
    }

    /**
     * Adds this entrant to a waiting list node in Firebase under a specific status with optional location.
     *
     * @param status the {@link EntrantStatus} to register under (e.g., WAITING, INVITED).
     * @param latitude the latitude of the entrant's location when joining (nullable).
     * @param longitude the longitude of the entrant's location when joining (nullable).
     */
    public void joinWaitingList(EntrantStatus status, Double latitude, Double longitude) {
        Log.i("RTD8", "hi wtf is " + eventId);

        String statusString = status.toString();
        this.status = status;

        HashMap<String, Object> data = new HashMap<>();
        data.put(" ", ""); // Keep existing placeholder for backward compatibility
        
        // Add location data if provided
        if (latitude != null && longitude != null) {
            data.put("latitude", latitude);
            data.put("longitude", longitude);
        }

        Log.i("printing stuff", eventId + " | " + status + " | " + entrantId);

        waitingListService.updateSubCollectionEntry(eventId, status.toString(), entrantId, data);
    }

    /**
     * Removes this entrant from a waiting list node in Firebase under a specific status.
     *
     * @param status the {@link EntrantStatus} from which the entrant should be removed.
     */
    public void leaveWaitingList(EntrantStatus status) {
        Log.i("FirestoreTest", "hi");

        HashMap<String, Object> data = new HashMap<>();
        this.status = null;

        waitingListService.deleteSubCollectionEntry(eventId, status.toString(), entrantId);
    }

    /**
     * Switches this entrant's waiting list status by first removing them from their current
     * {@link EntrantStatus} node and re-adding them under a new one.
     * Location data is preserved when swapping status.
     *
     * @param newStatus the new status to apply (e.g., from WAITING to INVITED).
     */
    public void swapStatus(EntrantStatus newStatus) {
        Log.i("RTD8", "output");
        
        // Read current location from Firebase before leaving old status
        String currentStatusString = status != null ? status.toString() : "WAITING";
        waitingListService.getReference()
                .child(eventId)
                .child(currentStatusString)
                .child(entrantId)
                .get()
                .addOnCompleteListener(task -> {
                    Double latitude = null;
                    Double longitude = null;
                    
                    if (task.isSuccessful() && task.getResult() != null) {
                        DataSnapshot snapshot = task.getResult();
                        Object latObj = snapshot.child("latitude").getValue();
                        Object lngObj = snapshot.child("longitude").getValue();
                        
                        if (latObj != null && lngObj != null) {
                            try {
                                latitude = latObj instanceof Number ? 
                                    ((Number) latObj).doubleValue() : 
                                    Double.parseDouble(latObj.toString());
                                longitude = lngObj instanceof Number ? 
                                    ((Number) lngObj).doubleValue() : 
                                    Double.parseDouble(lngObj.toString());
                                Log.i("Entrant", "Preserving location: " + latitude + ", " + longitude);
                            } catch (NumberFormatException e) {
                                Log.w("Entrant", "Invalid location data when swapping status", e);
                            }
                        }
                    }
                    
                    // Now perform the swap with preserved location
                    leaveWaitingList(status);
                    joinWaitingList(newStatus, latitude, longitude);
                });
    }

    /**
     * @return always {@code false} for Entrant objects.
     */
    public Boolean isOrganizer() { return false; }
}
