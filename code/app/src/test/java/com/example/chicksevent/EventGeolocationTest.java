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
 * Unit tests for {@link Event} geolocation functionality.
 * <p>
 * Tests validate that the geolocationRequired field is properly stored,
 * retrieved, and persisted to Firebase.
 * </p>
 *
 * @author Jinn Kasai
 */
public class EventGeolocationTest {

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

    @Test
    public void createEvent_withGeolocationRequired_includesInFirebase() throws Exception {
        Event e = new Event(
                "U1", null, "Test Event", "Desc", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", true  // geolocationRequired = true
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(e, "eventService", mockService);

        e.createEvent();

        // Verify that geolocationRequired is included in the map sent to Firebase
        verify(mockService, times(1)).addEntry(argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(true);
        }), eq("E123"));
    }

    @Test
    public void createEvent_withoutGeolocationRequired_defaultsToFalse() throws Exception {
        Event e = new Event(
                "U1", null, "Test Event", "Desc", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", false  // geolocationRequired = false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(e, "eventService", mockService);

        e.createEvent();

        // Verify that geolocationRequired is false
        verify(mockService, times(1)).addEntry(argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(false);
        }), eq("E123"));
    }

    @Test
    public void geolocationRequired_getterAndSetter_workCorrectly() {
        Event e = new Event(
                "U1", "E1", "Test", "Desc", null, null,
                null, null, null, null,
                10, null, null, false
        );

        // Test default value
        assertFalse(e.isGeolocationRequired());

        // Test setter
        e.setGeolocationRequired(true);
        assertTrue(e.isGeolocationRequired());

        // Test setter again
        e.setGeolocationRequired(false);
        assertFalse(e.isGeolocationRequired());
    }

    @Test
    public void constructor_withGeolocationRequired_preservesValue() {
        Event e1 = new Event(
                "U1", "E1", "Event1", "Desc", null, null,
                null, null, null, null,
                10, null, null, true
        );
        assertTrue(e1.isGeolocationRequired());

        Event e2 = new Event(
                "U1", "E2", "Event2", "Desc", null, null,
                null, null, null, null,
                10, null, null, false
        );
        assertFalse(e2.isGeolocationRequired());
    }

    // ==================== US 02.02.03: Organizer Enable/Disable Geolocation Tests ====================

    /**
     * Test Case 1: Organizer can enable geolocation requirement on an existing event.
     * 
     * As an organizer, I want to enable the geolocation requirement for my event.
     * This test verifies that an organizer can successfully enable geolocation
     * on an event that previously had it disabled.
     */
    @Test
    public void organizer_enableGeolocation_onExistingEvent_persistsToFirebase() throws Exception {
        // Create event with geolocation disabled
        Event event = new Event(
                "org-123", "E456", "Test Event", "Description", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", false
        );
        
        // Organizer enables geolocation
        event.setGeolocationRequired(true);
        assertTrue("Geolocation should be enabled", event.isGeolocationRequired());
        
        // Mock Firebase service
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq("E456"), any(HashMap.class))).thenReturn("E456");
        setPrivate(event, "eventService", mockService);
        
        // Organizer saves the change
        event.editEvent("E456");
        
        // Verify geolocation requirement is persisted to Firebase
        verify(mockService, times(1)).editEntry(eq("E456"), argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(true);
        }));
    }

    /**
     * Test Case 2: Organizer can disable geolocation requirement on an existing event.
     * 
     * As an organizer, I want to disable the geolocation requirement for my event.
     * This test verifies that an organizer can successfully disable geolocation
     * on an event that previously had it enabled.
     */
    @Test
    public void organizer_disableGeolocation_onExistingEvent_persistsToFirebase() throws Exception {
        // Create event with geolocation enabled
        Event event = new Event(
                "org-123", "E456", "Test Event", "Description", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", true
        );
        
        // Organizer disables geolocation
        event.setGeolocationRequired(false);
        assertFalse("Geolocation should be disabled", event.isGeolocationRequired());
        
        // Mock Firebase service
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq("E456"), any(HashMap.class))).thenReturn("E456");
        setPrivate(event, "eventService", mockService);
        
        // Organizer saves the change
        event.editEvent("E456");
        
        // Verify geolocation requirement is persisted to Firebase
        verify(mockService, times(1)).editEntry(eq("E456"), argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(false);
        }));
    }

    /**
     * Test Case 3: Organizer can toggle geolocation requirement multiple times.
     * 
     * As an organizer, I want to be able to change my mind and toggle the
     * geolocation requirement on and off multiple times.
     */
    @Test
    public void organizer_toggleGeolocation_multipleTimes_preservesFinalState() throws Exception {
        Event event = new Event(
                "org-123", "E456", "Test Event", "Description", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", false
        );
        
        // Organizer toggles: false -> true -> false -> true
        event.setGeolocationRequired(true);
        assertTrue(event.isGeolocationRequired());
        
        event.setGeolocationRequired(false);
        assertFalse(event.isGeolocationRequired());
        
        event.setGeolocationRequired(true);
        assertTrue(event.isGeolocationRequired());
        
        // Final state should be true
        assertTrue("Final geolocation state should be true", event.isGeolocationRequired());
    }

    /**
     * Test Case 4: Organizer can enable geolocation when creating a new event.
     * 
     * As an organizer, I want to enable geolocation requirement when I first
     * create my event, so entrants must provide location to join.
     */
    @Test
    public void organizer_createEvent_withGeolocationEnabled_persistsCorrectly() throws Exception {
        Event event = new Event(
                "org-123", null, "New Event", "Event Description", "09:00", "17:00",
                "2025-02-01", "2025-02-01",
                "2025-01-01", "2025-01-31",
                100, "new-poster.png", "sports", true  // geolocation enabled
        );
        
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(event, "eventService", mockService);
        
        // Organizer creates event with geolocation enabled
        event.createEvent();
        
        // Verify geolocation is included and set to true in Firebase
        verify(mockService, times(1)).addEntry(argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(true) &&
                   map.get("name").equals("New Event");
        }), eq("E123"));
    }

    /**
     * Test Case 5: Organizer can disable geolocation when creating a new event.
     * 
     * As an organizer, I want to disable geolocation requirement when I first
     * create my event, so entrants can join without providing location.
     */
    @Test
    public void organizer_createEvent_withGeolocationDisabled_persistsCorrectly() throws Exception {
        Event event = new Event(
                "org-123", null, "New Event", "Event Description", "09:00", "17:00",
                "2025-02-01", "2025-02-01",
                "2025-01-01", "2025-01-31",
                100, "new-poster.png", "sports", false  // geolocation disabled
        );
        
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(event, "eventService", mockService);
        
        // Organizer creates event with geolocation disabled
        event.createEvent();
        
        // Verify geolocation is included and set to false in Firebase
        verify(mockService, times(1)).addEntry(argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(false) &&
                   map.get("name").equals("New Event");
        }), eq("E123"));
    }

    /**
     * Test Case 6: Organizer can change geolocation setting after event creation.
     * 
     * As an organizer, I want to update the geolocation requirement after I've
     * already created the event, allowing me to change my decision later.
     */
    @Test
    public void organizer_updateGeolocation_afterEventCreation_persistsChange() throws Exception {
        // Create event with geolocation disabled
        Event event = new Event(
                "org-123", "E789", "Existing Event", "Description", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", false
        );
        
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq("E789"), any(HashMap.class))).thenReturn("E789");
        setPrivate(event, "eventService", mockService);
        
        // Organizer changes mind and enables geolocation
        event.setGeolocationRequired(true);
        event.editEvent("E789");
        
        // Verify the change was persisted
        verify(mockService, times(1)).editEntry(eq("E789"), argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(true);
        }));
    }

    /**
     * Test Case 7: Organizer's geolocation change preserves other event fields.
     * 
     * As an organizer, when I update the geolocation requirement, I want to ensure
     * that all other event details remain unchanged.
     */
    @Test
    public void organizer_updateGeolocation_preservesOtherEventFields() throws Exception {
        Event event = new Event(
                "org-123", "E999", "Preserved Event", "Original Description", "11:00", "19:00",
                "2025-03-01", "2025-03-02",
                "2025-02-01", "2025-02-28",
                75, "original-poster.png", "music", false
        );
        
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq("E999"), any(HashMap.class))).thenReturn("E999");
        setPrivate(event, "eventService", mockService);
        
        // Organizer only changes geolocation
        event.setGeolocationRequired(true);
        event.editEvent("E999");
        
        // Verify all other fields are preserved
        verify(mockService, times(1)).editEntry(eq("E999"), argThat(map -> {
            return map.get("geolocationRequired").equals(true) &&
                   map.get("name").equals("Preserved Event") &&
                   map.get("eventDetails").equals("Original Description") &&
                   map.get("entrantLimit").equals(75) &&
                   map.get("tag").equals("music");
        }));
    }

    /**
     * Test Case 8: Organizer can independently change geolocation without affecting other settings.
     * 
     * As an organizer, I want to change only the geolocation requirement without
     * accidentally modifying other event properties.
     */
    @Test
    public void organizer_changeGeolocation_independentlyOfOtherFields() throws Exception {
        Event event = new Event(
                "org-123", "E111", "Independent Event", "Details", "12:00", "20:00",
                "2025-04-01", "2025-04-02",
                "2025-03-01", "2025-03-31",
                200, "poster.jpg", "tech", true
        );
        
        // Store original values
        String originalName = event.getName();
        String originalDetails = event.getEventDetails();
        int originalLimit = event.getEntrantLimit();
        
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq("E111"), any(HashMap.class))).thenReturn("E111");
        setPrivate(event, "eventService", mockService);
        
        // Organizer only changes geolocation
        event.setGeolocationRequired(false);
        event.editEvent("E111");
        
        // Verify other fields unchanged
        assertEquals("Name should be unchanged", originalName, event.getName());
        assertEquals("Details should be unchanged", originalDetails, event.getEventDetails());
        assertEquals("Limit should be unchanged", originalLimit, event.getEntrantLimit());
        assertFalse("Geolocation should be changed", event.isGeolocationRequired());
    }

    /**
     * Test Case 9: Organizer's geolocation setting is correctly retrieved after edit.
     * 
     * As an organizer, after I update the geolocation requirement, I want to verify
     * that the change is correctly reflected when I check the event.
     */
    @Test
    public void organizer_retrieveGeolocation_afterUpdate_reflectsCorrectState() throws Exception {
        Event event = new Event(
                "org-123", "E222", "Retrieval Test", "Test", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "test", false
        );
        
        // Organizer enables geolocation
        event.setGeolocationRequired(true);
        
        // Verify getter returns correct state
        assertTrue("isGeolocationRequired() should return true", event.isGeolocationRequired());
        
        // Organizer disables geolocation
        event.setGeolocationRequired(false);
        
        // Verify getter returns correct state
        assertFalse("isGeolocationRequired() should return false", event.isGeolocationRequired());
    }

    /**
     * Test Case 10: Organizer can verify geolocation state persists through editEvent operation.
     * 
     * As an organizer, I want to ensure that when I update the geolocation requirement
     * and save it, the state is maintained correctly throughout the operation.
     */
    @Test
    public void organizer_geolocationState_persistsThroughEditOperation() throws Exception {
        Event event = new Event(
                "org-123", "E333", "Persistence Test", "Description", "10:00", "18:00",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "test", false
        );
        
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.editEntry(eq("E333"), any(HashMap.class))).thenReturn("E333");
        setPrivate(event, "eventService", mockService);
        
        // Organizer enables geolocation
        event.setGeolocationRequired(true);
        assertTrue("Geolocation should be enabled before edit", event.isGeolocationRequired());
        
        // Organizer saves the change
        String resultId = event.editEvent("E333");
        
        // Verify state is maintained after edit
        assertTrue("Geolocation should remain enabled after edit", event.isGeolocationRequired());
        assertEquals("Event ID should be preserved", "E333", resultId);
        
        // Verify Firebase was called with correct value
        verify(mockService, times(1)).editEntry(eq("E333"), argThat(map -> {
            return map.get("geolocationRequired").equals(true);
        }));
    }

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

