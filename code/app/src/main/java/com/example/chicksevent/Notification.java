package com.example.chicksevent;

import java.util.HashMap;
import java.util.Map;

public class Notification {
    private String userId;
    private String eventId;
    private NotificationType notificationType;
    private String message;
    private FirebaseService notificationService;

    Notification(String userId, String eventId, NotificationType notificationType, String message) {
        notificationService = new FirebaseService("Notification");
        this.userId = userId;
        this.eventId = eventId;
        this.notificationType = notificationType;
        this.message = message;
    }

    public void createNotification() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("message", message);
        notificationService.updateSubCollectionEntry(userId, eventId, notificationType.toString(), data);
    }

}
