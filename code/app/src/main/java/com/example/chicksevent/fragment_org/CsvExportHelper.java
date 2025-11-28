package com.example.chicksevent.fragment_org;

// This class has NO Android dependencies. It is a pure, simple Java class.
public class CsvExportHelper {    private static final String BASE_URL = "https://us-central1-listycity-friedchicken.cloudfunctions.net/exportFinalEntrants";

    /**
     * Builds the full CSV export URL from an event ID.
     * This logic is now isolated and easily testable.
     *
     * @param eventId The ID of the event to export.
     * @return The complete URL for the cloud function, or null if the eventId is invalid.
     * @author Juan Rea
     */
    public static String buildUrl(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return null;
        }
        return BASE_URL + "?eventId=" + eventId;
    }
}
