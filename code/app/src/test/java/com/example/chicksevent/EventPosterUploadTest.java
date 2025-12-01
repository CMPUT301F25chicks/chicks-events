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
 * Unit tests for US 02.04.01: As an organizer I want to upload an event poster 
 * to the event details page to provide visual information to entrants.
 * These tests validate that:
 * <ul>
 *   <li>Events can be created with poster information</li>
 *   <li>Poster field is stored correctly in Firebase</li>
 *   <li>Poster can be optional (null or empty)</li>
 *   <li>Poster field is accessible via getter methods</li>
 *   <li>Poster can be updated after event creation</li>
 * </ul>
 *
 * @author Jinn Kasai
 */
public class EventPosterUploadTest {

    private static final String ORGANIZER_ID = "org-123";
    private static final String EVENT_ID = "event-456";
    private static final String POSTER_URL = "https://example.com/poster.jpg";
    private static final String POSTER_BASE64 = "data:image/jpeg;base64,/9j/4AAQSkZJRg==";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockPushedRef;

    @Before
    public void setUp() {
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);
        mockEventRef = mock(DatabaseReference.class);
        mockPushedRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
        when(mockEventRef.push()).thenReturn(mockPushedRef);
        when(mockPushedRef.getKey()).thenReturn(EVENT_ID);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // ==================== Event Creation with Poster Tests ====================

    /**
     * Test Case 1: Organizer can create event with poster URL.
     * 
     * As an organizer, I should be able to create an event with a poster URL
     * that will be displayed to entrants.
     */
    @Test
    public void organizer_canCreateEvent_withPosterUrl() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        String eventId = event.createEvent();

        // Verify event was created
        assertNotNull("Event ID should be generated", eventId);
        assertEquals("Event ID should match", EVENT_ID, eventId);

