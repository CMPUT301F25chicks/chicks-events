package com.example.chicksevent.misc;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

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

        organizerService.getReference().get().addOnSuccessListener(snapshot -> {
            List<Organizer> organizers = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                Organizer o = child.getValue(Organizer.class);
                if (o != null) {
                    try {
                        o.setOrganizerId(child.getKey());
                    } catch (Exception ignored) {
                        // Ignore if setter fails (e.g., no such method)
                    }
                    organizers.add(o);
                }
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
                                "g", // placeholder
                                "s", // placeholder
                                "w", // placeholder
                                "q", // placeholder
                                "f", // placeholder
                                3,   // placeholder capacity
                                "v", // placeholder
                                "sa" // placeholder
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