package com.example.chicksevent;

/**
 * Defines the types of notifications that can be sent to a user.
 * <p>
 * Each type corresponds to a specific event-related status update, allowing the app
 * to categorize and display notifications appropriately.
 * </p>
 *
 * @author Jordan Kwan
 */
public enum NotificationType {

    /** Indicates that the user has joined an event’s waiting list. */
    WAITING,

    /** Indicates that the user has been invited to the event. */
    INVITED,

    /** Indicates that the user was not selected or invited to the event. */
    UNINVITED
}
