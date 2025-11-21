package com.example.chicksevent.misc;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;
//import com.google.zxing.BarcodeFormat;
//import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

/**
 * Domain model representing an event stored in Firebase Realtime Database.
 * <p>
 * This class mirrors the schema used under the <code>"Event"</code> root and provides a
 * convenience method {@link #createEvent()} to persist a newly constructed event. The Firebase
 * key (id) is generated server-side via <code>push()</code> and then written along with the
 * rest of the fields. The organizing user is represented by an {@link Organizer} derived from
 * {@code entrantId}.
 * </p>
 *
 * <h3>Firebase Structure</h3>
 * <pre>
 * Event/{eventId} : {
 *   id: string,
 *   name: string,
 *   eventDetails: string,
 *   eventStartDate: YYYY-MM-DD | null,
 *   eventEndDate: YYYY-MM-DD | null,
 *   registrationStartDate: YYYY-MM-DD | null,
 *   registrationEndDate: YYYY-MM-DD | null,
 *   entrantLimit: number,
 *   organizer: string, // organizerId
 *   poster: string | null, // URL
 *   tag: string | null     // space-separated tags
 * }
 * </pre>
 *
 * <p><b>Notes:</b> Firebase requires a no-arg constructor for automatic deserialization when
 * mapping to POJOs. If you intend to read {@code Event} objects back via
 * {@code DataSnapshot.getValue(Event.class)}, ensure a package-visible or public no-arg
 * constructor is available. The current class is primarily used for creation/writes.
 * </p>
 *
 * @author Jordan Kwan
 * @author Jinn Kasai
 */
@IgnoreExtraProperties
public class Event {
    /** Firebase service wrapper scoped to the "Event" root. */
    FirebaseService eventService = new FirebaseService("Event");

    /** Firebase key for this event. */
    private String id;

    /** Human-readable name of the event. */
    private String name;

    /** Long-form description/details for the event. */
    private String eventDetails;

    /** Inclusive date of the event in ISO format (YYYY-MM-DD). */
    private String eventDate;

    /** Inclusive start date of the event in ISO format (YYYY-MM-DD), nullable. */
    private String eventStartDate;          // "YYYY-MM-DD"

    /** Inclusive end date of the event in ISO format (YYYY-MM-DD), nullable. */
    private String eventEndDate;            // "YYYY-MM-DD"

    /** Registration open date in ISO format (YYYY-MM-DD), nullable. */
    private String registrationStartDate;   // "YYYY-MM-DD"

    /** Registration close date in ISO format (YYYY-MM-DD), nullable. */
    private String registrationEndDate;     // "YYYY-MM-DD"

    /** Maximum entrants allowed; 0 or negative may be treated as unlimited by callers. */
    private int entrantLimit;

    /** Organizer handle for this event; derived from {@link #entrantId}. */
    private Organizer organizer;

    /** Optional poster image URL. */
    private String poster;   // nullable URL (or null)

    /** Optional space-separated tags to aid filtering/search. */
    private String tag;      // space-separated tags

    /** Whether geolocation is required for entrants to join the waiting list. Defaults to false. */
    private boolean geolocationRequired;

    /** The user id of the account that created/owns this event (organizer). */
    private String entrantId;

    /**
     * Constructs a new {@code Event} instance using the provided metadata.
     * <p>
     * The {@code id} is typically generated on creation via {@link #createEvent()} and can be
     * supplied as {@code null} here. The {@code organizer} field is initialized from the
     * given {@code entrantId} and {@code id}.
     * </p>
     *
     * @param entrantId the user id of the event organizer (required).
     * @param id the Firebase key to use; may be {@code null} to generate a new key on create.
     * @param name the event's display name.
     * @param eventDetails a human-readable description of the event.
     * @param eventDate a human-readable description of the event.
     * @param eventStartDate start date of the register in YYYY-MM-DD (nullable).
     * @param eventEndDate end date of the register in YYYY-MM-DD (nullable).
     * @param registrationStartDate registration open date in YYYY-MM-DD (nullable).
     * @param registrationEndDate registration close date in YYYY-MM-DD (nullable).
     * @param entrantLimit maximum number of entrants allowed (0 or negative for no limit as per caller policy).
     * @param poster optional poster URL; may be {@code null}.
     * @param tag optional space-separated tags; may be {@code null}.
     * @param geolocationRequired whether geolocation is required for entrants to join.
     */
    public Event(String entrantId, String id, String name, String eventDetails, String eventDate,
                 String eventStartDate, String eventEndDate,
                 String registrationStartDate, String registrationEndDate,
                 int entrantLimit, String poster, String tag, boolean geolocationRequired) {
        this.entrantId = entrantId;
        this.id = id;
        this.name = name;
        this.eventDetails = eventDetails;
        this.eventDate = eventDate;
        this.eventStartDate = eventStartDate == null ? "" : eventStartDate;
        this.eventEndDate = eventEndDate == null ? "" : eventEndDate;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.entrantLimit = entrantLimit;

        this.poster = poster == null ? "" : tag;
        this.tag = tag == null ? "" : tag;
        this.geolocationRequired = geolocationRequired;

        this.organizer = new Organizer(entrantId, id);

    } // Required by Firebase
    public String createEvent(){
        Log.i("filtering", "creating event");
        HashMap<String, Object> map = new HashMap<>();
        id = eventService.getReference().push().getKey();


        map.put("id", id);
        map.put("name", getName());
        map.put("eventDetails", getEventDetails());
        map.put("eventDate", getEventDate());
        map.put("eventStartDate", getEventStartDate());
        map.put("eventEndDate", getEventEndDate());
        map.put("registrationStartDate", getRegistrationStartDate());
        map.put("registrationEndDate", getRegistrationEndDate());
        map.put("entrantLimit", getEntrantLimit());
        map.put("organizer", getOrganizer().getOrganizerId());
        map.put("poster", getPoster());              // null is fine; it will simply be omitted
        map.put("tag", getTag());
        map.put("geolocationRequired", isGeolocationRequired());
        id = eventService.addEntry(map, id);

        this.organizer = new Organizer(entrantId, id);

        return id;
    }

