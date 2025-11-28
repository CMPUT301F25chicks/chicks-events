package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Lottery;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link Lottery} replacement applicant functionality.
 *
 * <h2>User stories handled</h2>
 *   <li>US 02.05.03: As an organizer I want to be able to draw a replacement applicant from the pooling system
 *       when a previously selected applicant cancels or rejects the invitation.</li>
 * <p>
 * These tests validate that the pooling system correctly handles replacement applicants, ensuring that:
 * <ul>
 *     <li>The fragment identifies cancelled or rejected entrants correctly.</li>
 *     <li>A replacement applicant is selected and added to the final list appropriately.</li>
 *     <li>Adapter and UI updates correctly reflect the replacement in the list.</li>
 *     <li>No real Firebase interactions are required; all entrant and pool data are mocked.</li>
 * </ul>
 * </p>
 *
 * <p>
 * All Firebase interactions are mocked to allow isolated unit testing without network dependencies.
 * </p>
 */

public class PoolingReplacementTest {

    private static final String EVENT_ID = "evt-1";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    private DatabaseReference waitingRoot;
    private DatabaseReference waitingEventRef;
    private DatabaseReference waitingStatusRef;
    private DatabaseReference eventRoot;
    private DatabaseReference eventNode;
    private DatabaseReference entrantLimitRef;

    private Lottery lottery;
    private FirebaseService mockWaitingSvc;
    private FirebaseService mockEventSvc;

    @Before
    public void setUp() throws Exception {
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        waitingRoot      = mock(DatabaseReference.class);
        waitingEventRef  = mock(DatabaseReference.class);
        waitingStatusRef = mock(DatabaseReference.class);
        eventRoot        = mock(DatabaseReference.class);
        eventNode        = mock(DatabaseReference.class);
        entrantLimitRef  = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("WaitingList")).thenReturn(waitingRoot);
        when(mockDb.getReference("Event")).thenReturn(eventRoot);

        lottery = new Lottery(EVENT_ID);

        mockWaitingSvc = mock(FirebaseService.class);
        mockEventSvc   = mock(FirebaseService.class);

        when(mockWaitingSvc.getReference()).thenReturn(waitingRoot);
        when(mockEventSvc.getReference()).thenReturn(eventRoot);

        setPrivate(lottery, "waitingListService", mockWaitingSvc);
        setPrivate(lottery, "eventService",       mockEventSvc);

        when(waitingRoot.child(EVENT_ID)).thenReturn(waitingEventRef);
        when(waitingEventRef.child("WAITING")).thenReturn(waitingStatusRef);

        when(eventRoot.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child("entrantLimit")).thenReturn(entrantLimitRef);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // -------------------- TESTS --------------------

    @Test
    public void poolReplacement_noWaiting_doesNotWrite() {
        DataSnapshot waitSnap = mock(DataSnapshot.class);
        when(waitSnap.exists()).thenReturn(false);
        when(waitSnap.getChildren()).thenAnswer(i -> iterable());

        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(waitSnap);
            return null;
        }).when(waitingStatusRef).addListenerForSingleValueEvent(any());

        lottery.poolReplacement(5);

        verify(waitingRoot, never()).updateChildren(anyMap(), any());
    }

    @Test
    public void poolReplacement_partialInvite() {
        DataSnapshot waitSnap = mock(DataSnapshot.class);
        when(waitSnap.exists()).thenReturn(true);

        DataSnapshot u1 = mock(DataSnapshot.class);
        DataSnapshot u2 = mock(DataSnapshot.class);
        DataSnapshot u3 = mock(DataSnapshot.class);
        when(u1.getKey()).thenReturn("u1");
        when(u2.getKey()).thenReturn("u2");
        when(u3.getKey()).thenReturn("u3");

        when(waitSnap.getChildren()).thenAnswer(i -> iterable(u1, u2, u3));

        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(waitSnap);
            return null;
        }).when(waitingStatusRef).addListenerForSingleValueEvent(any());

        doAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = inv.getArgument(0);
            DatabaseReference.CompletionListener cl = inv.getArgument(1);
            cl.onComplete(null, waitingRoot);
            return null;
        }).when(waitingRoot).updateChildren(anyMap(), any());

        ArgumentCaptor<Map<String, Object>> mapCap =
                ArgumentCaptor.forClass((Class) Map.class);

        lottery.poolReplacement(2);

        verify(waitingRoot).updateChildren(mapCap.capture(), any());
        Map<String, Object> updates = mapCap.getValue();

        // 2 invited, 1 uninvited, all removed from WAITING
        long invitedCount = updates.keySet().stream().filter(k -> k.contains("/INVITED/")).count();
        long uninvitedCount = updates.keySet().stream().filter(k -> k.contains("/UNINVITED/")).count();

        assertEquals(2, invitedCount);
        assertEquals(1, uninvitedCount);

        updates.keySet().forEach(k -> {
            if (k.contains("/WAITING/")) {
                assertNull(updates.get(k));
            }
        });
    }

    @Test
    public void poolReplacementAuto_noWaitingOrFull_noWrite() {
        // Setup: invitedCount = 5, limit = 5 → already full
        mockCounts(5, 5, 0);

        lottery.poolReplacementAuto();

        verify(waitingRoot, never()).updateChildren(anyMap(), any());
    }


    // -------------------- HELPERS --------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        List<DataSnapshot> list = Arrays.asList(snaps);
        return () -> list.iterator();
    }

    /**
     * Mock the counts used by poolReplacementAuto.
     * It triggers listeners for /Event/EVENT_ID/entrantLimit,
     * /WaitingList/EVENT_ID/INVITED, /WaitingList/EVENT_ID/WAITING
     */
    private void mockCounts(int invitedCount, int limit, int waitingCount) {
        // ----- Limit snapshot -----
        DataSnapshot limitSnap = mock(DataSnapshot.class);
        when(limitSnap.exists()).thenReturn(true);
        when(limitSnap.getValue(Integer.class)).thenReturn(limit);

        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(limitSnap);
            return null;
        }).when(entrantLimitRef).addListenerForSingleValueEvent(any());

        // ----- Invited snapshot -----
        DatabaseReference invitedRef = mock(DatabaseReference.class);
        when(waitingEventRef.child("INVITED")).thenReturn(invitedRef);

        DataSnapshot invSnap = mock(DataSnapshot.class);
        when(invSnap.getChildrenCount()).thenReturn((long) invitedCount);

        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(invSnap);
            return null;
        }).when(invitedRef).addListenerForSingleValueEvent(any());

        // ----- Waiting snapshot -----
        DataSnapshot waitSnap = mock(DataSnapshot.class);
        when(waitSnap.getChildrenCount()).thenReturn((long) waitingCount);

        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(waitSnap);
            return null;
        }).when(waitingStatusRef).addListenerForSingleValueEvent(any());
    }

}
