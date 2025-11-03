package com.example.chicksevent;

public class Organizer extends User {
    private FirebaseService organizerService;

    Organizer() {
        organizerService = new FirebaseService("Organizer");
    }

    public void sendSelectedNotification() {

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

    public Boolean isOrganizer() {
        return true;
    }

    public Boolean isAdmin() {
        return false;
    }

}
