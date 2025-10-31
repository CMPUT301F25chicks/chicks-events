package com.example.chicksevent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Organizer extends User {
    private final EntryStore eventStore;

    // Production path (real Firebase)
    public Organizer() {
        this(new FirebaseService("Event"));
    }

    // Test path — MUST call super(store) to avoid Firebase in JVM tests
    public Organizer(EntryStore store) {
        super(store);               // <-- THIS prevents calling User() and Firebase
        this.eventStore = store;
    }

    public String createEvent(String organizerId, String name, String details,
                              int entrantLimit, Date regStart, Date regEnd, String posterUrl) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("organizer", organizerId);
        data.put("eventDetails", details);
        data.put("entrantLimit", entrantLimit);
        data.put("registrationStartDate", fmt.format(regStart));
        data.put("registrationEndDate", fmt.format(regEnd));
        data.put("poster", posterUrl == null ? "null" : posterUrl);
        data.put("waitingList", "WaitingList");
        data.put("lotteryWaitingList", "list of entrants in waiting list that got accepted by lottery");
        data.put("finalEntrants", "final list les go");
        data.put("cancelledEntrants", "List of Cancelled Entrnats (accepted by lottery but cancelled)");
        return eventStore.addEntry(data);
    }
}
