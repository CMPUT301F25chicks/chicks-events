package com.example.chicksevent;

import java.util.ArrayList;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Represents a waiting list for a specific event.
 * <p>
 * Encapsulates the relationship between an event and its list of {@link Entrant}s
 * who have joined the waiting queue. Provides access to the corresponding Firebase
 * reference for managing waiting list data.
 * </p>
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Maintain the in-memory list of entrants for an event.</li>
 *     <li>Hold a reference to the event ID that the waiting list belongs to.</li>
 *     <li>Provide a {@link FirebaseService} instance to interact with the database.</li>
 * </ul>
 * </p>
 *
 * <p>This class serves as a data structure and can be extended with methods for adding,
 * removing, or promoting entrants within the waiting list.</p>
 *
 * @author Jordan Kwan
 */
public class WaitingList {

    /** Firebase service for the "WaitingList" root. */
    private FirebaseService waitingListService;

    /** Collection of entrants currently on the waiting list. */
    private ArrayList<Entrant> entrantList;

    /** Identifier linking this waiting list to a specific event. */
    private String eventId;
}