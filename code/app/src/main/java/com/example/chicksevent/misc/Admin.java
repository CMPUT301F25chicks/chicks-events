package com.example.chicksevent.misc;

import android.os.Build;
import android.util.Log;

import com.example.chicksevent.enums.NotificationType;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents an administrator user with elevated permissions within the ChicksEvent app.
 * <p>
 * Responsibilities include browsing and administratively deleting events, organizers, and entrants.
 * All read/write operations are executed against Firebase Realtime Database via {@link FirebaseService}
 * and use the Play Services {@link Task} API for asynchronous completion.
 * </p>
 *
 * <h3>Key user stories</h3>
 * <ul>
 *   <li><b>US 03.01.01</b> — Admin can delete an event.</li>
 *   <li><b>US 03.05.01</b> — Admin can browse events.</li>
 * </ul>
 *
 * <p><b>Threading / async:</b> All public methods that touch Firebase return a {@link Task}
 * which completes on the listener thread provided by the Google Tasks framework.</p>
 *
 * @author Eric Kane
 * @author Jordan Kwan
 * @author Hanh
 */
public class Admin extends User {
    /** Service wrapper scoped to the "Admin" collection/root in Firebase. */
    private final FirebaseService adminService;

    /** Service wrapper scoped to the "User" (entrant) collection/root in Firebase. */
    private final FirebaseService userService;

    /** Service wrapper scoped to the "Event" collection/root in Firebase. */
    private final FirebaseService eventsService;

    /** Service wrapper scoped to the "Organizer" collection/root in Firebase. */
    private final FirebaseService organizerService;

    private final FirebaseService imageService = new FirebaseService("Image");

    private final FirebaseService waitingListService = new FirebaseService("WaitingList");

    /**
     * Constructs an {@code Admin} for the given user ID.
     *
     * @param userId the unique identifier of this admin user (must not be {@code null}).
     * @throws NullPointerException if {@code userId} is {@code null}
     */
    public Admin(String userId) {
        super(userId);
        this.adminService = new FirebaseService("Admin");
        this.userService = new FirebaseService("User");
        this.eventsService = new FirebaseService("Event");
        this.organizerService = new FirebaseService("Organizer");
    }

    /**
     * Deletes an event from the database by its ID. (US 03.01.01)
     * <p>
     * This issues a single <em>remove</em> operation to {@code /Event/{eventId}}. If the
     * {@code eventId} is {@code null} or empty, the operation is a no-op (logged but not failed).
     * If Firebase returns an error, it will be observable via the returned task's failure listener.
     * </p>
     *
     * @param eventId the Firebase key of the event to delete; must be non-empty.
     */
    public void deleteEvent(String eventId) {
        Log.i("DEL", "gonna delete " + eventId);
        if (eventId != null && !eventId.isEmpty()) {
            eventsService.deleteEntry(eventId);
        }
    }

    /**
     * Deletes a poster from the database by its ID.
     *
     * @param eventId the Firebase key of the event poster to delete; must be non-empty.
     */
    public void deletePoster(String eventId) {
        Log.i("DEL", "gonna delete " + eventId);
        if (eventId != null && !eventId.isEmpty()) {
            imageService.deleteEntry(eventId);
        }
    }

