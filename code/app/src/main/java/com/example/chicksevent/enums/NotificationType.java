package com.example.chicksevent.enums;

import com.example.chicksevent.misc.Notification;
import com.example.chicksevent.misc.User;

/**
 * Enumeration representing the different types of notifications that can be sent
 * to users in the ChicksEvent application.
 * <p>
 * Each notification type corresponds to a specific event lifecycle action:
 * <ul>
 *   <li>{@link #WAITING} — User is on the waiting list for an event</li>
 *   <li>{@link #INVITED} — User has been selected and invited to the event</li>
 *   <li>{@link #UNINVITED} — User was previously invited but has been removed</li>
 * </ul>
 * </p>
 *
 * <p>
 * These values are used to categorize notifications stored in Firebase under
 * a user's notification subcollection and to determine appropriate UI messaging.
 * </p>
 *
 * @see Notification
 * @see User#getNotificationList()
 */
public enum NotificationType {

    /**
     * Indicates the user is currently on the waiting list for an event.
     * <p>
     * Sent when a user signs up for a full event and is placed in the queue.
     * </p>
     */
    WAITING,

    /**
     * Indicates the user has been selected from the waiting list and invited to attend.
     * <p>
     * Sent when a spot opens up and the user is promoted to confirmed attendee.
     * </p>
     */
    INVITED,

    /**
     * Indicates the user was previously invited but has been removed from the event.
     * <p>
     * Sent when an organizer manually removes an attendee or due to policy violation.
     * </p>
     */
    UNINVITED,
}