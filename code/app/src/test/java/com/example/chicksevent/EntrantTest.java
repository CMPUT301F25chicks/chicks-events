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
    public void swapStatus_fromWaitingToInvited_deletesOld_thenAddsNew_andUpdatesStatus() throws Exception {
        // Mock the Firebase chain for async swapStatus
        com.google.firebase.database.DatabaseReference mockRef = mock(com.google.firebase.database.DatabaseReference.class);
        com.google.firebase.database.DatabaseReference mockEventRef = mock(com.google.firebase.database.DatabaseReference.class);
        com.google.firebase.database.DatabaseReference mockStatusRef = mock(com.google.firebase.database.DatabaseReference.class);
        com.google.firebase.database.DatabaseReference mockEntrantRef = mock(com.google.firebase.database.DatabaseReference.class);
        com.google.android.gms.tasks.Task<com.google.firebase.database.DataSnapshot> mockTask = mock(com.google.android.gms.tasks.Task.class);
        com.google.firebase.database.DataSnapshot mockSnapshot = mock(com.google.firebase.database.DataSnapshot.class);
        
        when(mockWaitingSvc.getReference()).thenReturn(mockRef);
        when(mockRef.child(EVENT_ID)).thenReturn(mockEventRef);
        when(mockEventRef.child("WAITING")).thenReturn(mockStatusRef);
        when(mockStatusRef.child(ENTRANT_ID)).thenReturn(mockEntrantRef);
        when(mockEntrantRef.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockSnapshot);
        when(mockSnapshot.child("latitude")).thenReturn(mock(com.google.firebase.database.DataSnapshot.class));
        when(mockSnapshot.child("longitude")).thenReturn(mock(com.google.firebase.database.DataSnapshot.class));
        
        // Make the task complete immediately so callback executes synchronously
        when(mockTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.database.DataSnapshot> listener = 
                invocation.getArgument(0);
            listener.onComplete(mockTask);
            return mockTask;
        });
        
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
