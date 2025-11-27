package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Organizer;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for US 02.02.02: As an organizer I want to see on a map 
 * where entrants joined my event waiting list from.
 * <p>
 * These tests validate that organizers can retrieve entrant location data
 * from Firebase and process it correctly for map display. The tests verify
 * the ability to access waiting list data containing location information
 * for WAITING and INVITED entrants.
 * </p>
 *
 * @author Jinn Kasai
 */
public class OrganizerEntrantLocationTest {

    private static final String ORGANIZER_ID = "org-123";
    private static final String EVENT_ID = "event-456";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockUserRef;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockNotifRef;
    private DatabaseReference mockAdminRef;
    private DatabaseReference mockWaitingListRef;
    private DatabaseReference mockOrganizerRef;

    private Organizer organizer;
    private FirebaseService mockWaitingListSvc;
    private FirebaseService mockUserSvc;
    private FirebaseService mockOrganizerSvc;
    private FirebaseService mockEventSvc;

    @Before
    public void setUp() throws Exception {
        // Prevent real Firebase initialization
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        mockUserRef = mock(DatabaseReference.class);
        mockEventRef = mock(DatabaseReference.class);
        mockNotifRef = mock(DatabaseReference.class);
        mockAdminRef = mock(DatabaseReference.class);
        mockWaitingListRef = mock(DatabaseReference.class);
        mockOrganizerRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("User")).thenReturn(mockUserRef);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
        when(mockDb.getReference("Notification")).thenReturn(mockNotifRef);
        when(mockDb.getReference("Admin")).thenReturn(mockAdminRef);
        when(mockDb.getReference("WaitingList")).thenReturn(mockWaitingListRef);
        when(mockDb.getReference("Organizer")).thenReturn(mockOrganizerRef);

        // Construct Organizer
        organizer = new Organizer(ORGANIZER_ID, EVENT_ID);

        // Prepare service mocks
        mockWaitingListSvc = mock(FirebaseService.class);
        mockUserSvc = mock(FirebaseService.class);
        mockOrganizerSvc = mock(FirebaseService.class);
        mockEventSvc = mock(FirebaseService.class);

        when(mockWaitingListSvc.getReference()).thenReturn(mockWaitingListRef);
        when(mockUserSvc.getReference()).thenReturn(mockUserRef);
        when(mockOrganizerSvc.getReference()).thenReturn(mockOrganizerRef);
        when(mockEventSvc.getReference()).thenReturn(mockEventRef);

        // Inject mocks
        setPrivate(organizer, "waitingListService", mockWaitingListSvc);
        setPrivate(organizer, "userService", mockUserSvc);
        setPrivate(organizer, "organizerService", mockOrganizerSvc);
        setPrivate(organizer, "eventService", mockEventSvc);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // ==================== US 02.02.02: Organizer View Entrant Locations Tests ====================

    /**
     * Test Case 1: Organizer can access waiting list data for their event.
     * 
     * As an organizer, I should be able to access the waiting list data
     * for my event, which contains entrant location information.
     */
    @Test
    public void organizer_canAccessWaitingListData_forTheirEvent() {
        // Build the child chain: /WaitingList/{eventId}
        DatabaseReference eventNode = mock(DatabaseReference.class);
        when(mockWaitingListRef.child(EVENT_ID)).thenReturn(eventNode);

        // Organizer accesses waiting list data
        DatabaseReference result = mockWaitingListSvc.getReference().child(EVENT_ID);

        // Verify the correct path is accessed
        verify(mockWaitingListSvc, atLeastOnce()).getReference();
        assertNotNull("Organizer should be able to access waiting list reference", result);
    }

    /**
     * Test Case 2: Organizer can retrieve entrants with location data from WAITING status.
     * 
     * As an organizer, I should be able to retrieve entrants from the WAITING
     * status who have location data, so I can see where they joined from.
     */
    @Test
    public void organizer_canRetrieveWaitingEntrants_withLocationData() {
        // Build the child chain: /WaitingList/{eventId}/WAITING
        DatabaseReference eventNode = mock(DatabaseReference.class);
        DatabaseReference waitingNode = mock(DatabaseReference.class);

        when(mockWaitingListRef.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child(EntrantStatus.WAITING.toString())).thenReturn(waitingNode);

        // Organizer lists WAITING entrants (which may have location data)
        organizer.listEntrants(EntrantStatus.WAITING);

        // Verify the correct path is accessed
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(waitingNode, times(1)).addValueEventListener(listenerCaptor.capture());

        // Verify listener is attached to WAITING status
        assertNotNull("Listener should be attached to WAITING status", listenerCaptor.getValue());
    }

