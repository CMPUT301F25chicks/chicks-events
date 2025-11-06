package com.example.chicksevent;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing an app user and related operations.
 * <p>
 * Provides convenience methods for event discovery (filtering by tags), reading notifications,
 * and persisting user preferences (e.g., notification opt-in). Firebase CRUD operations are
 * delegated to {@link FirebaseService} wrappers for the corresponding roots.
 * </p>
 *
 * <p><b>Firebase roots used:</b>
 * <ul>
 *   <li><code>User</code> — user profile & preferences</li>
 *   <li><code>Event</code> — event catalog (read for filtering)</li>
 *   <li><code>Notification</code> — per-user notification tree</li>
 * </ul>
 * </p>
 *
 * <p><b>Note:</b> This class does not enforce authorization; callers should ensure appropriate
 * access control before invoking read/write operations tied to a user.</p>
 *
 * @author Jordan Kwan
 * @author Juan Rea
 * @author Eric Kane
 * @author Jinn Kasai
 * @author Hanh
 * @author Dung
 */
public class User {

    /** Optional in-memory cache of events associated with this user. */
    private ArrayList<Event> eventList;

    /** Firebase service for the "User" root. */
    private FirebaseService userService;

    /** Firebase service for the "Event" root. */
    private FirebaseService eventService;

    /** Firebase service for the "Notification" root. */
    private FirebaseService notificationService;

    /** Firebase service for admin-related operations (reserved). */
    private FirebaseService adminService;

    /** Unique identifier for this user (e.g., Android ID). */
    private String userId;

    /** Optional display name. */
    private String username;

    /** Optional phone number. */
    private String phoneNumber;

    /** Optional email address. */
    private String email;

    /** Whether this user has enabled notifications. Defaults to {@code true}. */
    private boolean notificationsEnabled;

    /** Log tag. */
    String TAG = "RTD8";

    /**
     * Constructs a {@code User} bound to the provided identifier.
     *
     * @param userId unique identifier for the user (e.g., device Android ID)
     */
    User(String userId) {
        this.userId = userId;
        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");
        notificationService = new FirebaseService("Notification");
        this.notificationsEnabled = true;
    }

    /**
     * Returns a list of event IDs whose tags match any of the provided filter tokens.
     * <p>
     * The filter is applied against each event's {@code tag} field (space-separated tokens).
     * </p>
     *
     * @param filterList case-sensitive tokens to match against event tags
     * @return a task resolving to a list of matching event IDs
     */
    public Task<ArrayList<String>> filterEvents(ArrayList<String> filterList) {
        Log.i(TAG, "filterEvents invoked");
        return eventService.getReference().get().continueWith(task -> {
            Log.d(TAG, "=== Filtering events by tag ===");
            ArrayList<String> eventList = new ArrayList<>();
            for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                String key = childSnapshot.getKey();
                String[] value = ((Map<String, String>) childSnapshot.getValue()).get("tag").split(" ");
                for (String val : value) {
                    if (filterList.contains(val)) {
                        eventList.add(key);
                    }
                }
            }
            return eventList;
        });
    }

    /**
     * @return this user's unique identifier
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Logs all events to Logcat (diagnostic utility).
     */
    public void listEvents() {
        Log.i(TAG, "listEvents");
        eventService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "=== All Children at Root ===");
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    Object value = childSnapshot.getValue();
                    Log.d(TAG, "Key: " + key);
                    Log.d(TAG, "Value: " + value);
                    Log.d(TAG, "---");
                }
                Log.d(TAG, "Total children: " + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error reading data: " + databaseError.getMessage());
            }
        });
    }

    /**
     * @return whether notifications are enabled for this user
     */
    public boolean areNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets the in-memory notifications flag.
     *
     * @param notificationsEnabled desired notifications state
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Persists the user's notification preference to Firebase.
     *
     * @param isEnabled {@code true} to enable notifications; {@code false} to disable
     */
    public void updateNotificationPreference(boolean isEnabled) {
        this.setNotificationsEnabled(isEnabled);
        HashMap<String, Object> update = new HashMap<>();
        update.put("notificationsEnabled", isEnabled);
        userService.editEntry(userId, update);
    }

    /**
     * Reads the user's notifications from Firebase and materializes them into {@link Notification} objects.
     *
     * @return a task resolving to the user's notifications
     */
    public Task<ArrayList<Notification>> getNotificationList() {
        Log.i(TAG, "getNotificationList");
        return notificationService.getReference().child(userId).get().continueWith(task -> {
            ArrayList<Notification> notificationList = new ArrayList<>();
            for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                String eventId = childSnapshot.getKey();
                HashMap<String, HashMap<String, String>> value =
                        (HashMap<String, HashMap<String, String>>) childSnapshot.getValue();
                for (Map.Entry<String, HashMap<String, String>> entry : value.entrySet()) {
                    NotificationType notificationType;
                    switch (entry.getKey()) {
                        case "WAITING":
                            notificationType = NotificationType.WAITING;
                            break;
                        case "INVITED":
                            notificationType = NotificationType.INVITED;
                            break;
                        case "UNINVITED":
                            notificationType = NotificationType.UNINVITED;
                            break;
                        default:
                            notificationType = NotificationType.WAITING;
                            break;
                    }
                    notificationList.add(new Notification(userId, eventId, notificationType,
                            entry.getValue().get("message")));
                }
            }
            return notificationList;
        });
    }

    /**
     * Indicates whether this user is an admin. Default implementation returns {@code false}.
     *
     * @return {@code false} unless overridden by a derived type
     */
    public Boolean isAdmin() { return false; }

    /**
     * Indicates whether this user is an organizer. Default implementation returns {@code false}.
     *
     * @return {@code false} unless overridden by a derived type
     */
    public Boolean isOrganizer() { return false; }
}
