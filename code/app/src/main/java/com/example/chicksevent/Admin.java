package com.example.chicksevent;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private FirebaseService adminService;
    private FirebaseService userService;

    Admin() {
        super();
        adminService = new FirebaseService("Admin");
        userService = new FirebaseService("User");
    }
    public void deleteEvent() {

    }

    public Task<Void> deleteProfile(String uid) {

        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (uid == null || uid.isEmpty()) {
            tcs.setException(new IllegalArgumentException("Uid is empty"));
            return tcs.getTask();
        }

        // Delete user from Firebase
        userService.getReference().child(uid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    tcs.setResult(null);
                    Log.d("DeleteProfile", "User profile deleted successfully");
                })
                .addOnFailureListener(e -> {
                    tcs.setException(e);  // If deletion failed, return the exception
                    Log.e("DeleteProfile", "Failed to delete user profile", e);
                });

        return tcs.getTask();
    }






    // US 03.05.01
    public Task<List<User>> browseProfiles() {
        TaskCompletionSource<List<User>> tcs = new TaskCompletionSource<>();

        userService.getReference().get().addOnSuccessListener(snapshot -> {
            List<User> userList = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                User user = child.getValue(User.class);
                if (user != null) {
                    try {
                        user.setUid(child.getKey());
                    } catch (Exception ignored) {}
                    userList.add(user);
                }
            }
            tcs.setResult(userList);  // Return the list of users
        }).addOnFailureListener(tcs::setException);

        return tcs.getTask();

    }

    public void browseEvent() {

    }

    public Boolean isAdmin() {
        return true;
    }

    public Boolean isOrganizer() {
        return false;
    }
}