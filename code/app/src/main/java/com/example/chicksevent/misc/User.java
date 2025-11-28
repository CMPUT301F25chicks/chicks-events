package com.example.chicksevent.misc;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.chicksevent.enums.NotificationType;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

//    private ArrayList<Event> eventList;
//    private FirebaseService userService;
//    private FirebaseService eventService;
//    private FirebaseService notificationService;
//    private FirebaseService adminService;
//    private String userId;
//    private String phoneNumber;
//    private String email;
//    private String name;


    /** Optional in-memory cache of events associated with this user. */
    private ArrayList<Event> eventList;

    /** Firebase service for the "User" root. */
    private FirebaseService userService;

    /** Firebase service for the "Event" root. */
    private FirebaseService eventService;

    /** Firebase service for the "Notification" root. */
    FirebaseService notificationService;

    /** Firebase service for admin-related operations (reserved). */
    private FirebaseService adminService;

    /** Unique identifier for this user (e.g., Android ID). */
    private String userId;

    /* Optional user name */
    private String name;

    /** Optional phone number. */
    private String phoneNumber;

    /** Optional email address. */
    private String email;

    /** Whether this user has enabled notifications. Defaults to {@code true}. */
    private boolean notificationsEnabled;

    /** Whether this user is banned from creating events as an organizer. Defaults to {@code false}. */
    private boolean bannedFromOrganizer;



    String TAG = "RTD8";


    /**
     * Constructs a {@code User} bound to the provided identifier.
     *
     * @param userId unique identifier for the user (e.g., device Android ID)
     */
    public User(String userId) {
        this.userId = userId;
        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");
        notificationService = new FirebaseService("Notification");
        adminService = new FirebaseService("Admin");
        this.notificationsEnabled = true;
        this.bannedFromOrganizer = false;
    }

    @SuppressLint("NewApi")
    public ArrayList<LocalDate> getFilterDate(String filterAvailability) {
        LocalDate today = LocalDate.now();
        LocalDate filterStart;
        LocalDate filterEnd;
        ArrayList<LocalDate> filterArr = new ArrayList<>();
        if (filterAvailability.equals("This Week")) {

            filterStart = today.with(previousOrSame(SUNDAY));
            filterEnd = today.with(nextOrSame(SUNDAY));
        } else if (filterAvailability.equals("This Weekend")) {
            filterStart = today.with(nextOrSame(SATURDAY));
            filterEnd = today.with(nextOrSame(SUNDAY));
        } else if (filterAvailability.equals("Next Week")) {
            filterStart = today.with(nextOrSame(SUNDAY));
            filterEnd = today.with(nextOrSame(SUNDAY)).with(nextOrSame(SUNDAY));
        } else if (filterAvailability.equals("Next Month")) {
            LocalDate firstOfNextMonth = today
                    .plusMonths(1)
                    .withDayOfMonth(1);

            LocalDate firstOfNextNextMonth = today
                    .plusMonths(2)
                    .withDayOfMonth(1);

            filterStart = firstOfNextMonth;

            filterEnd = firstOfNextNextMonth;
        } else {
            filterStart = today;
            filterEnd = today;
        }

        filterArr.add(filterStart);
        filterArr.add(filterEnd);
        return filterArr;
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
    @SuppressLint("NewApi")
    public Task<ArrayList<String>> filterEvents(ArrayList<String> filterList, String filterAvailability) {
        Log.i(TAG, "what");
        Log.i(TAG, "e" + eventService);
        LocalDate filterStart;
        LocalDate filterEnd;
        if (filterAvailability != null) {
            ArrayList<LocalDate> filterArr = getFilterDate(filterAvailability);
            filterStart = filterArr.get(0);
            filterEnd = filterArr.get(1);
        } else {
            filterStart = null;
            filterEnd = null;
        }

        return eventService.getReference().get().continueWith(task -> {
            Log.d(TAG, "=== filtering events ===");
            ArrayList<String> eventList = new ArrayList<>();

            // Iterate through all children
            for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                String key = childSnapshot.getKey();
                String[] value = ((Map<String, String>) childSnapshot.getValue()).get("tag").split(",");
                Map<String, String> value2 = ((Map<String, String>) childSnapshot.getValue());
                boolean addEvent = false;


                Log.d(TAG, "Key: " + key);
                for (String val : value) {
                    Log.d(TAG, "Value: " + val);
                    if (filterList.contains(val)) {
                        addEvent = true;
//                        eventList.add(key);
//                        return eventList;
                    }
                }
                if (filterList.size() == 0 || filterList.contains(((Map<String, String>) childSnapshot.getValue()).get("name"))) {
//                    eventList.add(key);
                    addEvent = true;
                }

                Log.i("filter event", addEvent ? "yes" : "no");

                try {
                    if (filterStart != null && value2.get("eventStartDate") != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                        LocalDate date = LocalDate.parse(value2.get("eventStartDate"), formatter);
                        if (date.isBefore(filterStart)) addEvent = false;
                        Log.i("filter event", "set false a" );
                        Log.i("filter event", date.toString());
                        Log.i("filter event", filterStart.toString());


                    }
                    if (filterEnd != null && value2.get("eventEndDate") != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                        LocalDate date = LocalDate.parse(value2.get("eventEndDate"), formatter);
                        if (date.isAfter(filterEnd)) addEvent = false;
                        Log.i("filter event", "set false b");
                        Log.i("filter event", date.toString());
                        Log.i("filter event", filterEnd.toString());

                    }
                } catch (Exception e) {
                    Log.i("filter error", e.toString());
                    addEvent = false;
                }

                if (addEvent) {
//                    Event e = new Event("e", value.get("id"), value.get("name"),  value.get("eventDetails"), value.get("eventStartTime"), value.get("eventEndTime"), value.get("eventStartDate"), "N/A", value.get("registrationEndDate"), value.get("registrationStartDate"), 32, "N/A", value.get("tag"), false);
                    eventList.add(key);
                }

                Log.d(TAG, "---");
            }

//                Log.d(TAG, "Total children: " + dataSnapshot.getChildrenCount());
            return eventList;
        });
    }

    /**
     * @return this user's unique identifier
     */
    public String getUserId() {
        return userId;
    }

    public Task<String> getName() {
        return userService.getReference()
                .child(userId)
                .get()
                .continueWith(task -> {
                    if (task.getResult().exists()) {
                        return ((HashMap<String, String>) task.getResult().getValue()).get("name");

                    } else {
                        return "couldn't find name";
                    }
                });
    }


    /**
     * Logs all events to Logcat (diagnostic utility).
     */
    public void listEvents() {
        Log.i(TAG, "what");
        Log.i(TAG, "e" + eventService);
        eventService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "=== All Children at Root ===");

                // Iterate through all children
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
     * Sets the in-memory notifications flag.
     *
     * @param notificationsEnabled desired notifications state
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Reads the user's notifications from Firebase and materializes them into {@link Notification} objects.
     *
     * @return a task resolving to the user's notifications
     */
    public Task<ArrayList<Notification>> getNotificationList() {
        Log.i(TAG, "in notif list");
        return notificationService.getReference().child(userId).get().continueWith(task -> {
            ArrayList<Notification> notificationList = new ArrayList<Notification>();

            Log.d(TAG, "=== All Children at Root filter ===");

            for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                String eventId = childSnapshot.getKey();
                HashMap<String, HashMap<String, String>> value = (HashMap<String, HashMap<String,String>>) childSnapshot.getValue();
//                value.get("WAITING").

                Log.d(TAG, "Key: " + eventId);
                for (Map.Entry<String, HashMap<String, String>> entry : value.entrySet()) {
//                    Log.i(TAG, "Key: " + entry.getKey() + ", Value: " + entry.getValue());
//                    Log.d(TAG, "KKK: " + entry.getKey());

                    for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
//                        Log.i(TAG, "kkk2: " + entry.getKey() + ", Value: " + entry.getValue().get("message"));
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
                            case "CANCELLED":
                                notificationType = NotificationType.CANCELLED;
                                break;
                            case "SYSTEM":
                                notificationType = NotificationType.SYSTEM;
                                break;
                            default:
                                notificationType = NotificationType.WAITING;
                                break;
                        }

                        notificationList.add(new Notification(userId, eventId, notificationType, entry.getValue().get("message")));
                    }

                }

//

            }
            return notificationList;
        });
    }

    /**
     * Updates the user's profile in Firebase Realtime Database.
     * <p>
     * Validates input fields, updates local instance state, and persists changes to the
     * {@code /User/{userId}} node using only the fields that are being updated.
     * </p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>{@code userId} must be non-null and non-empty</li>
     *   <li>{@code name} and {@code email} must be non-null and non-empty after trimming</li>
     *   <li>{@code phone} is optional; if provided, it is trimmed</li>
     *   <li>{@code notification} is stored as-is</li>
     * </ul>
     *
     * @param name                the new display name (required)
     * @param email               the new email address (required)
     * @param phone               the new phone number (optional, may be {@code null})
     * @param notification        whether push notifications are enabled
     * @return a {@link Task} that completes successfully if the update is sent to Firebase,
     *         or fails with an exception if validation fails or Firebase reports an error
     */
    public boolean updateProfile(String name, String email, String phone, boolean notification) {
        // Basic validation
        if (userId == null || userId.isEmpty()) {
            System.err.println("Error: User ID is not set. Cannot update profile.");
            return false;
        }

        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            System.err.println("Error: Name and Email cannot be empty.");
            return false;
        }

        // Update the local object's properties
        this.name = name.trim();
        this.email = email.trim();
        this.phoneNumber = (phone != null) ? phone.trim() : "";
        this.notificationsEnabled = notification;

        // Create a map to send only the updated fields to Firebase
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", this.name);
        updates.put("email", this.email);
        updates.put("phoneNumber", this.phoneNumber);
        updates.put("uid", this.userId); // Store UID in the record itself
        updates.put("notificationsEnabled", this.notificationsEnabled);
        updates.put("bannedFromOrganizer", this.bannedFromOrganizer);

        // Call the editEntry method from existing FirebaseService
        userService.editEntry(userId, updates);
        return true;
    }


    public void createMockUser() {
        this.userId = "test-user-id";
        updateProfile("test-user", "test-email@gmail.com", "123-456-7890", false);
    }
    /**
     * Indicates whether this user is an admin. Default implementation returns {@code false}.
     *
     * @return {@code false} unless overridden by a derived type
     */
    public Task<Boolean> isAdmin() {
        return adminService.getReference().get().continueWith(ds -> {
            for (DataSnapshot d : ds.getResult().getChildren()) {
                Log.i("ilovechicken", d.getKey());
                if (d.getKey().equals(userId)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Indicates whether this user is an organizer. Default implementation returns {@code false}.
     *
     * @return {@code false} unless overridden by a derived type
     */
    public Boolean isOrganizer() {
        return false;
    }

    /**
     * Checks if this user is banned from creating events as an organizer.
     *
     * @return a Task that resolves to {@code true} if the user is banned, {@code false} otherwise
     */
    public Task<Boolean> isBannedFromOrganizer() {
        return userService.getReference().child(userId).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Object banned = task.getResult().child("bannedFromOrganizer").getValue();
                return banned instanceof Boolean && (Boolean) banned;
            }
            return false;
        });
    }

    /**
     * Sets the banned from organizer status locally (does not persist to Firebase).
     * Use Admin.banUserFromOrganizer() or Admin.unbanUserFromOrganizer() to persist changes.
     *
     * @param banned whether the user should be banned from organizing
     */
    public void setBannedFromOrganizer(boolean banned) {
        this.bannedFromOrganizer = banned;
    }
}