    // --- Getters and setters ---

    /** @return the Firebase id for this event. */
    public String getId() { return id; }

    /** @param id sets the Firebase id (use with care; typically managed by {@link #createEvent()}). */
    public void setId(String id) { this.id = id; }

    /** @return the display name of the event. */
    public String getName() { return name; }

    /** @param name sets the display name of the event. */
    public void setName(String name) { this.name = name; }

    /** @return long-form description of the event. */
    public String getEventDetails() { return eventDetails; }

    /** @param eventDetails sets the event description. */
    public void setEventDetails(String eventDetails) { this.eventDetails = eventDetails; }

    /** @return long-form description of the event. */
    public String getEventDate() { return eventDate; }

    /** @param eventDate sets the event description. */
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    /** @return the event start date in YYYY-MM-DD or {@code null}. */
    public String getEventStartDate() { return eventStartDate; }

    /** @param eventStartDate sets the event start date (YYYY-MM-DD). */
    public void setEventStartDate(String eventStartDate) { this.eventStartDate = eventStartDate; }

    /** @return the event end date in YYYY-MM-DD or {@code null}. */
    public String getEventEndDate() { return eventEndDate; }

    /** @param eventEndDate sets the event end date (YYYY-MM-DD). */
    public void setEventEndDate(String eventEndDate) { this.eventEndDate = eventEndDate; }

    /** @return the registration open date in YYYY-MM-DD or {@code null}. */
    public String getRegistrationStartDate() { return registrationStartDate; }

    /** @param registrationStartDate sets the registration open date (YYYY-MM-DD). */
    public void setRegistrationStartDate(String registrationStartDate) { this.registrationStartDate = registrationStartDate; }

    /** @return the registration close date in YYYY-MM-DD or {@code null}. */
    public String getRegistrationEndDate() { return registrationEndDate; }

    /** @param registrationEndDate sets the registration close date (YYYY-MM-DD). */
    public void setRegistrationEndDate(String registrationEndDate) { this.registrationEndDate = registrationEndDate; }

    /** @return the maximum entrants allowed for this event. */
    public int getEntrantLimit() { return entrantLimit; }

    /** @param entrantLimit sets the maximum entrants allowed. */
    public void setEntrantLimit(int entrantLimit) { this.entrantLimit = entrantLimit; }

    /** @return the organizer model associated with this event. */
    public Organizer getOrganizer() { return organizer; }

    /** @param organizer sets the organizer model. */
    public void setOrganizer(Organizer organizer) { this.organizer = organizer; }

    /** @return the optional poster URL or {@code null}. */
    public String getPoster() { return poster; }

    /** @param poster sets the optional poster URL. */
    public void setPoster(String poster) { this.poster = poster; }

    /** @return the space-separated tag string or {@code null}. */
    public String getTag() { return tag; }

    /** @param tag sets the space-separated tag string. */
    public void setTag(String tag) { this.tag = tag; }

    /** @return whether geolocation is required for entrants to join. */
    public boolean isGeolocationRequired() { return geolocationRequired; }

    /** @param geolocationRequired sets whether geolocation is required. */
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    // Generate QR for chicks://event/{eventId}
    //try {
    // String deepLink = "chicks://event/" + eventId;
//        BarcodeEncoder enc = new BarcodeEncoder();
//        Bitmap bmp = enc.encodeBitmap(deepLink, BarcodeFormat.QR_CODE, 900, 900);
//        qrImg.setImageBitmap(bmp);
//        Toast.makeText(requireContext(), "Event published. QR generated.", Toast.LENGTH_SHORT).show();
//    } catch (Exception ex) {
//        Toast.makeText(requireContext(), "QR error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
//    } finally {
//        publishBtn.setEnabled(true);
}
