package com.example.chicksevent;

import static org.junit.Assert.*;

import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.util.QRCodeGenerator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * Unit tests for US 02.01.01: As an organizer I want to create a new event 
 * and generate a unique promotional QR code that links to the event description 
 * and event poster in the app.
 * <p>
 * These tests validate that:
 * <ul>
 *   <li>Events can be created with unique IDs</li>
 *   <li>QR codes are generated with correct deep link format</li>
 *   <li>QR codes link to the event (which contains description and poster)</li>
 *   <li>Each event gets a unique QR code</li>
 * </ul>
 * </p>
 *
 * @author Jinn Kasai
 */
public class EventQRCodeTest {

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
        when(mockPushedRef.getKey()).thenReturn("E123");
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // ==================== US 02.01.01: Event Creation and QR Code Tests ====================

    /**
     * Test Case 1: Organizer can create a new event with a unique ID.
     * 
     * As an organizer, when I create a new event, it should be assigned
     * a unique event ID that can be used for QR code generation.
     */
    @Test
    public void organizer_createEvent_generatesUniqueEventId() throws Exception {
        Event event = new Event(
                "org-123", null, "Test Event", "Event Description", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(event, "eventService", mockService);

        // Organizer creates event
        String eventId = event.createEvent();

        // Verify event ID is generated and returned
        assertNotNull("Event ID should be generated", eventId);
        assertEquals("Event ID should match", "E123", eventId);
        assertEquals("Event ID should be set in event object", "E123", event.getId());
    }

    /**
     * Test Case 2: QR code deep link is generated with correct format for an event.
     * 
     * As an organizer, when a QR code is generated for my event, it should
     * contain a deep link in the format "chicksevent://event/{eventId}".
     */
    @Test
    public void organizer_qrCodeDeepLink_hasCorrectFormat() {
        String eventId = "E456";
        
        // Generate deep link for the event
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        // Verify deep link format
        assertNotNull("Deep link should be generated", deepLink);
        assertTrue("Deep link should start with chicksevent://", 
                  deepLink.startsWith("chicksevent://"));
        assertTrue("Deep link should contain /event/", 
                  deepLink.contains("/event/"));
        assertEquals("Deep link should match expected format", 
                    "chicksevent://event/E456", deepLink);
    }

    /**
     * Test Case 3: QR code deep link contains the correct event ID.
     * 
     * As an organizer, the QR code deep link should contain my event's
     * unique ID so it links to the correct event.
     */
    @Test
    public void organizer_qrCodeDeepLink_containsEventId() {
        String eventId = "E789";
        
        // Generate deep link
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        // Verify event ID is in the deep link
        assertTrue("Deep link should contain event ID", 
                  deepLink.contains(eventId));
        assertTrue("Deep link should end with event ID", 
                  deepLink.endsWith(eventId));
    }

    /**
     * Test Case 4: Deep link is generated correctly for QR code creation.
     * 
     * As an organizer, when a QR code is generated, it should use a deep link
     * that correctly references the event.
     * 
     * Note: Actual bitmap generation requires Android runtime and is tested
     * in instrumented tests. This unit test verifies the deep link format.
     */
    @Test
    public void organizer_qrCodeBitmap_generatedFromDeepLink() {
        String eventId = "E999";
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        // Verify deep link is valid for QR code generation
        // The deep link will be used to generate the QR code bitmap
        assertNotNull("Deep link should be valid", deepLink);
        assertFalse("Deep link should not be empty", deepLink.isEmpty());
        assertTrue("Deep link should contain event ID", deepLink.contains(eventId));
        assertEquals("Deep link should match expected format", 
                     "chicksevent://event/E999", deepLink);
        
        // Note: QR code bitmap generation is tested in instrumented tests
        // where Android runtime is available
    }

    /**
     * Test Case 5: Different events generate different QR codes.
     * 
     * As an organizer, each event I create should have a unique QR code
     * that links to that specific event.
     */
    @Test
    public void organizer_differentEvents_generateDifferentQRCodes() {
        String eventId1 = "E111";
        String eventId2 = "E222";
        
        // Generate deep links for different events
        String deepLink1 = QRCodeGenerator.generateEventDeepLink(eventId1);
        String deepLink2 = QRCodeGenerator.generateEventDeepLink(eventId2);
        
        // Verify deep links are different
        assertNotEquals("Different events should have different deep links", 
                       deepLink1, deepLink2);
        assertTrue("First deep link should contain first event ID", 
                  deepLink1.contains(eventId1));
        assertTrue("Second deep link should contain second event ID", 
                  deepLink2.contains(eventId2));
        
        // Verify deep links are different (which ensures different QR codes will be generated)
        // Since different deep links encode different data, they will produce different QR codes
        // Note: Actual QR code bitmap generation requires Android runtime and is tested
        // in instrumented tests. This unit test verifies the deep links are unique.
        assertNotEquals("Deep links should be different", deepLink1, deepLink2);
    }

    /**
     * Test Case 6: QR code deep link format is consistent across events.
     * 
     * As an organizer, all QR codes should follow the same deep link format
     * regardless of the event ID, ensuring consistent behavior.
     */
    @Test
    public void organizer_qrCodeDeepLink_formatIsConsistent() {
        String[] eventIds = {"E001", "E002", "E999", "EVENT-123", "test-event-id"};
        
        for (String eventId : eventIds) {
            String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
            
            // Verify consistent format
            assertTrue("Deep link should start with chicksevent://", 
                      deepLink.startsWith("chicksevent://"));
            assertTrue("Deep link should contain /event/", 
                      deepLink.contains("/event/"));
            assertTrue("Deep link should end with event ID", 
                      deepLink.endsWith(eventId));
        }
    }

    /**
     * Test Case 7: Event creation and QR code generation work together.
     * 
     * As an organizer, when I create an event, I should be able to generate
     * a QR code using the event's ID that links back to the event.
     */
    @Test
    public void organizer_createEvent_thenGenerateQRCode_worksTogether() throws Exception {
        // Create event
        Event event = new Event(
                "org-123", null, "QR Test Event", "Description", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "test", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(event, "eventService", mockService);

        // Organizer creates event
        String eventId = event.createEvent();
        assertNotNull("Event should be created with ID", eventId);

        // Organizer generates deep link for QR code
        // The deep link will be used to generate the QR code bitmap
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);

        // Verify deep link is correct and links to the created event
        assertTrue("Deep link should contain event ID", deepLink.contains(eventId));
        assertEquals("Deep link should match expected format", 
                     "chicksevent://event/" + eventId, deepLink);
        
        // Note: QR code bitmap generation requires Android runtime and is tested
        // in instrumented tests. This unit test verifies event creation and
        // deep link generation work together correctly.
    }

    /**
     * Test Case 8: QR code deep link correctly formats event IDs with special characters.
     * 
     * As an organizer, even if my event ID contains special characters or
     * unusual formats, the QR code deep link should still be generated correctly.
     */
    @Test
    public void organizer_qrCodeDeepLink_handlesSpecialEventIds() {
        String[] specialEventIds = {
            "E-123",
            "event_456",
            "EVENT.789",
            "test-event-id-123",
            "EventWithMixedCase"
        };

        for (String eventId : specialEventIds) {
            String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
            
            // Verify deep link is generated and contains the event ID
            assertNotNull("Deep link should be generated for: " + eventId, deepLink);
            assertTrue("Deep link should contain event ID: " + eventId, 
                      deepLink.contains(eventId));
            assertTrue("Deep link should follow format: " + eventId, 
                      deepLink.startsWith("chicksevent://event/"));
        }
    }

    /**
     * Test Case 9: QR code save method handles null bitmap gracefully.
     * 
     * As an organizer, the QR code save functionality should handle edge cases
     * gracefully, such as when bitmap generation fails.
     * 
     * Note: In unit tests, bitmap generation is not available. This test verifies
     * the save method handles null correctly. Actual bitmap saving is tested
     * in instrumented tests.
     */
    @Test
    public void organizer_qrCodeBitmap_canBeSavedToFile() throws Exception {
        String eventId = "E555";
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        // Create a temporary file to test saving
        File tempFile = File.createTempFile("test_qr_", ".png");
        tempFile.deleteOnExit();
        
        // Test that save method handles null bitmap correctly
        // In unit tests, we can't generate bitmaps, but we can test null handling
        boolean saved = QRCodeGenerator.saveQRCodeToFile(null, tempFile);
        
        // Verify save method returns false for null bitmap
        assertFalse("Save should return false for null bitmap", saved);
        
        // Verify deep link is valid
        assertNotNull("Deep link should be generated", deepLink);
        assertTrue("Deep link should contain event ID", deepLink.contains(eventId));
        
        // Note: Actual bitmap generation and saving is tested in instrumented tests
    }

    /**
     * Test Case 10: Event with description and poster can have QR code generated.
     * 
     * As an organizer, when I create an event with a description and poster,
     * I should be able to generate a QR code that links to this event,
     * allowing others to view the description and poster.
     */
    @Test
    public void organizer_eventWithDescriptionAndPoster_canGenerateQRCode() throws Exception {
        // Create event with description and poster
        Event event = new Event(
                "org-123", null, "Event with Poster", 
                "This is a detailed event description with information about the event.",
                "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster-image.png", "music", false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(event, "eventService", mockService);

        // Organizer creates event
        String eventId = event.createEvent();
        
        // Verify event has description and poster
        assertNotNull("Event description should be set", event.getEventDetails());
        assertNotNull("Event poster should be set", event.getPoster());
        assertFalse("Event description should not be empty", 
                   event.getEventDetails().isEmpty());

        // Generate deep link for QR code that will link to this event
        // The deep link will navigate to EventDetailFragment which shows
        // the event description and poster, so the QR code effectively
        // links to both the description and poster
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);

        // Verify deep link is correct and contains event ID
        assertTrue("Deep link should contain event ID", deepLink.contains(eventId));
        assertEquals("Deep link should match expected format", 
                     "chicksevent://event/" + eventId, deepLink);
        
        // Verify the deep link format allows navigation to event details
        // which will display both the description and poster
        assertTrue("Deep link should start with chicksevent://", 
                  deepLink.startsWith("chicksevent://"));
        assertTrue("Deep link should contain /event/ path", 
                  deepLink.contains("/event/"));
        
        // Note: QR code bitmap generation requires Android runtime and is tested
        // in instrumented tests. This unit test verifies that events with
        // description and poster can have QR codes generated (via deep link)
    }

    // -------------------- helpers --------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