        // Verify poster is set
        assertEquals("Poster should be set", POSTER_URL, event.getPoster());
    }

    /**
     * Test Case 2: Event poster field is stored in Firebase.
     * 
     * As an organizer, when I create an event with a poster, the poster
     * information should be stored in Firebase.
     */
    @Test
    public void organizer_eventPoster_storedInFirebase() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.createEvent();

        // Verify poster was included in Firebase data
        verify(mockService, times(1)).addEntry(
                argThat(map -> {
                    HashMap<String, Object> data = (HashMap<String, Object>) map;
                    return data.containsKey("poster") &&
                           data.get("poster").equals(POSTER_URL);
                }),
                eq(EVENT_ID)
        );
    }

    /**
     * Test Case 3: Event can be created without poster (poster is optional).
     * 
     * As an organizer, I should be able to create an event without uploading
     * a poster, as the poster is optional.
     */
    @Test
    public void organizer_canCreateEvent_withoutPoster() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, null, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        String eventId = event.createEvent();

        // Verify event was created successfully
        assertNotNull("Event ID should be generated", eventId);
        assertEquals("Event ID should match", EVENT_ID, eventId);

        // Verify poster is empty string (null is converted to empty string in constructor)
        assertEquals("Poster should be empty string", "", event.getPoster());
    }

    /**
     * Test Case 4: Event can be created with empty poster string.
     * 
     * As an organizer, I should be able to create an event with an empty
     * poster string, which is treated as no poster.
     */
    @Test
    public void organizer_canCreateEvent_withEmptyPoster() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "", "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        String eventId = event.createEvent();

        // Verify event was created
        assertNotNull("Event ID should be generated", eventId);
        assertEquals("Poster should be empty", "", event.getPoster());
    }

    /**
     * Test Case 5: Event poster getter returns correct value.
     * 
     * As an organizer, I should be able to retrieve the poster URL
     * from an event using the getter method.
     */
    @Test
    public void organizer_eventPoster_getterReturnsCorrectValue() {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        assertEquals("Poster getter should return correct value", 
                    POSTER_URL, event.getPoster());
    }

    /**
     * Test Case 6: Event poster setter updates the value.
     * 
     * As an organizer, I should be able to update the poster URL
     * of an event using the setter method.
     */
    @Test
    public void organizer_eventPoster_setterUpdatesValue() {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        String newPosterUrl = "https://example.com/new-poster.jpg";
        event.setPoster(newPosterUrl);

        assertEquals("Poster should be updated", 
                    newPosterUrl, event.getPoster());
    }

    /**
     * Test Case 7: Event poster can be set to null.
     * 
     * As an organizer, I should be able to set the poster to null
     * to remove the poster from an event.
     */
    @Test
    public void organizer_eventPoster_canBeSetToNull() {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        event.setPoster(null);

        assertNull("Poster should be null", event.getPoster());
    }

    /**
     * Test Case 8: Event poster can be set to empty string.
     * 
     * As an organizer, I should be able to set the poster to an empty string
     * to indicate no poster.
     */
    @Test
    public void organizer_eventPoster_canBeSetToEmpty() {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        event.setPoster("");

        assertEquals("Poster should be empty string", "", event.getPoster());
    }

    /**
     * Test Case 9: Different events can have different posters.
     * 
     * As an organizer, each event I create can have a unique poster
     * that is different from other events.
     */
    @Test
    public void organizer_differentEvents_canHaveDifferentPosters() throws Exception {
        String poster1 = "https://example.com/poster1.jpg";
        String poster2 = "https://example.com/poster2.jpg";

        Event event1 = new Event(
                ORGANIZER_ID, null, "Event 1", "Description 1",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, poster1, "fun", false
        );

        Event event2 = new Event(
                ORGANIZER_ID, null, "Event 2", "Description 2",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, poster2, "fun", false
        );

        assertNotEquals("Posters should be different", 
                       event1.getPoster(), event2.getPoster());
        assertEquals("Event 1 poster should match", poster1, event1.getPoster());
        assertEquals("Event 2 poster should match", poster2, event2.getPoster());
    }

    /**
     * Test Case 10: Event poster is included in Firebase data structure.
     * 
     * As an organizer, when I create an event with a poster, the poster
     * should be included in the Firebase data structure under the "poster" field.
     */
    @Test
    public void organizer_eventPoster_includedInFirebaseData() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.createEvent();

        // Verify poster field is in the data map
        verify(mockService, times(1)).addEntry(
                argThat(map -> {
                    HashMap<String, Object> data = (HashMap<String, Object>) map;
                    return data.containsKey("poster");
                }),
                eq(EVENT_ID)
        );
    }

    /**
     * Test Case 11: Event poster can be base64 encoded string.
     * 
     * As an organizer, the poster can be stored as a base64 encoded string
     * (which is how it's stored in Firebase Image service).
     */
    @Test
    public void organizer_eventPoster_canBeBase64String() {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_BASE64, "fun", false
        );

        assertEquals("Poster should accept base64 string", 
                    POSTER_BASE64, event.getPoster());
    }

    /**
     * Test Case 12: Event poster field persists after event creation.
     * 
     * As an organizer, after creating an event with a poster, the poster
     * field should remain accessible and unchanged.
     */
    @Test
    public void organizer_eventPoster_persistsAfterCreation() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        // Store poster before creation
        String originalPoster = event.getPoster();

        // Create event
        event.createEvent();

        // Verify poster is still accessible and unchanged
        assertEquals("Poster should persist after creation", 
                    originalPoster, event.getPoster());
        assertEquals("Poster should match original", 
                    POSTER_URL, event.getPoster());
    }

    /**
     * Test Case 13: Event poster can be updated after creation.
     * 
     * As an organizer, I should be able to update the poster of an event
     * after it has been created.
     */
    @Test
    public void organizer_eventPoster_canBeUpdatedAfterCreation() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.createEvent();

        // Update poster
        String newPoster = "https://example.com/updated-poster.jpg";
        event.setPoster(newPoster);

        assertEquals("Poster should be updated", 
                    newPoster, event.getPoster());
    }

    /**
     * Test Case 14: Event poster field is optional in constructor.
     * 
     * As an organizer, the poster parameter in the Event constructor
     * is optional and can be null without causing errors.
     */
    @Test
    public void organizer_eventPoster_optionalInConstructor() {
        // Create event with null poster
        Event event1 = new Event(
                ORGANIZER_ID, EVENT_ID, "Event 1", "Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, null, "fun", false
        );

        // Create event with empty poster
        Event event2 = new Event(
                ORGANIZER_ID, EVENT_ID, "Event 2", "Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "", "fun", false
        );

        // Both should be created successfully
        assertNotNull("Event with null poster should be created", event1);
        assertNotNull("Event with empty poster should be created", event2);
    }

    /**
     * Test Case 15: Event poster can be long URL string.
     * 
     * As an organizer, the poster URL can be a long string without
     * causing issues.
     */
    @Test
    public void organizer_eventPoster_canBeLongUrl() {
        String longPosterUrl = "https://example.com/very/long/path/to/poster/image/file/with/many/segments/poster.jpg";

        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, longPosterUrl, "fun", false
        );

        assertEquals("Poster should accept long URL", 
                    longPosterUrl, event.getPoster());
    }

    /**
     * Test Case 16: Event poster can be data URI format.
     * 
     * As an organizer, the poster can be stored in data URI format
     * (data:image/jpeg;base64,...).
     */
    @Test
    public void organizer_eventPoster_canBeDataUri() {
        String dataUri = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD";

        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, dataUri, "fun", false
        );

        assertEquals("Poster should accept data URI", 
                    dataUri, event.getPoster());
    }

    /**
     * Test Case 17: Event poster is independent of other event fields.
     * 
     * As an organizer, changing the poster should not affect other
     * event fields like name, description, or dates.
     */
    @Test
    public void organizer_eventPoster_independentOfOtherFields() {
        Event event = new Event(
                ORGANIZER_ID, EVENT_ID, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        // Store original values
        String originalName = event.getName();
        String originalDescription = event.getEventDetails();

        // Update poster
        event.setPoster("https://example.com/new-poster.jpg");

        // Verify other fields are unchanged
        assertEquals("Name should be unchanged", 
                    originalName, event.getName());
        assertEquals("Description should be unchanged", 
                    originalDescription, event.getEventDetails());
    }

    /**
     * Test Case 18: Multiple events can share the same poster URL.
     * 
     * As an organizer, multiple events can use the same poster URL
     * if desired.
     */
    @Test
    public void organizer_multipleEvents_canShareSamePoster() {
        Event event1 = new Event(
                ORGANIZER_ID, "E1", "Event 1", "Description 1",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        Event event2 = new Event(
                ORGANIZER_ID, "E2", "Event 2", "Description 2",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        assertEquals("Both events should have same poster", 
                    event1.getPoster(), event2.getPoster());
        assertEquals("Poster should match", 
                    POSTER_URL, event1.getPoster());
        assertEquals("Poster should match", 
                    POSTER_URL, event2.getPoster());
    }

    /**
     * Test Case 19: Event poster field is serializable to Firebase.
     * 
     * As an organizer, the poster field should be properly serialized
     * when saving to Firebase.
     */
    @Test
    public void organizer_eventPoster_serializableToFirebase() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        event.createEvent();

        // Verify poster is included in the serialized data
        verify(mockService, times(1)).addEntry(
                argThat(map -> {
                    HashMap<String, Object> data = (HashMap<String, Object>) map;
                    Object posterValue = data.get("poster");
                    return posterValue != null && 
                           posterValue.toString().equals(POSTER_URL);
                }),
                eq(EVENT_ID)
        );
    }

    /**
     * Test Case 20: Event poster can be retrieved after Firebase save.
     * 
     * As an organizer, after saving an event with a poster to Firebase,
     * I should still be able to retrieve the poster value from the Event object.
     */
    @Test
    public void organizer_eventPoster_retrievableAfterFirebaseSave() throws Exception {
        Event event = new Event(
                ORGANIZER_ID, null, "Test Event", "Event Description",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, POSTER_URL, "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq(EVENT_ID))).thenReturn(EVENT_ID);
        setPrivate(event, "eventService", mockService);

        // Save to Firebase
        event.createEvent();

        // Verify poster is still accessible
        assertNotNull("Poster should be accessible", event.getPoster());
        assertEquals("Poster should match original", 
                    POSTER_URL, event.getPoster());
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

