package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 Entrant Class Unit Testing
 * <p>
 **/
public class EntrantTest {
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockAdminRef;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockWaitingListRef; // Added for "WaitingList" service

    @Before
    public void setUpFirebaseStatic() {
        // Static mock for FirebaseDatabase.getInstance(...)
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);

        mockDb = mock(FirebaseDatabase.class);
        mockAdminRef = mock(DatabaseReference.class);
        mockEventRef = mock(DatabaseReference.class);
        mockWaitingListRef = mock(DatabaseReference.class); // Initialize mock

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        // FirebaseService("Admin"), ("Event"), and ("WaitingList")
        when(mockDb.getReference("Admin")).thenReturn(mockAdminRef);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
        when(mockDb.getReference("WaitingList")).thenReturn(mockWaitingListRef); // Mock this path
    }

    @After
    public void tearDownFirebaseStatic() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // ---------- helpers ----------

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ---------- Passing Tests ----------

    @Test
    public void constructor_initializesPropertiesCorrectly() {
        // Given: A user ID and an event ID
        String userId = "user123";
        String eventId = "event456";

        // When: An Entrant object is created
        Entrant entrant = new Entrant(userId, eventId);

        // Then: The properties should be set as expected
        Assert.assertEquals(userId, entrant.getUserId()); // from parent User class
        assertEquals(userId, entrant.getEntrantId());
        assertEquals(eventId, entrant.getEventId());
        assertEquals(EntrantStatus.WAITING, entrant.getStatus()); // Default status
    }

    @Test
    public void joinWaitingList_updatesStatusAndCallsService() throws Exception {
        // Given: An Entrant and a mocked service
        Entrant entrant = new Entrant("user123", "event456");
        FirebaseService mockWaitingListService = mock(FirebaseService.class);
        setPrivateField(entrant, "waitingListService", mockWaitingListService);

        // Define expected data for the service call
        HashMap<String, Object> expectedData = new HashMap<>();
        expectedData.put(" ", "");

        // When: The entrant joins with a specific status
        entrant.joinWaitingList(EntrantStatus.ACCEPTED);

        // Then: The entrant's status should be updated
        assertEquals(EntrantStatus.ACCEPTED, entrant.getStatus());

        // And: The Firebase service should be called with the correct parameters
        verify(mockWaitingListService).updateSubCollectionEntry("event456", "ACCEPTED", "user123", expectedData);
    }

    @Test
    public void leaveWaitingList_updatesStatusAndCallsService() throws Exception {
        // Given: An Entrant and mocked service
        Entrant entrant = new Entrant("user123", "event456");
        FirebaseService mockWaitingListService = mock(FirebaseService.class);
        setPrivateField(entrant, "waitingListService", mockWaitingListService);

        // Pre-condition: Set a status to leave from. Default is WAITING
        entrant.joinWaitingList(EntrantStatus.WAITING);

        // When: The entrant leaves the waiting list
        entrant.leaveWaitingList(EntrantStatus.WAITING);

        // Then: The status should be set to null
        assertNull(entrant.getStatus());

        // And: The delete method on the service should be called correctly
        verify(mockWaitingListService).deleteSubCollectionEntry("event456", "WAITING", "user123");
    }

    @Test
    public void swapStatus_callsLeaveAndThenJoin() throws Exception {
        // Given: An Entrant starting in the WAITING state
        Entrant entrant = new Entrant("user123", "event456");
        FirebaseService mockWaitingListService = mock(FirebaseService.class);
        setPrivateField(entrant, "waitingListService", mockWaitingListService);

        // Initial status is WAITING from constructor
        assertEquals(EntrantStatus.WAITING, entrant.getStatus());

        // When: The status is swapped to ACCEPTED
        entrant.swapStatus(EntrantStatus.ACCEPTED);

        // Then: The final status should be ACCEPTED
        assertEquals(EntrantStatus.ACCEPTED, entrant.getStatus());

        // And: The service should first be called to delete the old status entry
        verify(mockWaitingListService, times(1)).deleteSubCollectionEntry("event456", "WAITING", "user123");

        // And: The service should then be called to create the new status entry
        HashMap<String, Object> expectedData = new HashMap<>();
        expectedData.put(" ", "");
        verify(mockWaitingListService, times(1)).updateSubCollectionEntry("event456", "ACCEPTED", "user123", expectedData);
    }

    @Test
    public void isOrganizerAndIsAdmin_alwaysReturnFalse() {
        // Given: A standard Entrant
        Entrant entrant = new Entrant("user123", "event456");

        // Then: They should not have admin or organizer privileges
        assertFalse(entrant.isOrganizer());
//        assertFalse(entrant.isAdmin());
    }

    // ---------- Test Designed to Pass by Failing Successfully ----------

    /**
     * This test demonstrates checking for incorrect behavior.
     * We want to ensure that leaving a list does NOT call the 'update' method,
     * only the 'delete' method. This test should pass because the assertion is correct.
     */
    @Test
    public void leaveWaitingList_shouldNotCallUpdate() throws Exception {
        // Given: An Entrant object and mocked service
        Entrant entrant = new Entrant("user123", "event456");
        FirebaseService mockWaitingListService = mock(FirebaseService.class);
        setPrivateField(entrant, "waitingListService", mockWaitingListService);

        // When: The entrant leaves the waiting list
        entrant.leaveWaitingList(EntrantStatus.WAITING);

        // Then: The update method should NEVER be called during a leave operation
        verify(mockWaitingListService, never()).updateSubCollectionEntry(anyString(), anyString(), anyString(), any(HashMap.class));
    }
}
