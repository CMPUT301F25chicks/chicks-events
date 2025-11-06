package com.example.chicksevent;

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

    public void deleteProfile() {

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
