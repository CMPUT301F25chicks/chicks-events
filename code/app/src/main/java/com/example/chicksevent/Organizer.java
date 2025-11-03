package com.example.chicksevent;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

public class Organizer extends User {
    private FirebaseService organizerService;
    private String organizerId;

    Organizer() {
        organizerService = new FirebaseService("Organizer");
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void sendSelectedNotification() {
        FirebaseService selectedEntrantService = new FirebaseService("WaitingList");
        FirebaseService entrantService = new FirebaseService("Entrant");

        selectedEntrantService.getReference().get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("Firebase", "Error getting data", task.getException());
                    return;
                }
                DataSnapshot snapshot = task.getResult();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    String value = childSnapshot.getValue(String.class);

                    Log.i("friedchickenblob", key + " | " + value);
                    // Process the child
                }
            }
        });
    }

    // us 02.07.01
    public void sendWaitingListNotification() {

    }

    public void cancelDidNotSignUp() {

    }

    public void createEvent() {
    }

    public void listEntrants() {

    }

    public void rerollAttendees() {

    }

    public void listCancelledEntrants() {

    }

    public void isAdmin() {
    }

    public void isOrganizer() {

    }
}
