package com.example.chicksevent;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private FirebaseService adminService;
    private FirebaseService eventsService;

    Admin() {
        super();
        adminService = new FirebaseService("Admin");
        this.eventsService = new FirebaseService("Event");
    }

    // US 03.01.01
    public Task<Void> deleteEvent(String eventId) {


        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (eventId == null || eventId.isEmpty()) {
            tcs.setException(new IllegalArgumentException("eventId is empty"));
            return tcs.getTask();
            }

        DatabaseReference ref = eventsService.getReference().child(eventId);
        ref.removeValue((DatabaseError error, DatabaseReference ignored) -> {
            if (error == null) {
                tcs.setResult(null);
            } else {
                tcs.setException(error.toException());
            }
            });
        return tcs.getTask();
    }

    public void deleteProfile() {

    }

    public void browseProfile() {

    }

    // US 03.05.01
    public Task<List<Event>> browseEvents() {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();

        eventsService.getReference().get().addOnSuccessListener(snapshot ->{
            List<Event> list = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                Event e = child.getValue(Event.class);
                if (e != null) {
                    try { e.setId(child.getKey()); } catch (Exception ignored) {}
                    list.add(e);
                }
            }
            tcs.setResult(list);
        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }


    public Boolean isAdmin() {
        return true;
    }

    public Boolean isOrganizer() {
        return false;
    }
}

/*
Admin admin = new Admin();
admin.browseEvents()
     .addOnSuccessListener(events -> {
        for (Event e : events) {
        Log.d("BrowseEvents", e.getName() + " (" + e.getEventStartDate() + ")");
        }
        })
        .addOnFailureListener(err -> Log.e("BrowseEvents", "Failed to fetch events", err));
*/