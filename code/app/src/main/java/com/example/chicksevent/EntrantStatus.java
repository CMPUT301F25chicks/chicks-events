package com.example.chicksevent;

public enum EntrantStatus {
    WAITING,    // Joined waiting list
    INVITED,    // Selected in the lottery
    ACCEPTED,   // Accepted invitation
    DECLINED,   // Declined invitation
    CANCELLED,  // Organizer removed the entrant
    UNINVITED,  // Not selected in the lottery
    CONFIRMED   // Finalized list of attendees
}