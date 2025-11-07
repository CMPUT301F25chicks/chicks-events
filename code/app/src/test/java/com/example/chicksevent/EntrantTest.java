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
import org.mockito.InOrder;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Robust unit tests for Entrant (no real Firebase, no Android main thread).
 * - Mocks android.util.Log statically to avoid "Method ... not mocked" crash.
 * - Injects mocked FirebaseService via reflection.
 */
public class EntrantTest {

    private static final String EVENT_ID   = "evt-123";
    private static final String ENTRANT_ID = "u-777";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private MockedStatic<Log> logStatic; // <-- mock android.util.Log

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

        entrant = new Entrant(ENTRANT_ID, EVENT_ID);

        mockWaitingSvc = mock(FirebaseService.class);
        mockEntrantSvc = mock(FirebaseService.class);
        mockEventSvc   = mock(FirebaseService.class);

        setPrivate(entrant, "waitingListService", mockWaitingSvc);
        setPrivate(entrant, "entrantService",     mockEntrantSvc);
        setPrivate(entrant, "eventService",       mockEventSvc);

        // Make void methods safe; explicit but optional
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

    // ---------------------- joinWaitingList ----------------------

    @Test
    public void joinWaitingList_default_callsUpdate_onWaitingPath_andSetsStatus() {
        entrant.joinWaitingList(); // default WAITING

        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), anyString(), eq(ENTRANT_ID), any(HashMap.class));

        assertEquals(EntrantStatus.WAITING, entrant.getStatus());
    }

    @Test
    public void joinWaitingList_specificStatus_invited_callsUpdate_andSetsStatus() {
        entrant.joinWaitingList(EntrantStatus.INVITED);

        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), anyString(), eq(ENTRANT_ID), any(HashMap.class));

        assertEquals(EntrantStatus.INVITED, entrant.getStatus());
    }

    // ---------------------- leaveWaitingList ----------------------

    @Test
    public void leaveWaitingList_default_waiting_callsDelete_andClearsStatus() {
        entrant.leaveWaitingList(); // default WAITING

        verify(mockWaitingSvc, times(1)).deleteSubCollectionEntry(
                eq(EVENT_ID), anyString(), eq(ENTRANT_ID));

        assertNull(entrant.getStatus());
    }

    @Test
    public void leaveWaitingList_specificStatus_invited_callsDelete_andClearsStatus() {
        entrant.joinWaitingList(EntrantStatus.INVITED);
        entrant.leaveWaitingList(EntrantStatus.INVITED);

        verify(mockWaitingSvc, times(1)).deleteSubCollectionEntry(
                eq(EVENT_ID), anyString(), eq(ENTRANT_ID));

        assertNull(entrant.getStatus());
    }

    // ---------------------- swapStatus ----------------------

    @Test
    public void swapStatus_fromWaitingToInvited_deletesOld_thenAddsNew_andUpdatesStatus() {
        entrant.swapStatus(EntrantStatus.INVITED);

        InOrder inOrder = inOrder(mockWaitingSvc);
        inOrder.verify(mockWaitingSvc).deleteSubCollectionEntry(eq(EVENT_ID), anyString(), eq(ENTRANT_ID));
        inOrder.verify(mockWaitingSvc).updateSubCollectionEntry(eq(EVENT_ID), anyString(), eq(ENTRANT_ID), any(HashMap.class));

        assertEquals(EntrantStatus.INVITED, entrant.getStatus());
    }

    // ---------------------- trivial getters / role ----------------------

    @Test
    public void getters_and_isOrganizer() {
        assertEquals(EVENT_ID, entrant.getEventId());
        assertEquals(ENTRANT_ID, entrant.getEntrantId());
        assertFalse(entrant.isOrganizer());
    }

    // ---------------------- helper ----------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
