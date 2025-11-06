package com.example.chicksevent;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class User {

    private ArrayList<Event> eventList;
    private FirebaseService userService;
    private FirebaseService eventService;
    private FirebaseService notificationService;
    private String userId;
    private String name;
    private String phoneNumber;
    private String email;
    String TAG = "RTD8";

    // handles setting the user ID and initializing the FirebaseService
    User(String userId) {
        this.userId = userId;
        // The service now correctly points to "User" based on your Firebase structure image
        userService = new FirebaseService("User");
        eventService = new FirebaseService("Event");
        notificationService = new FirebaseService("Notification");
    }

    /**
     * Updates the user's profile information in Firebase.
     * @param name The user's full name.
     * @param email The user's email address.
     * @param phone The user's optional phone number. Can be null or empty.
     */
    public void updateProfile(String name, String email, String phone) {
        // Basic validation
        if (userId == null || userId.isEmpty()) {
            System.err.println("Error: User ID is not set. Cannot update profile.");
            return;
        }

        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            System.err.println("Error: Name and Email cannot be empty.");
            return;
        }

        // Update the local object's properties
        this.name = name.trim();
        this.email = email.trim();
        this.phoneNumber = (phone != null) ? phone.trim() : "";

        // Create a map to send only the updated fields to Firebase
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", this.name);
        updates.put("email", this.email);
        updates.put("phoneNumber", this.phoneNumber);
        updates.put("uid", this.userId); // Store UID in the record itself

        // Call the editEntry method from existing FirebaseService
        userService.editEntry(userId, updates);
    }

    // In User.java

    /**
     * Deletes the user's entire profile from the Firebase database.
     * This is an irreversible action.
     */
    public void deleteProfile() {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot delete profile: User ID is not set.");
            return;
        }
        userService.deleteEntry(userId);
        Log.i(TAG, "Deletion requested for user: " + userId);
    }

    public Task<Boolean> filterEvents(ArrayList<String> filterList) {
        Log.i(TAG, "what");
        Log.i(TAG, "e" + eventService);
        return eventService.getReference().get().continueWith(task -> {
            Log.d(TAG, "=== All Children at Root filter ===");

            for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                String key = childSnapshot.getKey();
                if (childSnapshot.hasChild("tag") && childSnapshot.child("tag").getValue() != null) {
                    String[] value = childSnapshot.child("tag").getValue(String.class).split(" ");

                    Log.d(TAG, "Key: " + key);
                    for (String val : value) {
                        if (filterList.contains(val)) {
                            return true;
                        }
                    }
                    Log.d(TAG, "Value: " + Arrays.toString(value));
                }
                Log.d(TAG, "---");
            }
            return false;
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error reading data: " + databaseError.getMessage());
            }
        });
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


    public Boolean isAdmin() {
        return false;
    }

    public Boolean isOrganizer() {
        return false;
    }

    // --- GETTERS AND SETTERS ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
