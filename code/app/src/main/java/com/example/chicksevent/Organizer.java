package com.example.chicksevent;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents an organizer user who manages events and their waiting lists.
 * <p>
 * Provides utilities to list entrants by status (e.g., WAITING/INVITED) and to broadcast
 * {@link Notification} messages to entrants in a given bucket. Organizer identity is derived
 * from the supplied user id, and actions are scoped to a specific {@code eventId}.
 * </p>
 *
 * <p><b>Firebase roots used:</b>
 * <ul>
 *   <li><code>WaitingList</code> — read buckets and notify entrants</li>
 *   <li><code>Organizer</code> — reserved for organizer-specific data</li>
 *   <li><code>Event</code> — reserved for event reads/writes</li>
 * </ul>
 * </p>
 *
 * <p><b>Note:</b> This class performs no authorization checks; callers should ensure only
 * permitted users invoke organizer actions.</p>
 *
 * @author Jordan Kwan
 * @author Jinn Kasai
 * @author Hanh
 * @author Dung
 */

/**
 * Represents an organizer user who manages events and their waiting lists.
 * <p>
 * Provides utilities to list entrants by status (e.g., WAITING/INVITED) and to broadcast
 * {@link Notification} messages to entrants in a given bucket. Organizer identity is derived
 * from the supplied user id, and actions are scoped to a specific {@code eventId}.
 * </p>
 *
 * <p><b>Firebase roots used:</b>
 * <ul>
 *   <li><code>WaitingList</code> — read buckets and notify entrants</li>
 *   <li><code>Organizer</code> — reserved for organizer-specific data</li>
 *   <li><code>Event</code> — reserved for event reads/writes</li>
 * </ul>
 * </p>
 *
 * <p><b>Note:</b> This class performs no authorization checks; callers should ensure only
 * permitted users invoke organizer actions.</p>
 *
 * @author Jordan Kwan
 * @author Jinn Kasai
 * @author Hanh
 * @author Dung
 */
public class Organizer extends User {

    /** Firebase service for organizer-specific operations (root: "Organizer"). */
    private FirebaseService organizerService;

    /** Firebase service for event operations (root: "Event"). */
    private FirebaseService eventService;

    /** Firebase service for waiting list operations (root: "WaitingList"). */
    private FirebaseService waitingListService;
    private FirebaseService userService;

    /** The organizer's user id. */
    private String organizerId;

    /** The event id this organizer instance is operating on. */
    private String eventId;

    /**
     * Creates an organizer bound to a specific event context.
     *
     * @param id the organizer's user id
     * @param eventId the event identifier to operate on
     */
    Organizer(String id, String eventId) {
        super(id);
        organizerId = id;
        this.eventId = eventId;
        waitingListService = new FirebaseService("WaitingList");
        organizerService = new FirebaseService("Organizer");
        eventService = new FirebaseService("Event");
        userService = new FirebaseService("User");
    }

    /**
     * Returns the organizer's user id.
     *
     * @return the organizer id
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Lists entrants in the default bucket ({@link EntrantStatus#WAITING}).
     */
    public void listEntrants() {
        listEntrants(EntrantStatus.WAITING);
    }

    /**
     * Lists entrants for the given status bucket under this event's waiting list.
     * Results are logged via Logcat.
     *
     * @param status the entrant status bucket to read
     */
    public void listEntrants(EntrantStatus status) {
        Log.i(TAG, "Listing entrants for event=" + eventId + " status=" + status);
        waitingListService.getReference().child(eventId).child(status.toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "Received entrants for status=" + status);
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            Log.i(TAG, "entrant uid: " + childSnap.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    }
                });
    }

    /**
     * Sends a notification to all entrants currently INVITED for this event.
     *
     * @param message the notification message body
     */

    public void sendWaitingListNotification(String message) {
        sendWaitingListNotification(EntrantStatus.WAITING, message);
    }

    /**
     * Broadcasts a {@link Notification} to all entrants in the specified status bucket.
     *
     * @param status the waiting-list bucket whose entrants should be notified
     * @param message the notification message body
     */
    public void sendWaitingListNotification(EntrantStatus status, String message) {
        sendWaitingListNotificationHelper(status, message).addOnCompleteListener(task -> {
            ArrayList<Notification> notifications = task.getResult();

            userService.getReference().get().addOnCompleteListener(userTask -> {
                if (!userTask.isSuccessful() || userTask.getResult() == null) {
                    Log.e("Notification", "Failed to get user data", userTask.getException());
                    return;
                }

                DataSnapshot usersSnapshot = userTask.getResult();

                for (Notification notif : notifications) {
                    DataSnapshot userSnap = usersSnapshot.child(notif.getUserId());
                    if (userSnap.exists()) {
                        Object enabled = userSnap.child("notificationsEnabled").getValue();
                        if (enabled instanceof Boolean && (Boolean) enabled) {
                            notif.createNotification();
                        }
                    }
                }
            });
        });
    }

    public Task<ArrayList<Notification>> sendWaitingListNotificationHelper(EntrantStatus status, String message) {
        return waitingListService.getReference().child(eventId).child(status.toString()).get().continueWith(t -> {
            ArrayList<Notification> notifList = new ArrayList<>();
            for (DataSnapshot childSnap : t.getResult().getChildren()) {
                NotificationType notifType;
                switch (status) {
                    case WAITING:
                        notifType = NotificationType.WAITING;
                        break;
                    case INVITED:
                        notifType = NotificationType.INVITED;
                        break;
                    default:
                        notifType = NotificationType.UNINVITED;
                }

                Notification n = new Notification(childSnap.getKey(), eventId, notifType, message);
                notifList.add(n);


            }

            return notifList;
        });
    }


        /** Placeholder for future cancellation logic for no-shows. */
    public void cancelDidNotSignUp() { }

    /** Placeholder for future event-creation logic. */
    public void createEvent() { }

    /** Placeholder for future re-roll/lottery logic. */
    public void rerollAttendees() { }

    /** Placeholder for future listing of cancelled entrants. */
    public void listCancelledEntrants() { }

    /** @return {@code true} because this user is an organizer. */
    public Boolean isOrganizer() { return true; }

    public void setOrganizerId(String key) {
        organizerId = key;
    }
}