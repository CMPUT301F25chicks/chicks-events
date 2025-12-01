package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.util.Log;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Unit tests for US 01.06.02: As an entrant I want to be able to join 
 * the waitlist from the event details.
 * These tests validate that:
 * <ul>
 *   <li>Entrants can join the waiting list from event details</li>
 *   <li>Entrant is created with correct event ID and user ID</li>
 *   <li>Join waiting list operation sets correct status (WAITING)</li>
 *   <li>Join waiting list with location data works correctly</li>
 *   <li>Firebase operations are called correctly</li>
 *   <li>Edge cases are handled properly</li>
 * </ul>
 *
 * @author Jinn Kasai
 */
public class EntrantJoinWaitlistTest {

    private static final String EVENT_ID = "event-123";
    private static final String ENTRANT_ID = "entrant-456";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private MockedStatic<Log> logStatic;

    private FirebaseService mockWaitingSvc;
    private FirebaseService mockEntrantSvc;
    private FirebaseService mockEventSvc;

    private Entrant entrant;

    @Before
    public void setUp() throws Exception {
        // Block FirebaseApp init from FirebaseService constructor(s)
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        FirebaseDatabase mockDb = mock(FirebaseDatabase.class);
        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        // Mock android.util.Log (all common methods used in code)
        logStatic = mockStatic(Log.class);
        when(Log.i(anyString(), anyString())).thenReturn(0);
        when(Log.d(anyString(), anyString())).thenReturn(0);
        when(Log.e(anyString(), anyString())).thenReturn(0);
        when(Log.e(anyString(), anyString(), any(Throwable.class))).thenReturn(0);
        when(Log.w(anyString(), anyString(), any(Throwable.class))).thenReturn(0);

        // Create entrant instance
        entrant = new Entrant(ENTRANT_ID, EVENT_ID);

        // Create mock services
        mockWaitingSvc = mock(FirebaseService.class);
        mockEntrantSvc = mock(FirebaseService.class);
        mockEventSvc = mock(FirebaseService.class);

        // Inject mocked services via reflection
        setPrivate(entrant, "waitingListService", mockWaitingSvc);
        setPrivate(entrant, "entrantService", mockEntrantSvc);
        setPrivate(entrant, "eventService", mockEventSvc);

        // Make void methods safe
        doNothing().when(mockWaitingSvc).updateSubCollectionEntry(
                anyString(), anyString(), anyString(), any(HashMap.class));
        doNothing().when(mockWaitingSvc).deleteSubCollectionEntry(
                anyString(), anyString(), anyString());
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
        if (logStatic != null) logStatic.close();
    }

    // ==================== Entrant Creation Tests ====================

    /**
     * Test Case 1: Entrant can be created with event ID and user ID.
     * 
     * As an entrant, when I view event details, an Entrant object should
     * be created with my user ID and the event ID.
     */
    @Test
    public void entrant_canBeCreated_withEventIdAndUserId() {
        // Entrant is created in setUp()
        
        assertNotNull("Entrant should be created", entrant);
        assertEquals("Event ID should match", EVENT_ID, entrant.getEventId());
        assertEquals("Entrant ID should match", ENTRANT_ID, entrant.getEntrantId());
    }

