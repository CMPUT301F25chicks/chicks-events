package com.example.chicksevent;

import android.util.Log;

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

    private FirebaseService entrantsService;
    private FirebaseService organizersService;

    Admin(String userId) {
        super(userId);
        adminService = new FirebaseService("Admin");
        this.eventsService = new FirebaseService("Event");
        this.entrantsService = new FirebaseService("Entrant");
        this.organizersService = new FirebaseService("Organizer");
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

    public Task<Void> deleteEntrantProfile(String entrantId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (entrantId == null || entrantId.isEmpty()) {
            tcs.setException(new IllegalArgumentException("entrantId is empty"));
            return tcs.getTask();
        }

        DatabaseReference ref = entrantsService.getReference().child(entrantId);
        ref.removeValue((DatabaseError error, DatabaseReference ignored) -> {
            if (error == null) {
                Log.d("AdminDeleteEntrant", "Entrant deleted successfully");
                tcs.setResult(null);
            } else {
                Log.e("AdminDeleteEntrant", "Error deleting entrant", error.toException());
                tcs.setException(error.toException());
            }
        });
        return tcs.getTask();
    }
    public Task<Void> deleteOrganizerProfile(String organizerId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (organizerId == null || organizerId.isEmpty()) {
            tcs.setException(new IllegalArgumentException("organizerId is empty"));
            return tcs.getTask();
        }

        DatabaseReference ref = organizersService.getReference().child(organizerId);
        ref.removeValue((DatabaseError error, DatabaseReference ignored) -> {
            if (error == null) {
                Log.d("AdminDeleteOrganizer", "Organizer deleted successfully");
                tcs.setResult(null);
            } else {
                Log.e("AdminDeleteOrganizer", "Error deleting organizer", error.toException());
                tcs.setException(error.toException());
            }
        });
        return tcs.getTask();
    }

    public Task<List<User>> browseEntrants() {
        TaskCompletionSource<List<User>> tcs = new TaskCompletionSource<>();

        entrantsService.getReference().get().addOnSuccessListener(snapshot -> {
            List<User> entrants = new ArrayList<>();
            for (DataSnapshot child: snapshot.getChildren()) {
                Entrant e = child.getValue(Entrant.class);
                if (e != null) {
                    try { e.setEntrantId(child.getKey()); } catch (Exception ignored) {}
                    entrants.add(e);
                }
            }
            tcs.setResult(entrants);
        }).addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    public Task<List<Organizer>> browseOrganizers() {
        TaskCompletionSource<List<Organizer>> tcs = new TaskCompletionSource<>();

        organizersService.getReference().get().addOnSuccessListener(snapshot -> {
            List<Organizer> organizers = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                Organizer o = child.getValue(Organizer.class);
                if (o != null) {
                    try { o.setOrganizerId(child.getKey()); } catch (Exception ignored) {}
                    organizers.add(o);
                }
            }
            tcs.setResult(organizers);
        }).addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }


    // US 03.05.01
    public Task<List<Event>> browseEvents() {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();

        eventsService.getReference().get().addOnSuccessListener(snapshot ->{
            List<Event> list = new ArrayList<>();
            for (DataSnapshot child: snapshot.getChildren()) {
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