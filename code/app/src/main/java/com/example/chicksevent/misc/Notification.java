package com.example.chicksevent.misc;

import com.example.chicksevent.enums.NotificationType;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

/**
 * Represents a user notification related to an event.
 * <p>
 * Each notification is linked to a user and event, and records a {@link NotificationType}
 * describing the context (e.g., invitation, update, cancellation).
 * The class provides functionality to create and upload notification data
 * to Firebase under the <code>Notification</code> root.
 * </p>
 *
 * <p><b>Firebase path used:</b> {@code Notification/{userId}/{eventId}/{notificationType}}</p>
 *
 * @author Jordan Kwan
 */
public class Notification {

    /** The unique identifier of the user receiving the notification. */
    private String userId;

    /** The unique identifier of the event associated with this notification. */
    private String eventId;

    /** The type of this notification (e.g., INVITE, UPDATE). */
    private NotificationType notificationType;

    /** The message body or content of the notification. */
    private String message;

    private String eventName;

    /** Firebase service for performing notification-related database operations. */
    private FirebaseService notificationService;
    private FirebaseService eventService;

    /**
     * Constructs a new {@code Notification} for a specific user and event.
     *
     * @param userId the identifier of the user receiving the notification
     * @param eventId the identifier of the related event
     * @param notificationType the type of notification to send
     * @param message the message content of the notification
     */
    Notification(String userId, String eventId, NotificationType notificationType, String message) {
        notificationService = new FirebaseService("Notification");
        eventService = new FirebaseService("Event");
        this.userId = userId;
        this.eventId = eventId;
        this.notificationType = notificationType;
        this.message = message;
    }

    /**
     * Creates and uploads this notification to Firebase under the appropriate user and event node.
     * <p>
     * The data includes the notification message and type.
     * </p>
     */
    public void createNotification() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("message", message);
        notificationService.updateSubCollectionEntry(userId, eventId, notificationType.toString(), data);
    }

    /**
     * Returns the type of this notification.
     *
     * @return the {@link NotificationType}
     */
    public NotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * Returns the ID of the associated event.
     *
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    public Task<String> getEventName() {
        return eventService.getReference().get().continueWith(task -> {
//            eventName =
            for (DataSnapshot ds : task.getResult().getChildren()) {
                if (ds.getKey().equals(eventId)) {
                    return ((HashMap<String, String>) ds.getValue()).get("name");
                }
            }
            return "NO NAME";
        });
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }
}