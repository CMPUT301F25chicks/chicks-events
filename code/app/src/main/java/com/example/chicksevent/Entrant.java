package com.example.chicksevent;

public class Entrant extends User {

    private FirebaseService entrantService;

    Entrant() {
        super();
        entrantService = new FirebaseService("Entrant");
    }

    public void joinWaitingList() {

    }

    public void leaveWaitingList() {

    }

    public void acceptInvitation() {

    }

    public void declineInvitation() {

    }

    public void isAdmin() {
    }

    public void isOrganizer() {

    }
}
