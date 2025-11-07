package com.example.chicksevent.misc;

import android.util.Log;

import com.example.chicksevent.enums.EntrantStatus;

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

    /**
     * Adds this entrant to the waiting list with default status {@link EntrantStatus#WAITING}.
     */
    public void joinWaitingList() {
        joinWaitingList(EntrantStatus.WAITING);
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
        Log.i("RTD8", "hi wtf is " + eventId);

        String statusString = status.toString();
        this.status = status;

        HashMap<String, Object> data = new HashMap<>();
        data.put(" ", "");

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
     *
     * @param newStatus the new status to apply (e.g., from WAITING to INVITED).
     */
    public void swapStatus(EntrantStatus newStatus) {
        Log.i("RTD8", "output");
        leaveWaitingList(status);
        joinWaitingList(newStatus);
    }

    /**
     * @return always {@code false} for Entrant objects.
     */
    public Boolean isOrganizer() { return false; }
}
