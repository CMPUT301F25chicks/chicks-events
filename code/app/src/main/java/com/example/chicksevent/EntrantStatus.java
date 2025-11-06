package com.example.chicksevent;

/**
 * Enumeration representing the participation status of an entrant in an event.
 * <p>
 * Each constant reflects a specific phase of the entrant's journey — from joining the waiting
 * list, to being invited, accepting or declining, and finally confirmation or removal.
 * </p>
 *
 * <p><b>Usage:</b>
 * The {@link EntrantStatus} values are used to structure Firebase nodes under:
 * <pre>
 * WaitingList/{eventId}/{EntrantStatus}/{entrantId}
 * </pre>
 * This allows efficient querying and management of entrants grouped by their current status.
 * </p>
 *
 * @author Jordan Kwan
 */
public enum EntrantStatus {

    /** Entrant has joined the waiting list but not yet selected. */
    WAITING,

    /** Entrant was randomly selected or chosen for invitation. */
    INVITED,

    /** Entrant accepted the invitation to participate. */
    ACCEPTED,

    /** Entrant declined the invitation to participate. */
    DECLINED,

    /** Organizer cancelled the entrant's participation. */
    CANCELLED,

    /** Entrant was not selected in the lottery. */
    UNINVITED,

    /** Entrant's participation is finalized and confirmed. */
    CONFIRMED
}