package com.example.chicksevent;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;


public class Admin extends User {
    private FirebaseService adminService;
    private FirebaseService organizerService;

    Admin() {
        super();
        adminService = new FirebaseService("Admin");
        this.organizerService = new FirebaseService("Organizer");
    }
    // testing constructor
    public Admin(FirebaseService organizerService) {
        super();
        this.organizerService = organizerService;
    }
    public void deleteEvent() {

    }

    public Task<Void> removeOrganizer(String organizerId) {
        try {
            TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

            organizerService.getReference()
                    .child(organizerId)
                    .removeValue()
                    .addOnSuccessListener(a -> {
                        android.util.Log.d("Admin", "Organizer removed: " + organizerId);
                        tcs.setResult(null);
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("Admin", "Failed to remove organizer", e);
                        tcs.setException(e);
                    });

            return tcs.getTask();
        } catch (Exception e) {
            return Tasks.forResult(null);
        }
    }


public void deleteProfile() {

}

public void browseProfile() {

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
