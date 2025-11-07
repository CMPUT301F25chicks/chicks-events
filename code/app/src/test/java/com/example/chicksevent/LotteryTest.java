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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link Lottery}.
 *
 * <p>
 * These tests provide a fully synchronous verification of the {@link Lottery#runLottery()} logic,
 * ensuring that Firebase interactions occur as expected without relying on real network operations
 * or randomised selections. Firebase Realtime Database calls are mocked using Mockito, and
 * {@link FirebaseDatabase#getInstance(String)} is statically replaced to avoid SDK initialisation.
 * </p>
 *
 * <h2>Key Behaviours Verified</h2>
 * <ul>
 *   <li>Correct listener attachment and event handling on {@code entrantLimit} and {@code WAITING} nodes</li>
 *   <li>No writes occur when no waiting entrants exist</li>
 *   <li>Atomic update payload correctly includes {@code INVITED} entries and deletions from {@code WAITING}</li>
 *   <li>Ensures that no {@code UNINVITED} nodes are created when all entrants fit within the limit</li>
 * </ul>
 *
 * <h2>Testing Approach</h2>
 * <ul>
 *   <li>Mocks Firebase structure under {@code /Event} and {@code /WaitingList}</li>
 *   <li>Injects {@link FirebaseService} instances using reflection to isolate test behaviour</li>
 *   <li>Triggers {@link ValueEventListener#onDataChange(DataSnapshot)} manually to simulate database reads</li>
 *   <li>Uses {@link ArgumentCaptor} to validate the correctness of atomic update payloads</li>
 * </ul>
 *
 * <p>
 * These tests run purely on the JVM and are deterministic — no async tasks, randomness,
 * or external Firebase interactions are required.
 * </p>
 *
 * @author Hanh
 * @author Jinn Kasai
 */
public class LotteryTest {

    private static final String EVENT_ID = "evt-1";

    // Static mock for FirebaseDatabase.getInstance(String)
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    // RTDB references used by FirebaseService("WaitingList") and ("Event")
    private DatabaseReference waitingRoot;     // /WaitingList
    private DatabaseReference eventRoot;       // /Event

    // Chained refs for paths
    private DatabaseReference eventNode;       // /Event/EVENT_ID
    private DatabaseReference entrantLimitRef; // /Event/EVENT_ID/entrantLimit

    private DatabaseReference waitingEventRef; // /WaitingList/EVENT_ID
    private DatabaseReference waitingStatusRef;// /WaitingList/EVENT_ID/WAITING

    // Under test
    private Lottery lottery;

    // Service mocks we inject
    private FirebaseService mockWaitingSvc;
    private FirebaseService mockEventSvc;

    @Before
    public void setUp() throws Exception {
        // Prevent real Firebase init
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        waitingRoot      = mock(DatabaseReference.class);
        eventRoot        = mock(DatabaseReference.class);
        eventNode        = mock(DatabaseReference.class);
        entrantLimitRef  = mock(DatabaseReference.class);
        waitingEventRef  = mock(DatabaseReference.class);
        waitingStatusRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("WaitingList")).thenReturn(waitingRoot);
        when(mockDb.getReference("Event")).thenReturn(eventRoot);

        // Safe construct (no real Firebase)
        lottery = new Lottery(EVENT_ID);

        // Prepare service mocks & inject
        mockWaitingSvc = mock(FirebaseService.class);
        mockEventSvc   = mock(FirebaseService.class);

        when(mockWaitingSvc.getReference()).thenReturn(waitingRoot);
        when(mockEventSvc.getReference()).thenReturn(eventRoot);

        setPrivate(lottery, "waitingListService", mockWaitingSvc);
        setPrivate(lottery, "eventService",       mockEventSvc);

        // Wire child chains the production code uses
        when(eventRoot.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child("entrantLimit")).thenReturn(entrantLimitRef);

        when(waitingRoot.child(EVENT_ID)).thenReturn(waitingEventRef);
        when(waitingEventRef.child("WAITING")).thenReturn(waitingStatusRef);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // -------------------- Tests --------------------

    @Test
    public void runLottery_noWaiting_doesNotWrite() {
        // entrantLimit exists and is 5
        DataSnapshot limitSnap = mock(DataSnapshot.class);
        when(limitSnap.exists()).thenReturn(true);
        when(limitSnap.getValue(Integer.class)).thenReturn(5);

        // WAITING node is absent/empty
        DataSnapshot waitSnap = mock(DataSnapshot.class);
        when(waitSnap.exists()).thenReturn(false);
        when(waitSnap.getChildren()).thenAnswer(i -> iterable());

        // Fire both listeners immediately
        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(limitSnap);
            return null;
        }).when(entrantLimitRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(waitSnap);
            return null;
        }).when(waitingStatusRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Run
        lottery.runLottery();

        // Because updates would be empty, no atomic update should be attempted
        verify(waitingRoot, never()).updateChildren(anyMap(), any());
    }

    @Test
    public void runLottery_allInvited_whenWaitingLessOrEqualLimit() {
        // entrantLimit exists and is 3
        DataSnapshot limitSnap = mock(DataSnapshot.class);
        when(limitSnap.exists()).thenReturn(true);
        when(limitSnap.getValue(Integer.class)).thenReturn(3);

        // WAITING has two users: u1, u2  (<= limit)
        DataSnapshot waitSnap = mock(DataSnapshot.class);
        when(waitSnap.exists()).thenReturn(true);

        DataSnapshot u1 = mock(DataSnapshot.class);
        DataSnapshot u2 = mock(DataSnapshot.class);
        when(u1.getKey()).thenReturn("u1");
        when(u2.getKey()).thenReturn("u2");
        when(waitSnap.getChildren()).thenAnswer(i -> iterable(u1, u2));

        // Fire both listeners immediately
        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(limitSnap);
            return null;
        }).when(entrantLimitRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        doAnswer(inv -> {
            ValueEventListener l = inv.getArgument(0);
            l.onDataChange(waitSnap);
            return null;
        }).when(waitingStatusRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Make updateChildren invoke completion (as success)
        doAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = inv.getArgument(0);
            DatabaseReference.CompletionListener cl = inv.getArgument(1);
            cl.onComplete(null, waitingRoot);
            return null;
        }).when(waitingRoot).updateChildren(anyMap(), any(DatabaseReference.CompletionListener.class));

        // Capture payload
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> mapCap =
                ArgumentCaptor.forClass((Class) Map.class);

        // Run
        lottery.runLottery();

        verify(waitingRoot, times(1))
                .updateChildren(mapCap.capture(), any(DatabaseReference.CompletionListener.class));

        Map<String, Object> updates = mapCap.getValue();

        // Expect invited entries and deletions from WAITING; no UNINVITED keys
        assertEquals(Boolean.TRUE, updates.get(EVENT_ID + "/INVITED/u1"));
        assertEquals(Boolean.TRUE, updates.get(EVENT_ID + "/INVITED/u2"));
        assertTrue(updates.containsKey(EVENT_ID + "/WAITING/u1"));
        assertTrue(updates.containsKey(EVENT_ID + "/WAITING/u2"));
        assertNull(updates.get(EVENT_ID + "/WAITING/u1")); // deletion is null
        assertNull(updates.get(EVENT_ID + "/WAITING/u2"));

        // No UNINVITED keys should be present
        assertFalse(updates.containsKey(EVENT_ID + "/UNINVITED/u1"));
        assertFalse(updates.containsKey(EVENT_ID + "/UNINVITED/u2"));
    }

    // -------------------- helpers --------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        List<DataSnapshot> list = Arrays.asList(snaps);
        return new Iterable<DataSnapshot>() {
            @Override public Iterator<DataSnapshot> iterator() {
                return list.iterator();
            }
        };
    }
}