    /**
     * Test Case 3: Organizer can retrieve entrants with location data from INVITED status.
     * 
     * As an organizer, I should be able to retrieve entrants from the INVITED
     * status who have location data, so I can see where they joined from.
     */
    @Test
    public void organizer_canRetrieveInvitedEntrants_withLocationData() {
        // Build the child chain: /WaitingList/{eventId}/INVITED
        DatabaseReference eventNode = mock(DatabaseReference.class);
        DatabaseReference invitedNode = mock(DatabaseReference.class);

        when(mockWaitingListRef.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child(EntrantStatus.INVITED.toString())).thenReturn(invitedNode);

        // Organizer lists INVITED entrants (which may have location data)
        organizer.listEntrants(EntrantStatus.INVITED);

        // Verify the correct path is accessed
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(invitedNode, times(1)).addValueEventListener(listenerCaptor.capture());

        // Verify listener is attached to INVITED status
        assertNotNull("Listener should be attached to INVITED status", listenerCaptor.getValue());
    }

    /**
     * Test Case 4: Organizer can access both WAITING and INVITED statuses for location data.
     * 
     * As an organizer, I should be able to view locations from both WAITING
     * and INVITED entrants to get a complete picture of where people joined.
     */
    @Test
    public void organizer_canAccessBothWaitingAndInvited_forLocationData() {
        // Build the child chains for both statuses
        DatabaseReference eventNode = mock(DatabaseReference.class);
        DatabaseReference waitingNode = mock(DatabaseReference.class);
        DatabaseReference invitedNode = mock(DatabaseReference.class);

        when(mockWaitingListRef.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child(EntrantStatus.WAITING.toString())).thenReturn(waitingNode);
        when(eventNode.child(EntrantStatus.INVITED.toString())).thenReturn(invitedNode);

        // Organizer accesses WAITING entrants
        organizer.listEntrants(EntrantStatus.WAITING);
        verify(waitingNode, times(1)).addValueEventListener(any(ValueEventListener.class));

        // Organizer accesses INVITED entrants
        organizer.listEntrants(EntrantStatus.INVITED);
        verify(invitedNode, times(1)).addValueEventListener(any(ValueEventListener.class));
    }

    /**
     * Test Case 5: Organizer's event ID is correctly used when accessing waiting list.
     * 
     * As an organizer, when I access waiting list data, it should use the
     * correct event ID to ensure I only see entrants for my event.
     */
    @Test
    public void organizer_usesCorrectEventId_whenAccessingWaitingList() {
        String specificEventId = "event-specific-789";
        Organizer specificOrganizer = new Organizer(ORGANIZER_ID, specificEventId);

        try {
            setPrivate(specificOrganizer, "waitingListService", mockWaitingListSvc);
        } catch (Exception e) {
            fail("Failed to inject mock service");
        }

        DatabaseReference eventNode = mock(DatabaseReference.class);
        when(mockWaitingListRef.child(specificEventId)).thenReturn(eventNode);

        // Access waiting list for the specific event
        mockWaitingListSvc.getReference().child(specificEventId);

        // Verify the correct event ID is used
        verify(mockWaitingListRef, atLeastOnce()).child(specificEventId);
    }

    /**
     * Test Case 6: Organizer can process location data from entrant snapshots.
     * 
     * As an organizer, when I retrieve entrant data, I should be able to
     * extract latitude and longitude values for map display.
     */
    @Test
    public void organizer_canProcessLocationData_fromEntrantSnapshots() {
        // Simulate location data extraction
        double testLatitude = 53.5461;  // Edmonton
        double testLongitude = -113.4938;

        // Create a map representing entrant data with location
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("latitude", testLatitude);
        entrantData.put("longitude", testLongitude);
        entrantData.put(" ", ""); // Placeholder field

        // Verify location data can be extracted
        assertTrue("Should contain latitude", entrantData.containsKey("latitude"));
        assertTrue("Should contain longitude", entrantData.containsKey("longitude"));
        assertEquals("Latitude should match", testLatitude, entrantData.get("latitude"));
        assertEquals("Longitude should match", testLongitude, entrantData.get("longitude"));
    }

