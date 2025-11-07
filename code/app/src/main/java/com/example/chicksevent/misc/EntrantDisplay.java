package com.example.chicksevent.misc;

import com.example.chicksevent.adapter.UserAdapter;
import com.example.chicksevent.enums.EntrantStatus;

/**
 * A simple data holder class representing a displayable entrant with their ID and current status.
 * <p>
 * This class is used to decouple UI display logic from the full {@link Entrant} model.
 * It contains only the essential information needed to show an entrant in a list
 * (e.g., in waiting list or chosen list views).
 * </p>
 *
 * <p>
 * The {@code status} field typically holds values like "WAITING", "INVITED", or "UNINVITED"
 * as strings (mirroring {@link EntrantStatus} but kept as String for simplicity in adapters).
 * </p>
 *
 * @see Entrant
 * @see EntrantStatus
 * @see UserAdapter
 */
public class EntrantDisplay {

    /**
     * The unique identifier of the entrant (typically the Firebase user ID or device ID).
     */
    private String entrantId;

    /**
     * The current status of the entrant in the event lifecycle.
     * <p>
     * Example values: "WAITING", "INVITED", "UNINVITED".
     * </p>
     */
    private String status;

    /**
     * Constructs a new {@code EntrantDisplay} with the given entrant ID and status.
     *
     * @param entrantId the unique ID of the entrant
     * @param status    the current status of the entrant (e.g., "WAITING")
     */
    public EntrantDisplay(String entrantId, String status) {
        this.entrantId = entrantId;
        this.status = status;
    }

    /**
     * Returns the entrant ID.
     *
     * @return the unique identifier of the entrant
     */
    public String getEntrantId() {
        return entrantId;
    }

    /**
     * Returns the current status of the entrant.
     *
     * @return the status as a string (e.g., "WAITING", "INVITED")
     */
    public String getStatus() {
        return status;
    }
}