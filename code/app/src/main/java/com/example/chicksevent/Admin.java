package com.example.chicksevent;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an administrator user with elevated permissions within the ChicksEvent app.
 * <p>
 * Responsibilities include browsing and administratively deleting events. All read/write
 * operations are executed against Firebase Realtime Database via {@link FirebaseService}
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
    private FirebaseService adminService;

    /** Service wrapper scoped to the "Event" collection/root in Firebase. */
    private FirebaseService eventsService;

    /**
     * Constructs an {@code Admin} for the given user id.
     *
     * @param userId the unique identifier of this admin user (must not be {@code null}).
     */
    Admin(String userId) {
        super(userId);
        adminService = new FirebaseService("Admin");
        this.eventsService = new FirebaseService("Event");
    }

    /**
     * Deletes an event from the database by its id. (US 03.01.01)
     * <p>
     * This issues a single <em>remove</em> operation to {@code /Event/{eventId}}. If the
     * {@code eventId} is {@code null} or empty, the returned task fails with an
     * {@link IllegalArgumentException}. If Firebase returns an error, the task fails with that
     * exception; otherwise it completes successfully with {@code null} result.
     * </p>
     *
     * @param eventId the Firebase key of the event to delete; must be non-empty.
     * @return a {@link Task} that completes when the delete operation finishes.
     */
    public Task<Void> deleteEvent(String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (eventId == null || eventId.isEmpty()) {
            tcs.setException(new IllegalArgumentException("eventId is empty"));
            return tcs.getTask();
        }

        DatabaseReference ref = eventsService.getReference().child(eventId);
        ref.removeValue((DatabaseError error, DatabaseReference ignored) -> {
            if (error == null) {
                tcs.setResult(null);
            } else {
                tcs.setException(error.toException());
            }
        });
        return tcs.getTask();
    }

    /**
     * Deletes the admin's profile.
     * <p>
     * <b>Status:</b> Not yet implemented.
     * </p>
     */
    public void deleteProfile() {
        // TODO: implement admin profile deletion if/when profile schema is defined.
    }

    /**
     * Browses (reads) the admin's profile.
     * <p>
     * <b>Status:</b> Not yet implemented.
     * </p>
     */
    public void browseProfile() {
        // TODO: implement admin profile browsing if/when profile schema is defined.
    }

    /**
     * Retrieves all events from the database. (US 03.05.01)
     * <p>
     * This performs a one-shot read of the {@code /Event} root, maps children to
     * {@link Event} instances, assigns the Firebase key as {@link Event#setId(String)}, and
     * returns the list.
     * </p>
     *
     * @return a {@link Task} that resolves to a list of events on success.
     */
    public Task<List<Event>> browseEvents() {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();

        eventsService.getReference().get().addOnSuccessListener(snapshot ->{
            List<Event> list = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                Event e = child.getValue(Event.class);
                if (e != null) {
                    try { e.setId(child.getKey()); } catch (Exception ignored) {}
                    list.add(e);
                }
            }
            tcs.setResult(list);
        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Identifies this user as an admin.
     *
     * @return always {@code true} for this class.
     */
    public Boolean isAdmin() {
        return true;
    }

    /**
     * Identifies this user as an organizer.
     *
     * @return always {@code false} for this class.
     */
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