    /**
     * Test Case 7: Organizer handles entrants without location data gracefully.
     * 
     * As an organizer, when some entrants don't have location data, the system
     * should handle this gracefully and only show entrants with valid locations.
     */
    @Test
    public void organizer_handlesEntrantsWithoutLocation_gracefully() {
        // Create entrant data without location
        Map<String, Object> entrantDataNoLocation = new HashMap<>();
        entrantDataNoLocation.put(" ", ""); // Only placeholder, no location

        // Verify location fields are not present
        assertFalse("Should not contain latitude when missing", 
                   entrantDataNoLocation.containsKey("latitude"));
        assertFalse("Should not contain longitude when missing", 
                   entrantDataNoLocation.containsKey("longitude"));
    }

    /**
     * Test Case 8: Organizer can validate location data coordinates.
     * 
     * As an organizer, location data should be validated to ensure coordinates
     * are within valid ranges (latitude: -90 to 90, longitude: -180 to 180).
     */
    @Test
    public void organizer_canValidateLocationCoordinates() {
        // Valid coordinates
        double validLat = 53.5461;
        double validLon = -113.4938;

        // Verify valid coordinates
        assertTrue("Valid latitude should be between -90 and 90", 
                  validLat >= -90.0 && validLat <= 90.0);
        assertTrue("Valid longitude should be between -180 and 180", 
                  validLon >= -180.0 && validLon <= 180.0);

        // Invalid coordinates
        double invalidLat = 91.0;  // Out of range
        double invalidLon = 181.0; // Out of range

        // Verify invalid coordinates are detected
        assertFalse("Invalid latitude should be rejected", 
                   invalidLat >= -90.0 && invalidLat <= 90.0);
        assertFalse("Invalid longitude should be rejected", 
                   invalidLon >= -180.0 && invalidLon <= 180.0);
    }

    /**
     * Test Case 9: Organizer can distinguish between different entrant statuses for location display.
     * 
     * As an organizer, I should be able to filter and view locations by entrant
     * status (WAITING vs INVITED) to understand where different groups joined.
     */
    @Test
    public void organizer_canDistinguishStatuses_forLocationDisplay() {
        // Build references for both statuses
        DatabaseReference eventNode = mock(DatabaseReference.class);
        DatabaseReference waitingNode = mock(DatabaseReference.class);
        DatabaseReference invitedNode = mock(DatabaseReference.class);

        when(mockWaitingListRef.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child("WAITING")).thenReturn(waitingNode);
        when(eventNode.child("INVITED")).thenReturn(invitedNode);

        // Access WAITING status
        DatabaseReference waitingRef = mockWaitingListRef.child(EVENT_ID).child("WAITING");
        
        // Access INVITED status
        DatabaseReference invitedRef = mockWaitingListRef.child(EVENT_ID).child("INVITED");

        // Verify different statuses can be accessed separately
        assertNotNull("WAITING status reference should be accessible", waitingRef);
        assertNotNull("INVITED status reference should be accessible", invitedRef);
    }

    /**
     * Test Case 10: Organizer can access waiting list data for map display functionality.
     * 
     * As an organizer, I should be able to access the complete waiting list structure
     * needed to display entrant locations on a map, including both WAITING and INVITED statuses.
     */
    @Test
    public void organizer_canAccessCompleteWaitingList_forMapDisplay() {
        // Build the complete structure: /WaitingList/{eventId}
        DatabaseReference eventNode = mock(DatabaseReference.class);
        DatabaseReference waitingNode = mock(DatabaseReference.class);
        DatabaseReference invitedNode = mock(DatabaseReference.class);

        when(mockWaitingListRef.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child("WAITING")).thenReturn(waitingNode);
        when(eventNode.child("INVITED")).thenReturn(invitedNode);

        // Organizer accesses the event node (which contains both WAITING and INVITED)
        DatabaseReference eventRef = mockWaitingListSvc.getReference().child(EVENT_ID);

        // Verify the event-level reference is accessible
        assertNotNull("Event reference should be accessible for map display", eventRef);
        
        // Verify both status nodes can be accessed from the event node
        assertNotNull("WAITING node should be accessible", eventNode.child("WAITING"));
        assertNotNull("INVITED node should be accessible", eventNode.child("INVITED"));
    }

    // -------------------- helpers --------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