    /**
     * Retrieves all entrant profiles from the database.
     * <p>
     * Reads the entire {@code /User} node, creates a {@link User} instance for each child
     * using the Firebase key as the user ID. Returns a list of lightweight {@link User} objects.
     * </p>
     *
     * @return a {@link Task} that resolves to a {@link List} of {@link User} objects on success.
     */
    public Task<List<User>> browseUsers() {
        return userService.getReference().get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                List<User> entrants = new ArrayList<>();
                DataSnapshot snapshot = task.getResult();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Log.i("friedchicken", child.getKey());
                    entrants.add(new User(child.getKey()));
                }
                return com.google.android.gms.tasks.Tasks.forResult(entrants);
            } else {
                return com.google.android.gms.tasks.Tasks.forException(task.getException());
            }
        });
    }

    /**
     * Retrieves all organizer profiles from the database.
     * <p>
     * Reads the entire {@code /Organizer} node, deserializes each child into an {@link Organizer}
     * object, and assigns the Firebase key as the organizer ID.
     * </p>
     *
     * @return a {@link Task} that resolves to a {@link List} of {@link Organizer} objects on success.
     */
    public Task<List<Organizer>> browseOrganizers() {
        TaskCompletionSource<List<Organizer>> tcs = new TaskCompletionSource<>();

        // Get all events and extract unique organizer IDs
        eventsService.getReference().get().addOnSuccessListener(snapshot -> {
            List<Organizer> organizers = new ArrayList<>();
            java.util.Set<String> organizerIds = new java.util.HashSet<>();

            // Collect all unique organizer IDs from events
            for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                HashMap<String, Object> eventData = (HashMap<String, Object>) eventSnapshot.getValue();
                if (eventData != null) {
                    Object organizerId = eventData.get("organizer");
                    if (organizerId != null && !organizerId.toString().isEmpty()) {
                        organizerIds.add(organizerId.toString());
                    }
                }
            }

            // Create Organizer objects for each unique organizer ID
            // Use a placeholder eventId since Organizer constructor requires it
            for (String organizerId : organizerIds) {
                Organizer organizer = new Organizer(organizerId, "");
                organizers.add(organizer);
            }

            tcs.setResult(organizers);
        }).addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }


    /**
     * Browses (reads) the admin's profile.
     * <p>
     * <b>Status:</b> Not yet implemented. Reserved for future use when admin profile schema is defined.
     * </p>
     */
    public void browseProfile() {
        // TODO: implement admin profile browsing if/when profile schema is defined.
    }

    /**
     * Retrieves all events from the database. (US 03.05.01)
     * <p>
     * Performs a one-shot read of the {@code /Event} root. Each child is expected to be a map
     * of string fields. Currently constructs {@link Event} objects using hardcoded parameter order
     * based on expected fields from the map (temporary until proper POJO mapping is implemented).
     * </p>
     *
     * @return a {@link Task} that resolves to a list of {@link Event} objects on success.
     */
    public Task<List<Event>> browseEvents() {
        return eventsService.getReference().get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                List<Event> events = new ArrayList<>();
                DataSnapshot snapshot = task.getResult();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Log.i("friedchicken", child.getKey());
                    HashMap<String, String> eventHash = (HashMap<String, String>) child.getValue();
                    if (eventHash != null) {
                        events.add(new Event(
                                "e", // placeholder or type
                                eventHash.get("id"),
                                eventHash.get("name"),
                                "d",
                                "g", // placeholder
                                "OCT/n30",
                                "s", // placeholder
                                "w", // placeholder
                                "q", // placeholder
                                "f", // placeholder
                                3,   // placeholder capacity
                                "v", // placeholder
                                "sa", // placeholder
                                false // geolocationRequired
                        ));
                    }
                }
                return com.google.android.gms.tasks.Tasks.forResult(events);
            } else {
                return com.google.android.gms.tasks.Tasks.forException(task.getException());
            }
        });
    }

    /**
     * Deletes an organizer's profile from the database.
     * <p>
     * Removes the entire node at {@code /Organizer/{organizerId}}. If the ID is {@code null}
     * or empty, the task fails immediately with an {@link IllegalArgumentException}.
     * </p>
     *
     * @param organizerId the Firebase key of the organizer to delete
     * @return a {@link Task} that completes with {@code null} on success or an exception on failure
     * @throws IllegalArgumentException if {@code organizerId} is {@code null} or empty
     */
    public Task<Void> deleteOrganizerProfile(String organizerId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (organizerId == null || organizerId.isEmpty()) {
            tcs.setException(new IllegalArgumentException("organizerId is empty"));
            return tcs.getTask();
        }

        DatabaseReference ref = organizerService.getReference().child(organizerId);
        ref.removeValue((error, ignored) -> {
            if (error == null) {
                Log.d("AdminDeleteOrganizer", "Organizer deleted successfully");
                tcs.setResult(null);
            } else {
                Log.e("AdminDeleteOrganizer", "Error deleting organizer", error.toException());
                tcs.setException(error.toException());
            }
        });

        return tcs.getTask();
    }

    /**
     * Deletes an entrant's profile from the database.
     * <p>
     * Issues a delete operation at {@code /User/{entrantId}}. No-op if ID is {@code null} or empty.
     * </p>
     *
     * @param entrantId the Firebase key of the entrant to delete
     */
    public void deleteUserProfile(String entrantId) {
        if (entrantId != null && !entrantId.isEmpty()) {
            userService.deleteEntry(entrantId);
        }
    }

    /**
     * Identifies whether this user is an organizer.
     *
     * @return always {@code false} for {@code Admin} instances
     */
    @Override
    public Boolean isOrganizer() {
        return false;
    }

    /**
     * Retrieves all events created by a specific organizer.
     *
     * @param organizerId the user ID of the organizer
     * @return a Task that resolves to a list of Event IDs created by the organizer
     */
    public Task<List<String>> getEventsByOrganizer(String organizerId) {
        return eventsService.getReference().get().continueWith(task -> {
            List<String> eventIds = new ArrayList<>();
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                for (DataSnapshot child : snapshot.getChildren()) {
                    HashMap<String, Object> eventData = (HashMap<String, Object>) child.getValue();
                    if (eventData != null) {
                        Object organizer = eventData.get("organizer");
                        if (organizer != null && organizer.toString().equals(organizerId)) {
                            eventIds.add(child.getKey());
                        }
                    }
                }
            }
            return eventIds;
        });
    }

    /**
     * Deletes an event and cleans up all related data (WaitingList, Notifications).
     * Also notifies all entrants that the event has been cancelled.
     *
     * @param eventId the ID of the event to delete
     * @param eventName the name of the event (for notification message)
     * @return a Task that completes when the deletion and cleanup are done
     */
    public Task<Void> deleteEventAndCleanup(String eventId, String eventName) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // First, get all entrants from WaitingList and notify them
        waitingListService.getReference().child(eventId).get().addOnCompleteListener(waitingListTask -> {
            if (waitingListTask.isSuccessful()) {
                DataSnapshot waitingListSnapshot = waitingListTask.getResult();
                List<String> entrantIds = new ArrayList<>();

                // Collect all entrant IDs from all status buckets
                for (DataSnapshot statusSnapshot : waitingListSnapshot.getChildren()) {
                    for (DataSnapshot entrantSnapshot : statusSnapshot.getChildren()) {
                        String entrantId = entrantSnapshot.getKey();
                        if (entrantId != null && !entrantIds.contains(entrantId)) {
                            entrantIds.add(entrantId);
                        }
                    }
                }

                // Send cancellation notifications to all entrants
                String message = "The event \"" + eventName + "\" has been cancelled.";
                for (String entrantId : entrantIds) {
                    Notification notification = new Notification(
                            entrantId,
                            eventId,
                            NotificationType.SYSTEM,
                            message
                    );
                    notification.createNotification();
                }

                // Delete WaitingList entries for this event
                waitingListService.getReference().child(eventId).removeValue();

                // Delete Notification entries for this event
                notificationService.getReference().get().addOnCompleteListener(notifTask -> {
                    if (notifTask.isSuccessful()) {
                        DataSnapshot notifSnapshot = notifTask.getResult();
                        for (DataSnapshot userSnapshot : notifSnapshot.getChildren()) {
                            notificationService.getReference()
                                    .child(userSnapshot.getKey())
                                    .child(eventId)
                                    .removeValue();
                        }
                    }
                });

                // Delete the event itself
                deleteEvent(eventId);
                deletePoster(eventId);

                tcs.setResult(null);
            } else {
                // Even if waiting list fetch fails, still delete the event
                deleteEvent(eventId);
                deletePoster(eventId);
                tcs.setResult(null);
            }
        });

        return tcs.getTask();
    }

    /**
     * Checks if an event is happening today based on its eventStartDate.
     *
     * @param eventStartDate the event start date in YYYY-MM-DD format (can be null or empty)
     * @return true if the event is happening today, false otherwise
     */
    private boolean isEventHappeningToday(String eventStartDate) {
        if (eventStartDate == null || eventStartDate.isEmpty()) {
            return false;
        }
        try {
            LocalDate today = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate eventDate = LocalDate.parse(eventStartDate, formatter);
                return eventDate.equals(today);
            }

            return false;
        } catch (Exception e) {
            Log.e("Admin", "Error parsing event date: " + eventStartDate, e);
            return false;
        }
    }

    /**
     * Checks if an event has already happened (eventStartDate is before today).
     *
     * @param eventStartDate the event start date in YYYY-MM-DD format (can be null or empty)
     * @return true if the event has already happened, false otherwise
     */
    private boolean isEventInPast(String eventStartDate) {
        if (eventStartDate == null || eventStartDate.isEmpty()) {
            return false;
        }
        try {
            LocalDate today = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate eventDate = LocalDate.parse(eventStartDate, formatter);
                return eventDate.isBefore(today);
            }
            return false;
        } catch (Exception e) {
            Log.e("Admin", "Error parsing event date: " + eventStartDate, e);
            return false;
        }
    }

    /**
     * Bans a user from creating new events as an organizer.
     * Puts all events created by the user on hold (except events happening today or events that have already happened) and notifies them of the ban.
     *
     * @param userId the ID of the user to ban
     * @param reason the reason for banning the organizer
     * @return a Task that completes when the ban is processed
     */
    public Task<Void> banUserFromOrganizer(String userId, String reason) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // Get all events created by this user
        getEventsByOrganizer(userId).addOnCompleteListener(eventsTask -> {
            if (eventsTask.isSuccessful()) {
                List<String> eventIds = eventsTask.getResult();

                // Put events on hold (except those happening today or events that have already happened)
                eventsService.getReference().get().addOnCompleteListener(allEventsTask -> {
                    if (allEventsTask.isSuccessful()) {
                        DataSnapshot allEventsSnapshot = allEventsTask.getResult();
                        List<String> eventsToNotify = new ArrayList<>(); // Store eventId|eventName pairs

                        // First pass: put events on hold and collect event info for notifications
                        for (String eventId : eventIds) {
                            DataSnapshot eventSnapshot = allEventsSnapshot.child(eventId);
                            if (eventSnapshot.exists()) {
                                HashMap<String, Object> eventData = (HashMap<String, Object>) eventSnapshot.getValue();
                                if (eventData != null) {
                                    String eventStartDate = eventData.get("eventStartDate") != null
                                            ? eventData.get("eventStartDate").toString()
                                            : null;

                                    // Skip events happening today or events that have already happened (don't touch past events)
                                    if (!isEventHappeningToday(eventStartDate) && !isEventInPast(eventStartDate)) {
                                        String eventName = eventData.get("name") != null
                                                ? eventData.get("name").toString()
                                                : "Event";

                                        // Put event on hold
                                        HashMap<String, Object> eventUpdates = new HashMap<>();
                                        eventUpdates.put("onHold", true);
                                        eventsService.editEntry(eventId, eventUpdates);

                                        // Store event info for notification
                                        eventsToNotify.add(eventId + "|" + eventName);
                                    }
                                }
                            }
                        }

                        // Second pass: collect all entrants from all events and notify them
                        final int[] completedQueries = {0};
                        final int totalEvents = eventsToNotify.size();

                        if (totalEvents == 0) {
                            // No events to process, just ban the user
                            HashMap<String, Object> updates = new HashMap<>();
                            updates.put("bannedFromOrganizer", true);
                            userService.editEntry(userId, updates);

                            Notification banNotification = new Notification(
                                    userId,
                                    "SYSTEM",
                                    NotificationType.SYSTEM,
                                    "You have been banned from creating events.\n\nReason: " + reason
                            );
                            banNotification.createNotification();
                            tcs.setResult(null);
                            return;
                        }

                        HashMap<String, String> eventIdToName = new HashMap<>();
                        HashMap<String, List<String>> eventIdToEntrants = new HashMap<>();

                        for (String eventInfo : eventsToNotify) {
                            String[] parts = eventInfo.split("\\|", 2);
                            String eventId = parts[0];
                            String eventName = parts.length > 1 ? parts[1] : "Event";
                            eventIdToName.put(eventId, eventName);
                            eventIdToEntrants.put(eventId, new ArrayList<>());

                            // Collect entrants for this event
                            waitingListService.getReference().child(eventId).get().addOnCompleteListener(waitingListTask -> {
                                synchronized (completedQueries) {
                                    if (waitingListTask.isSuccessful()) {
                                        DataSnapshot waitingListSnapshot = waitingListTask.getResult();
                                        if (waitingListSnapshot.exists()) {
                                            List<String> entrantIds = eventIdToEntrants.get(eventId);
                                            for (DataSnapshot statusSnapshot : waitingListSnapshot.getChildren()) {
                                                for (DataSnapshot entrantSnapshot : statusSnapshot.getChildren()) {
                                                    String entrantId = entrantSnapshot.getKey();
                                                    if (entrantId != null && !entrantIds.contains(entrantId)) {
                                                        entrantIds.add(entrantId);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    completedQueries[0]++;

                                    // When all queries are done, send notifications
                                    if (completedQueries[0] == totalEvents) {
                                        // Notify all entrants
                                        for (String eventIdNotify : eventIdToEntrants.keySet()) {
                                            String eventNameNotify = eventIdToName.get(eventIdNotify);
                                            List<String> entrantIds = eventIdToEntrants.get(eventIdNotify);

                                            for (String entrantId : entrantIds) {
                                                Notification onHoldNotification = new Notification(
                                                        entrantId,
                                                        eventIdNotify,
                                                        NotificationType.SYSTEM,
                                                        "The event \"" + eventNameNotify + "\" has been put on hold. You cannot join or leave the waiting list until it is restored."
                                                );
                                                onHoldNotification.createNotification();
                                            }
                                        }

                                        // Update user's banned status in Firebase
                                        HashMap<String, Object> updates = new HashMap<>();
                                        updates.put("bannedFromOrganizer", true);
                                        userService.editEntry(userId, updates);

                                        // Notify the user that they've been banned
                                        Notification banNotification = new Notification(
                                                userId,
                                                "SYSTEM",
                                                NotificationType.SYSTEM,
                                                "You have been banned from creating events. Your events have been put on hold.\n\nReason: " + reason
                                        );
                                        banNotification.createNotification();

                                        tcs.setResult(null);
                                    }
                                }
                            });
                        }
                    } else {
                        // Even if getting events fails, still ban the user
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("bannedFromOrganizer", true);
                        userService.editEntry(userId, updates);

                        Notification banNotification = new Notification(
                                userId,
                                "SYSTEM",
                                NotificationType.SYSTEM,
                                "You have been banned from creating events.\n\nReason: " + reason
                        );
                        banNotification.createNotification();

                        tcs.setResult(null);
                    }
                });
            } else {
                // Even if getting events fails, still ban the user
                HashMap<String, Object> updates = new HashMap<>();
                updates.put("bannedFromOrganizer", true);
                userService.editEntry(userId, updates);

                Notification banNotification = new Notification(
                        userId,
                        "SYSTEM",
                        NotificationType.SYSTEM,
                        "You have been banned from creating events.\n\nReason: " + reason
                );
                banNotification.createNotification();

                tcs.setResult(null);
            }
        });

        return tcs.getTask();
    }

    /**
     * Unbans a user, allowing them to create events again.
     * Takes all their events off hold and notifies them and all entrants.
     *
     * @param userId the ID of the user to unban
     * @return a Task that completes when the unban is processed
     */
    public Task<Void> unbanUserFromOrganizer(String userId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // Get all events created by this user
        getEventsByOrganizer(userId).addOnCompleteListener(eventsTask -> {
            if (eventsTask.isSuccessful()) {
                List<String> eventIds = eventsTask.getResult();

                // Get all events and restore those that are on hold
                eventsService.getReference().get().addOnCompleteListener(allEventsTask -> {
                    if (allEventsTask.isSuccessful()) {
                        DataSnapshot allEventsSnapshot = allEventsTask.getResult();
                        List<String> eventsToRestore = new ArrayList<>(); // Store eventId|eventName pairs

                        // First pass: take events off hold and collect event info for notifications
                        for (String eventId : eventIds) {
                            DataSnapshot eventSnapshot = allEventsSnapshot.child(eventId);
                            if (eventSnapshot.exists()) {
                                HashMap<String, Object> eventData = (HashMap<String, Object>) eventSnapshot.getValue();
                                if (eventData != null) {
                                    Object onHoldObj = eventData.get("onHold");
                                    boolean isOnHold = onHoldObj instanceof Boolean && (Boolean) onHoldObj;

                                    if (isOnHold) {
                                        String eventName = eventData.get("name") != null
                                                ? eventData.get("name").toString()
                                                : "Event";

                                        // Take event off hold
                                        HashMap<String, Object> eventUpdates = new HashMap<>();
                                        eventUpdates.put("onHold", false);
                                        eventsService.editEntry(eventId, eventUpdates);

                                        // Store event info for notification
                                        eventsToRestore.add(eventId + "|" + eventName);
                                    }
                                }
                            }
                        }

                        // Second pass: collect all entrants from all restored events and notify them
                        final int[] completedQueries = {0};
                        final int totalEvents = eventsToRestore.size();

                        if (totalEvents == 0) {
                            // No events to restore, just unban the user
                            HashMap<String, Object> updates = new HashMap<>();
                            updates.put("bannedFromOrganizer", false);
                            userService.editEntry(userId, updates);

                            Notification unbanNotification = new Notification(
                                    userId,
                                    "SYSTEM",
                                    NotificationType.SYSTEM,
                                    "You have been unbanned from organizing events. You can now create events again."
                            );
                            unbanNotification.createNotification();
                            tcs.setResult(null);
                            return;
                        }

                        HashMap<String, String> eventIdToName = new HashMap<>();
                        HashMap<String, List<String>> eventIdToEntrants = new HashMap<>();

                        for (String eventInfo : eventsToRestore) {
                            String[] parts = eventInfo.split("\\|", 2);
                            String eventId = parts[0];
                            String eventName = parts.length > 1 ? parts[1] : "Event";
                            eventIdToName.put(eventId, eventName);
                            eventIdToEntrants.put(eventId, new ArrayList<>());

                            // Collect entrants for this event
                            waitingListService.getReference().child(eventId).get().addOnCompleteListener(waitingListTask -> {
                                synchronized (completedQueries) {
                                    if (waitingListTask.isSuccessful()) {
                                        DataSnapshot waitingListSnapshot = waitingListTask.getResult();
                                        if (waitingListSnapshot.exists()) {
                                            List<String> entrantIds = eventIdToEntrants.get(eventId);
                                            for (DataSnapshot statusSnapshot : waitingListSnapshot.getChildren()) {
                                                for (DataSnapshot entrantSnapshot : statusSnapshot.getChildren()) {
                                                    String entrantId = entrantSnapshot.getKey();
                                                    if (entrantId != null && !entrantIds.contains(entrantId)) {
                                                        entrantIds.add(entrantId);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    completedQueries[0]++;

                                    // When all queries are done, send notifications
                                    if (completedQueries[0] == totalEvents) {
                                        // Notify all entrants
                                        for (String eventIdNotify : eventIdToEntrants.keySet()) {
                                            String eventNameNotify = eventIdToName.get(eventIdNotify);
                                            List<String> entrantIds = eventIdToEntrants.get(eventIdNotify);

                                            for (String entrantId : entrantIds) {
                                                Notification restoredNotification = new Notification(
                                                        entrantId,
                                                        eventIdNotify,
                                                        NotificationType.SYSTEM,
                                                        "The event \"" + eventNameNotify + "\" has been restored. You can now join or leave the waiting list."
                                                );
                                                restoredNotification.createNotification();
                                            }
                                        }

                                        // Update user's banned status in Firebase
                                        HashMap<String, Object> updates = new HashMap<>();
                                        updates.put("bannedFromOrganizer", false);
                                        userService.editEntry(userId, updates);

                                        // Notify the user that they've been unbanned
                                        Notification unbanNotification = new Notification(
                                                userId,
                                                "SYSTEM",
                                                NotificationType.SYSTEM,
                                                "You have been unbanned from organizing events. Your events have been restored and you can now create events again."
                                        );
                                        unbanNotification.createNotification();

                                        tcs.setResult(null);
                                    }
                                }
                            });
                        }
                    } else {
                        // Even if getting events fails, still unban the user
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("bannedFromOrganizer", false);
                        userService.editEntry(userId, updates);

                        Notification unbanNotification = new Notification(
                                userId,
                                "SYSTEM",
                                NotificationType.SYSTEM,
                                "You have been unbanned from organizing events. You can now create events again."
                        );
                        unbanNotification.createNotification();

                        tcs.setResult(null);
                    }
                });
            } else {
                // Even if getting events fails, still unban the user
                HashMap<String, Object> updates = new HashMap<>();
                updates.put("bannedFromOrganizer", false);
                userService.editEntry(userId, updates);

                Notification unbanNotification = new Notification(
                        userId,
                        "SYSTEM",
                        NotificationType.SYSTEM,
                        "You have been unbanned from organizing events. You can now create events again."
                );
                unbanNotification.createNotification();

                tcs.setResult(null);
            }
        });

        return tcs.getTask();
    }
}

/*
 * Example usage:
 *
 * Admin admin = new Admin("someUserId");
 * admin.browseEvents()
 *      .addOnSuccessListener(events -> {
 *          for (Event e : events) {
 *              Log.d("BrowseEvents", e.getName() + " (" + e.getEventStartDate() + ")");
 *          }
 *      })
 *      .addOnFailureListener(err -> Log.e("BrowseEvents", "Failed to fetch events", err));
 */