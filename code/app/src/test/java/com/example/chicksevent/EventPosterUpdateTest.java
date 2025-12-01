package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Unit tests for US 02.04.02: As an organizer I want to update an event poster 
 * to provide visual information to entrants.
 * These tests validate that:
 * <ul>
 *   <li>Events can be updated with a new poster</li>
 *   <li>Poster field is updated correctly in Firebase</li>
 *   <li>Existing poster can be replaced with a new one</li>
 *   <li>Poster can be removed (set to null/empty)</li>
 *   <li>Poster updates persist after editEvent()</li>
 * </ul>
 *
 * @author Jinn Kasai
 */
public class EventPosterUpdateTest {

    private static final String ORGANIZER_ID = "org-123";
    private static final String EVENT_ID = "event-456";
    private static final String ORIGINAL_POSTER = "https://example.com/original-poster.jpg";
    private static final String NEW_POSTER = "https://example.com/new-poster.jpg";
    private static final String UPDATED_POSTER = "https://example.com/updated-poster.jpg";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockEventRef;

    @Before
    public void setUp() {
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);
        mockEventRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // ==================== Event Poster Update Tests ====================

    /**
     * Test Case 1: Organizer can update event with new poster URL.
     * 
     * As an organizer, I should be able to update an existing event with
     * a new poster URL that will replace the old one.
     */
    @Test
    public void organizer_canUpdateEvent_withNewPosterUrl() throws Exception {
        // Create event with original poster
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Update poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        // Update event
        String updatedId = event.editEvent(EVENT_ID);

        // Verify event was updated
        assertEquals("Event ID should match", EVENT_ID, updatedId);
        assertEquals("Poster should be updated", NEW_POSTER, event.getPoster());
    }

