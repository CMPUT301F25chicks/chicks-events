package com.example.chicksevent;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private ArrayList<Event> eventList;
    private FirebaseService userService;
    private FirebaseService eventService;
    private FirebaseService notificationService;
    private FirebaseService adminService;
    private String userId;
    private String phoneNumber;
    private String email;
    private String name;
    private boolean notificationsEnabled;

    String TAG = "RTD8";


    User(String userId) {
        this.userId = userId;
        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");
        notificationService = new FirebaseService("Notification");
        adminService = new FirebaseService("Admin");
        this.notificationsEnabled = true;
    }

    public Task<ArrayList<String>> filterEvents(ArrayList<String> filterList) {
        Log.i(TAG, "what");
        Log.i(TAG, "e" + eventService);
        return eventService.getReference().get().continueWith(task -> {
            Log.d(TAG, "=== filtering events ===");
            ArrayList<String> eventList = new ArrayList<>();

            // Iterate through all children
            for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                String key = childSnapshot.getKey();
                String[] value = ((Map<String, String>) childSnapshot.getValue()).get("tag").split(" ");


                Log.d(TAG, "Key: " + key);
                for (String val : value) {
                    Log.d(TAG, "Value: " + val);
                    if (filterList.contains(val)) {
                        eventList.add(key);
//                        return eventList;
                    }
                }

                Log.d(TAG, "---");
            }

//                Log.d(TAG, "Total children: " + dataSnapshot.getChildrenCount());
            return eventList;
        });
    }

    public String getUserId() {
        return userId;
    }

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

    public boolean areNotificationsEnabled() {
        return notificationsEnabled;
    }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    // update value in firebase
    public void updateNotificationPreference(boolean isEnabled) {
        this.setNotificationsEnabled(isEnabled);

        java.util.HashMap<String, Object> update = new java.util.HashMap<>();
        update.put("notificationsEnabled", isEnabled);
        userService.editEntry(userId, update);
    }

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

        // Call the editEntry method from existing FirebaseService
        userService.editEntry(userId, updates);
        return true;
    }



//    public void listEvents() {
//        Log.i("RTD8", "hi");
////        Log.i("RTD8", String.format(eventService.getReference().get().getResult()));
//        eventService.getReference().get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//            @Override
//            public void onSuccess(DataSnapshot snapshot) {
//                Log.i("RTD8", "in here");
//                Log.i("RTD8", String.format("%d", snapshot.getChildrenCount()));
//
//                Log.i("RTD8", String.valueOf(snapshot.getChildren()));
////                List<String> childrenKeys = new ArrayList<>();
////                List<Object> childrenValues = new ArrayList<>();
////                if (snapshot.getValue() != null) {
////                Log.i("RTDB", "Raw value: " + snapshot.getValue());
//////                }
//////                if (snapshot.getValue() != null) {
////                Log.i("RTDB", "poop");
////                }
////                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//////                    String key = childSnapshot.getKey();                // e.g., "-Nabc123"
//////                    Object value = childSnapshot.getValue();            // Full child data
////                    // Or typed: String msg = childSnapshot.child("message").getValue(String.class);
////
////
////                    Log.i("RTDB", "Key: ");
////                }
//
//                Log.i("RTD8", "bruh");
//            }
//        }).addOnFailureListener(e -> {
//            Log.i("RTDB", "Error: " + e.getMessage());
//        });
//
//
//
//    }

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

    public Boolean isOrganizer() {
        return false;
    }
}
