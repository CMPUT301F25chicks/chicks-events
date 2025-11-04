package com.example.chicksevent;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.example.chicksevent.FirebaseService;
import com.google.firebase.database.IgnoreExtraProperties;
//import com.google.zxing.BarcodeFormat;
//import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

/**
 * Event model for Realtime Databas
 * Matches the schema you showed in your screenshot.
 */
@IgnoreExtraProperties
public class Event {
    FirebaseService eventService = new FirebaseService("Event");

    private String id;

    private String name;
    private String eventDetails;

    private String eventStartDate;          // "YYYY-MM-DD"
    private String eventEndDate;            // "YYYY-MM-DD"
    private String registrationStartDate;   // "YYYY-MM-DD"
    private String registrationEndDate;     // "YYYY-MM-DD"

    private int entrantLimit;
    private String organizer;
    private String poster;   // nullable URL (or null)
    private String tag;      // space-separated tags

    public Event(String id, String name, String eventDetails,
                 String eventStartDate, String eventEndDate,
                 String registrationStartDate, String registrationEndDate,
                 int entrantLimit, String organizer, String poster, String tag) {
        this.id = id;
        this.name = name;
        this.eventDetails = eventDetails;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.entrantLimit = entrantLimit;
        this.organizer = organizer;
        this.poster = poster;
        this.tag = tag;
    } // Required by Firebase
    public void createEvent(){

        HashMap<String, Object> map = new HashMap<>();
        id = eventService.getReference().push().getKey();


        map.put("id", id);
        map.put("name", getName());
        map.put("eventDetails", getEventDetails());
        map.put("eventStartDate", getEventStartDate());
        map.put("eventEndDate", getEventEndDate());
        map.put("registrationStartDate", getRegistrationStartDate());
        map.put("registrationEndDate", getRegistrationEndDate());
        map.put("entrantLimit", getEntrantLimit());
        map.put("organizer", getOrganizer());
        map.put("poster", getPoster());              // null is fine; it will simply be omitted
        map.put("tag", getTag());
        id = eventService.addEntry(map, id);
    }

    // --- Getters and setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEventDetails() { return eventDetails; }
    public void setEventDetails(String eventDetails) { this.eventDetails = eventDetails; }

    public String getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(String eventStartDate) { this.eventStartDate = eventStartDate; }

    public String getEventEndDate() { return eventEndDate; }
    public void setEventEndDate(String eventEndDate) { this.eventEndDate = eventEndDate; }

    public String getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(String registrationStartDate) { this.registrationStartDate = registrationStartDate; }

    public String getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(String registrationEndDate) { this.registrationEndDate = registrationEndDate; }

    public int getEntrantLimit() { return entrantLimit; }
    public void setEntrantLimit(int entrantLimit) { this.entrantLimit = entrantLimit; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

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