    /**
     * Test Case 2: Event poster update is stored in Firebase.
     * 
     * As an organizer, when I update an event with a new poster, the new
     * poster information should be stored in Firebase.
     */
    @Test
    public void organizer_eventPosterUpdate_storedInFirebase() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Update poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify new poster was included in Firebase data
        verify(mockService, times(1)).editEntry(
                eq(EVENT_ID),
                argThat(map -> {
                    HashMap<String, Object> data = (HashMap<String, Object>) map;
                    return data.containsKey("poster") &&
                           data.get("poster").equals(NEW_POSTER);
                })
        );
    }

    /**
     * Test Case 3: Organizer can replace existing poster with new one.
     * 
     * As an organizer, I should be able to replace an existing poster
     * with a completely new poster image.
     */
    @Test
    public void organizer_canReplaceExistingPoster_withNewPoster() throws Exception {
        // Create event with original poster
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Verify original poster
        assertEquals("Original poster should be set", ORIGINAL_POSTER, event.getPoster());

        // Replace with new poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify poster was replaced
        assertEquals("Poster should be replaced", NEW_POSTER, event.getPoster());
        assertNotEquals("Poster should be different from original", 
                       ORIGINAL_POSTER, event.getPoster());
    }

    /**
     * Test Case 4: Organizer can remove poster by setting to null.
     * 
     * As an organizer, I should be able to remove the poster from an event
     * by setting it to null.
     */
    @Test
    public void organizer_canRemovePoster_bySettingToNull() throws Exception {
        // Create event with poster
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Remove poster
        event.setPoster(null);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify poster was removed
        assertNull("Poster should be null", event.getPoster());
    }

    /**
     * Test Case 5: Organizer can remove poster by setting to empty string.
     * 
     * As an organizer, I should be able to remove the poster from an event
     * by setting it to an empty string.
     */
    @Test
    public void organizer_canRemovePoster_bySettingToEmpty() throws Exception {
        // Create event with poster
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Remove poster
        event.setPoster("");

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify poster was removed
        assertEquals("Poster should be empty", "", event.getPoster());
    }

    /**
     * Test Case 6: Event poster update persists after editEvent().
     * 
     * As an organizer, after updating an event's poster, the new poster
     * should persist and be accessible.
     */
    @Test
    public void organizer_eventPosterUpdate_persistsAfterEdit() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Update poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        // Update event
        event.editEvent(EVENT_ID);

        // Verify poster persists
        assertEquals("Poster should persist after edit", 
                   NEW_POSTER, event.getPoster());
    }

    /**
     * Test Case 7: Organizer can update poster multiple times.
     * 
     * As an organizer, I should be able to update the poster multiple times,
     * with each update replacing the previous one.
     */
    @Test
    public void organizer_canUpdatePoster_multipleTimes() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        // First update
        event.setPoster(NEW_POSTER);
        event.editEvent(EVENT_ID);
        assertEquals("First update should work", NEW_POSTER, event.getPoster());

        // Second update
        event.setPoster(UPDATED_POSTER);
        event.editEvent(EVENT_ID);
        assertEquals("Second update should work", UPDATED_POSTER, event.getPoster());
        assertNotEquals("Poster should be different from first update", 
                       NEW_POSTER, event.getPoster());
    }

    /**
     * Test Case 8: Event poster update does not affect other fields.
     * 
     * As an organizer, updating the poster should not affect other event
     * fields like name, description, or dates.
     */
    @Test
    public void organizer_posterUpdate_doesNotAffectOtherFields() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Store original values
        String originalName = event.getName();
        String originalDescription = event.getEventDetails();
        String originalStartDate = event.getEventStartDate();

        // Update poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify other fields are unchanged
        assertEquals("Name should be unchanged", originalName, event.getName());
        assertEquals("Description should be unchanged", 
                   originalDescription, event.getEventDetails());
        assertEquals("Start date should be unchanged", 
                   originalStartDate, event.getEventStartDate());
    }

    /**
     * Test Case 9: Event poster update includes poster in Firebase data.
     * 
     * As an organizer, when I update an event's poster, the poster field
     * should be included in the Firebase update data.
     */
    @Test
    public void organizer_posterUpdate_includedInFirebaseData() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify poster field is in the update data
        verify(mockService, times(1)).editEntry(
                eq(EVENT_ID),
                argThat(map -> {
                    HashMap<String, Object> data = (HashMap<String, Object>) map;
                    return data.containsKey("poster");
                })
        );
    }

    /**
     * Test Case 10: Event poster can be updated from null to a URL.
     * 
     * As an organizer, I should be able to add a poster to an event that
     * previously had no poster (null).
     */
    @Test
    public void organizer_canAddPoster_toEventWithoutPoster() throws Exception {
        // Create event without poster
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, null, "fun", false
        );

        // Verify no poster initially
        assertEquals("Event should have no poster initially", "", event.getPoster());

        // Add poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify poster was added
        assertEquals("Poster should be added", NEW_POSTER, event.getPoster());
    }

    /**
     * Test Case 11: Event poster can be updated from empty to a URL.
     * 
     * As an organizer, I should be able to add a poster to an event that
     * previously had an empty poster string.
     */
    @Test
    public void organizer_canAddPoster_toEventWithEmptyPoster() throws Exception {
        // Create event with empty poster
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "", "fun", false
        );

        // Add poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify poster was added
        assertEquals("Poster should be added", NEW_POSTER, event.getPoster());
    }

    /**
     * Test Case 12: Event poster update can use base64 encoded string.
     * 
     * As an organizer, I should be able to update the poster with a base64
     * encoded string (which is how it's stored in Firebase Image service).
     */
    @Test
    public void organizer_canUpdatePoster_withBase64String() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        String base64Poster = "data:image/jpeg;base64,/9j/4AAQSkZJRg==";
        event.setPoster(base64Poster);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        assertEquals("Poster should accept base64 string", 
                    base64Poster, event.getPoster());
    }

    /**
     * Test Case 13: Event poster update can use data URI format.
     * 
     * As an organizer, I should be able to update the poster with a data URI
     * format (data:image/jpeg;base64,...).
     */
    @Test
    public void organizer_canUpdatePoster_withDataUri() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        String dataUri = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD";
        event.setPoster(dataUri);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        assertEquals("Poster should accept data URI", dataUri, event.getPoster());
    }

    /**
     * Test Case 14: Event poster update can use long URL string.
     * 
     * As an organizer, I should be able to update the poster with a long URL
     * string without causing issues.
     */
    @Test
    public void organizer_canUpdatePoster_withLongUrl() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        String longUrl = "https://example.com/very/long/path/to/poster/image/file/with/many/segments/poster.jpg";
        event.setPoster(longUrl);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        assertEquals("Poster should accept long URL", longUrl, event.getPoster());
    }

    /**
     * Test Case 15: Event poster update is independent of event ID.
     * 
     * As an organizer, updating the poster should work regardless of the
     * event ID, as long as the event exists.
     */
    @Test
    public void organizer_posterUpdate_independentOfEventId() throws Exception {
        String eventId1 = "event-001";
        String eventId2 = "event-002";

        Event event1 = new Event(
                ORGANIZER_ID, eventId1, "Event 1", "Description 1",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        Event event2 = new Event(
                ORGANIZER_ID, eventId2, "Event 2", "Description 2",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Update both events with different posters
        event1.setPoster(NEW_POSTER);
        event2.setPoster(UPDATED_POSTER);

        FirebaseService mockService1 = mock(FirebaseService.class);
        FirebaseService mockService2 = mock(FirebaseService.class);
        when(mockService1.getReference()).thenReturn(mockEventRef);
        when(mockService2.getReference()).thenReturn(mockEventRef);
        when(mockService1.editEntry(eq(eventId1), any(HashMap.class))).thenReturn(eventId1);
        when(mockService2.editEntry(eq(eventId2), any(HashMap.class))).thenReturn(eventId2);
        setPrivate(event1, "eventService", mockService1);
        setPrivate(event2, "eventService", mockService2);

        event1.editEvent(eventId1);
        event2.editEvent(eventId2);

        // Verify both updates worked independently
        assertEquals("Event 1 poster should be updated", NEW_POSTER, event1.getPoster());
        assertEquals("Event 2 poster should be updated", UPDATED_POSTER, event2.getPoster());
    }

    /**
     * Test Case 16: Event poster update can be retrieved after editEvent().
     * 
     * As an organizer, after updating an event's poster, I should be able
     * to retrieve the updated poster value from the Event object.
     */
    @Test
    public void organizer_posterUpdate_retrievableAfterEdit() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Update poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        // Update event
        event.editEvent(EVENT_ID);

        // Verify updated poster is retrievable
        assertNotNull("Poster should be accessible", event.getPoster());
        assertEquals("Poster should match updated value", 
                    NEW_POSTER, event.getPoster());
    }

    /**
     * Test Case 17: Event poster update is serializable to Firebase.
     * 
     * As an organizer, the updated poster field should be properly serialized
     * when updating to Firebase.
     */
    @Test
    public void organizer_posterUpdate_serializableToFirebase() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify poster is included in the serialized data
        verify(mockService, times(1)).editEntry(
                eq(EVENT_ID),
                argThat(map -> {
                    HashMap<String, Object> data = (HashMap<String, Object>) map;
                    Object posterValue = data.get("poster");
                    return posterValue != null && 
                           posterValue.toString().equals(NEW_POSTER);
                })
        );
    }

    /**
     * Test Case 18: Event poster update replaces old value in Firebase.
     * 
     * As an organizer, when I update the poster, the new value should
     * replace the old value in Firebase, not be added alongside it.
     */
    @Test
    public void organizer_posterUpdate_replacesOldValueInFirebase() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Verify original poster
        assertEquals("Original poster should be set", ORIGINAL_POSTER, event.getPoster());

        // Update poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify new poster is in Firebase data (not old one)
        verify(mockService, times(1)).editEntry(
                eq(EVENT_ID),
                argThat(map -> {
                    HashMap<String, Object> data = (HashMap<String, Object>) map;
                    Object posterValue = data.get("poster");
                    return posterValue != null && 
                           posterValue.toString().equals(NEW_POSTER) &&
                           !posterValue.toString().equals(ORIGINAL_POSTER);
                })
        );
    }

    /**
     * Test Case 19: Event poster update works with same poster URL.
     * 
     * As an organizer, I should be able to "update" the poster to the same
     * URL without causing errors (idempotent operation).
     */
    @Test
    public void organizer_posterUpdate_worksWithSamePosterUrl() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // "Update" to same poster
        event.setPoster(ORIGINAL_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify poster is still the same
        assertEquals("Poster should remain the same", 
                    ORIGINAL_POSTER, event.getPoster());
    }

    /**
     * Test Case 20: Event poster update maintains event integrity.
     * 
     * As an organizer, updating the poster should maintain the integrity
     * of the event object and all its other fields.
     */
    @Test
    public void organizer_posterUpdate_maintainsEventIntegrity() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, ORIGINAL_POSTER, "fun", false
        );

        // Store all original values
        String originalId = event.getId();
        String originalName = event.getName();
        String originalDescription = event.getEventDetails();
        int originalLimit = event.getEntrantLimit();
        boolean originalGeo = event.isGeolocationRequired();

        // Update poster
        event.setPoster(NEW_POSTER);

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq(EVENT_ID), any(HashMap.class))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.editEvent(EVENT_ID);

        // Verify all fields are maintained
        assertEquals("ID should be maintained", originalId, event.getId());
        assertEquals("Name should be maintained", originalName, event.getName());
        assertEquals("Description should be maintained", 
                   originalDescription, event.getEventDetails());
        assertEquals("Limit should be maintained", originalLimit, event.getEntrantLimit());
        assertEquals("Geo requirement should be maintained", 
                   originalGeo, event.isGeolocationRequired());
        assertEquals("Poster should be updated", NEW_POSTER, event.getPoster());
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to set private fields via reflection.
     */
    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