    /**
     * Test Case 2: Entrant has default WAITING status when created.
     * 
     * As an entrant, when an Entrant object is created, it should have
     * a default status of WAITING.
     */
    @Test
    public void entrant_hasDefaultWaitingStatus_whenCreated() {
        assertEquals("Default status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    // ==================== Join Waiting List Tests ====================

    /**
     * Test Case 3: Entrant can join waiting list from event details.
     * 
     * As an entrant, I should be able to join the waiting list for an event
     * when viewing event details.
     */
    @Test
    public void entrant_canJoinWaitingList_fromEventDetails() {
        // Entrant joins waiting list
        entrant.joinWaitingList();

        // Verify Firebase operation was called
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                any(HashMap.class));

        // Verify status is set to WAITING
        assertEquals("Status should be WAITING after joining", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 4: Entrant status is set to WAITING when joining.
     * 
     * As an entrant, when I join the waiting list, my status should be
     * set to WAITING.
     */
    @Test
    public void entrant_statusSetToWaiting_whenJoining() {
        // Join waiting list
        entrant.joinWaitingList();

        // Verify status
        assertEquals("Status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 5: Entrant can join waiting list with location data.
     * 
     * As an entrant, if geolocation is required for the event, I should
     * be able to join the waiting list with my location coordinates.
     */
    @Test
    public void entrant_canJoinWaitingList_withLocation() {
        Double latitude = 53.5461;
        Double longitude = -113.4938;

        // Join waiting list with location
        entrant.joinWaitingList(latitude, longitude);

        // Verify Firebase operation was called with location data
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                argThat(data -> {
                    HashMap<String, Object> map = (HashMap<String, Object>) data;
                    return map.containsKey("latitude") && 
                           map.containsKey("longitude") &&
                           map.get("latitude").equals(latitude) &&
                           map.get("longitude").equals(longitude);
                }));

        // Verify status is set
        assertEquals("Status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 6: Entrant can join waiting list without location data.
     * 
     * As an entrant, if geolocation is not required, I should be able
     * to join the waiting list without providing location.
     */
    @Test
    public void entrant_canJoinWaitingList_withoutLocation() {
        // Join waiting list without location
        entrant.joinWaitingList();

        // Verify Firebase operation was called
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                any(HashMap.class));

        // Verify status is set
        assertEquals("Status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 7: Join waiting list uses correct Firebase path.
     * 
     * As an entrant, when I join the waiting list, the data should be
     * stored in the correct Firebase path: WaitingList/{eventId}/WAITING/{entrantId}
     */
    @Test
    public void entrant_joinWaitingList_usesCorrectFirebasePath() {
        entrant.joinWaitingList();

        // Verify correct path structure
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID),      // eventId
                eq("WAITING"),     // status
                eq(ENTRANT_ID),    // entrantId
                any(HashMap.class) // data
        );
    }

    /**
     * Test Case 8: Join waiting list can be called multiple times.
     * 
     * As an entrant, if I accidentally try to join the waiting list multiple
     * times, the operation should still work (idempotent behavior).
     */
    @Test
    public void entrant_canJoinWaitingList_multipleTimes() {
        // Join multiple times
        entrant.joinWaitingList();
        entrant.joinWaitingList();
        entrant.joinWaitingList();

        // Verify Firebase operation was called multiple times
        verify(mockWaitingSvc, times(3)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                any(HashMap.class));

        // Status should still be WAITING
        assertEquals("Status should remain WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 9: Join waiting list with null location handles gracefully.
     * 
     * As an entrant, if location is null when joining, the operation
     * should still succeed without location data.
     */
    @Test
    public void entrant_joinWaitingList_withNullLocation_handlesGracefully() {
        // Join with null location (should work like no location)
        entrant.joinWaitingList(null, null);

        // Verify Firebase operation was called
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                any(HashMap.class));

        // Verify status is set
        assertEquals("Status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 10: Join waiting list with partial location (only latitude).
     * 
     * As an entrant, if only latitude is provided (longitude is null),
     * the operation should handle it gracefully.
     */
    @Test
    public void entrant_joinWaitingList_withPartialLocation_handlesGracefully() {
        Double latitude = 53.5461;

        // Join with only latitude (longitude is null)
        entrant.joinWaitingList(EntrantStatus.WAITING, latitude, null);

        // Verify Firebase operation was called
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                any(HashMap.class));

        // Status should be set
        assertEquals("Status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 11: Join waiting list with partial location (only longitude).
     * 
     * As an entrant, if only longitude is provided (latitude is null),
     * the operation should handle it gracefully.
     */
    @Test
    public void entrant_joinWaitingList_withOnlyLongitude_handlesGracefully() {
        Double longitude = -113.4938;

        // Join with only longitude (latitude is null)
        entrant.joinWaitingList(EntrantStatus.WAITING, null, longitude);

        // Verify Firebase operation was called
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                any(HashMap.class));

        // Status should be set
        assertEquals("Status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 12: Join waiting list with specific status works.
     * 
     * As an entrant, I should be able to join with a specific status
     * (though typically WAITING is used from event details).
     */
    @Test
    public void entrant_canJoinWaitingList_withSpecificStatus() {
        // Join with specific status
        entrant.joinWaitingList(EntrantStatus.WAITING);

        // Verify Firebase operation was called with correct status
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                any(HashMap.class));

        // Verify status is set
        assertEquals("Status should match", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 13: Join waiting list updates entrant status correctly.
     * 
     * As an entrant, when I join the waiting list, my internal status
     * should be updated to reflect that I'm on the waiting list.
     */
    @Test
    public void entrant_joinWaitingList_updatesStatusCorrectly() {
        // Initially status is WAITING (from constructor)
        assertEquals("Initial status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());

        // Join waiting list
        entrant.joinWaitingList();

        // Status should still be WAITING
        assertEquals("Status should remain WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 14: Different entrants can join same event waiting list.
     * 
     * As an entrant, multiple entrants should be able to join the same
     * event's waiting list without conflicts.
     */
    @Test
    public void entrant_multipleEntrants_canJoinSameEvent() throws Exception {
        String entrantId2 = "entrant-789";
        Entrant entrant2 = new Entrant(entrantId2, EVENT_ID);

        // Inject mocked services
        setPrivate(entrant2, "waitingListService", mockWaitingSvc);
        setPrivate(entrant2, "entrantService", mockEntrantSvc);
        setPrivate(entrant2, "eventService", mockEventSvc);

        // Both entrants join
        entrant.joinWaitingList();
        entrant2.joinWaitingList();

        // Verify both operations were called
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), eq("WAITING"), eq(ENTRANT_ID), any(HashMap.class));
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), eq("WAITING"), eq(entrantId2), any(HashMap.class));
    }

    /**
     * Test Case 15: Entrant can join different events' waiting lists.
     * 
     * As an entrant, I should be able to join waiting lists for multiple
     * different events.
     */
    @Test
    public void entrant_canJoinMultipleEvents() throws Exception {
        String eventId2 = "event-789";
        Entrant entrant2 = new Entrant(ENTRANT_ID, eventId2);

        // Inject mocked services
        FirebaseService mockWaitingSvc2 = mock(FirebaseService.class);
        doNothing().when(mockWaitingSvc2).updateSubCollectionEntry(
                anyString(), anyString(), anyString(), any(HashMap.class));
        setPrivate(entrant2, "waitingListService", mockWaitingSvc2);
        setPrivate(entrant2, "entrantService", mockEntrantSvc);
        setPrivate(entrant2, "eventService", mockEventSvc);

        // Join both events
        entrant.joinWaitingList();
        entrant2.joinWaitingList();

        // Verify both operations were called with different event IDs
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), eq("WAITING"), eq(ENTRANT_ID), any(HashMap.class));
        verify(mockWaitingSvc2, times(1)).updateSubCollectionEntry(
                eq(eventId2), eq("WAITING"), eq(ENTRANT_ID), any(HashMap.class));
    }

    /**
     * Test Case 16: Join waiting list includes placeholder data.
     * 
     * As an entrant, when I join the waiting list, the data structure
     * should include the required placeholder field for backward compatibility.
     */
    @Test
    public void entrant_joinWaitingList_includesPlaceholderData() {
        entrant.joinWaitingList();

        // Verify data includes placeholder
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                argThat(data -> {
                    HashMap<String, Object> map = (HashMap<String, Object>) data;
                    return map.containsKey(" "); // Placeholder key
                }));
    }

    /**
     * Test Case 17: Join waiting list with location includes both coordinates.
     * 
     * As an entrant, when I join with location, both latitude and longitude
     * should be included in the data.
     */
    @Test
    public void entrant_joinWaitingList_withLocation_includesBothCoordinates() {
        Double latitude = 53.5461;
        Double longitude = -113.4938;

        entrant.joinWaitingList(latitude, longitude);

        // Verify both coordinates are included
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), 
                eq("WAITING"), 
                eq(ENTRANT_ID), 
                argThat(data -> {
                    HashMap<String, Object> map = (HashMap<String, Object>) data;
                    return map.containsKey("latitude") && 
                           map.containsKey("longitude") &&
                           map.get("latitude").equals(latitude) &&
                           map.get("longitude").equals(longitude);
                }));
    }

    /**
     * Test Case 18: Entrant getters return correct values after joining.
     * 
     * As an entrant, after joining the waiting list, getter methods should
     * return the correct event ID, entrant ID, and status.
     */
    @Test
    public void entrant_getters_returnCorrectValues_afterJoining() {
        // Join waiting list
        entrant.joinWaitingList();

        // Verify getters
        assertEquals("Event ID should match", EVENT_ID, entrant.getEventId());
        assertEquals("Entrant ID should match", ENTRANT_ID, entrant.getEntrantId());
        assertEquals("Status should be WAITING", 
                    EntrantStatus.WAITING, entrant.getStatus());
    }

    /**
     * Test Case 19: Join waiting list does not affect other entrant properties.
     * 
     * As an entrant, joining the waiting list should only update the status
     * and Firebase, not affect other properties like event ID or entrant ID.
     */
    @Test
    public void entrant_joinWaitingList_doesNotAffectOtherProperties() {
        // Store initial values
        String initialEventId = entrant.getEventId();
        String initialEntrantId = entrant.getEntrantId();

        // Join waiting list
        entrant.joinWaitingList();

        // Verify properties are unchanged
        assertEquals("Event ID should not change", 
                    initialEventId, entrant.getEventId());
        assertEquals("Entrant ID should not change", 
                    initialEntrantId, entrant.getEntrantId());
    }

    /**
     * Test Case 20: Entrant is not an organizer.
     * 
     * As an entrant, the isOrganizer() method should return false,
     * distinguishing entrants from organizers.
     */
    @Test
    public void entrant_isNotOrganizer() {
        assertFalse("Entrant should not be an organizer", 
                   entrant.isOrganizer());
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

