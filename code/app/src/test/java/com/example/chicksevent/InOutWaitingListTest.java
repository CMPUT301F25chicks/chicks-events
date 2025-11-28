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
 * Unit tests for US 01.05.02: As an entrant I want to be able to accept the invitation to
 * register/sign up when chosen to participate in an event.
 * US 01.05.03 As an entrant I want to be able to decline an invitation when chosen to participate
 * in an event.
 * US 01.01.02 As an entrant, I want to leave the waiting list for a specific event
 * US 01.05.01 As an entrant I want another chance to be chosen from the waiting list if a selected
 * user declines an invitation to sign up
 * <p>
 * These tests validate that:
 * <ul>
 *   <li>Entrants can leave the waiting list from event details</li>
 *   <li>Entrants can accept the invitation from event details</li>
 *   <li>Entrants can decline the invitation from event details</li>
 *   <li>Entrants can rejoin the waiting list from event details</li>
 * </ul>
 * </p>
 *
 * @author Dam Dung Nguyen Mong
 */

public class InOutWaitingListTest {

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

    /**
     * Test Case 1: Entrant status is set to null when leaving.
     *
     * As an entrant, when I leave the waiting list, my status should be
     * set to null.
     */
    @Test
    public void entrant_canLeaveWaitingList() {
        // First join the waiting list so status = WAITING
        entrant.joinWaitingList();

        // Act: leave waiting list
        entrant.leaveWaitingList();

        // Firebase deletion should be called ONCE for WAITING/{entrantId}
        verify(mockWaitingSvc, times(1)).deleteSubCollectionEntry(
                eq(EVENT_ID),
                eq("WAITING"),
                eq(ENTRANT_ID)
        );

        // Entrant status should now be null (your implementation sets it null)
        assertNull("Status should be null after leaving the waiting list",
                entrant.getStatus());
    }

    /**
     * Test Case 2: Entrant status is set to ACCEPTED when accepting.
     *
     * As an entrant, when I accept the invitation, my status should be
     * set to ACCEPTED.
     */
    @Test
    public void entrant_canAcceptWaitingList() {
        // Act
        entrant.acceptWaitingList();   // default ACCEPTED

        // 1. updateSubCollectionEntry should be called for ACCEPTED
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID),
                eq("ACCEPTED"),
                eq(ENTRANT_ID),
                any(HashMap.class)
        );

        // 2. delete from INVITED node
        verify(mockWaitingSvc, times(1)).deleteSubCollectionEntry(
                eq(EVENT_ID),
                eq("INVITED"),
                eq(ENTRANT_ID)
        );

        // 3. Entrant status is updated
        assertEquals(EntrantStatus.ACCEPTED, entrant.getStatus());
    }

    /**
     * Test Case 3: Entrant status is set to DECLINED when accepting.
     *
     * As an entrant, when I decline the invitation, my status should be
     * set to DECLINED.
     */
    @Test
    public void entrant_canDeclineWaitingList() {
        // Act
        entrant.declineWaitingList();   // default DECLINED

        // 1. updateSubCollectionEntry should be called for DECLINED
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID),
                eq("DECLINED"),
                eq(ENTRANT_ID),
                any(HashMap.class)
        );

        // 2. delete from INVITED node
        verify(mockWaitingSvc, times(1)).deleteSubCollectionEntry(
                eq(EVENT_ID),
                eq("INVITED"),
                eq(ENTRANT_ID)
        );

        // 3. Entrant status is updated
        assertEquals(EntrantStatus.DECLINED, entrant.getStatus());
    }

    /**
     * Test Case 4: Entrant status is set to WAITING when rejoining.
     *
     * As an entrant, when I rejoin the waiting list, my status should be
     * set to WAITING from UNINVITED.
     */
    @Test
    public void entrant_withUninvitedStatus_canJoinWaitingList() throws Exception {
        // Arrange: manually force entrant status to UNINVITED
        Field statusField = entrant.getClass().getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(entrant, EntrantStatus.UNINVITED);

        // Act
        entrant.joinWaitingList(); // default -> WAITING

        // Assert: Firebase write happened
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID),
                eq("WAITING"),
                eq(ENTRANT_ID),
                any(HashMap.class)
        );

        // Assert: Entrant status is now WAITING
        assertEquals(EntrantStatus.WAITING, entrant.getStatus());
    }


    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
