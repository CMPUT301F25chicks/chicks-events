package com.example.chicksevent;

public class Admin extends User {
    private FirebaseService adminService;

    Admin() {
        super();
        adminService = new FirebaseService("Admin");
    }
    public void deleteEvent() {

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
