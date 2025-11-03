package com.example.chicksevent;
import java.util.HashMap;
public class Participation {
    private String eventId;       // The event this participation refers to
    private String entrantId;     // The entrant participating
    private EntrantStatus status; // Current state of this participation
    public Participation(String eventId, String entrantId) {
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.status = EntrantStatus.WAITING; // default when joining waiting list
    }
    // Getter methods
    public String getEventId() { return eventId; }
    public String getEntrantId() { return entrantId; }
    public EntrantStatus getStatus() { return status; }

    public void joinWaitingList(FirebaseService service) {

        // When entrant joins they'll be marked WAITING
        status = EntrantStatus.WAITING;

        HashMap<String, Object> data = new HashMap<>();
        data.put("status", "WAITING");
        data.put("eventId", eventId);
        data.put("entrantId", entrantId);

        service.updateSubCollectionEntry(entrantId, "participation", eventId, data);
    }
}