package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Notification;
import com.example.chicksevent.misc.Organizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for {@link ../Organizer}.
 *
 * - Statically mocks FirebaseDatabase.getInstance(...) to avoid FirebaseApp init.
 * - Injects mocked FirebaseService for WaitingList/Event/Organizer services.
 * - Uses mockConstruction(Notification) to verify notifications are created & sent.
 */
public class OrganizerTest {

    private static final String ORG_ID = "org-123";
    private static final String EVENT_ID = "evt-999";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    private FirebaseService waitingListSvc;
    private FirebaseService organizerSvc;
    private FirebaseService eventSvc;

    private DatabaseReference waitingRootRef;
    private DatabaseReference eventNodeRef;
    private DatabaseReference statusNodeRef;

    private Organizer organizer;

    @Before
    public void setUp() throws Exception {
        // Block any real Firebase initialization
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);
        firebaseDbStatic.when(FirebaseDatabase::getInstance).thenReturn(mockDb);
        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(mockDb);

        // Mock the three services Organizer creates
        waitingListSvc = mock(FirebaseService.class);
        organizerSvc   = mock(FirebaseService.class);
        eventSvc       = mock(FirebaseService.class);

        // Basic DB refs used by waitingList path
        waitingRootRef = mock(DatabaseReference.class);
        eventNodeRef   = mock(DatabaseReference.class);
        statusNodeRef  = mock(DatabaseReference.class);

        when(waitingListSvc.getReference()).thenReturn(waitingRootRef);
        when(waitingRootRef.child(EVENT_ID)).thenReturn(eventNodeRef);

        organizer = new Organizer(ORG_ID, EVENT_ID);

        // Inject mocked services so no real DB is touched
        inject(organizer, "waitingListService", waitingListSvc);
        inject(organizer, "organizerService",   organizerSvc);
        inject(organizer, "eventService",       eventSvc);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // ---- helper to set private fields ----
    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ----------------------------------------------------------------------
    // listEntrants()
    // ----------------------------------------------------------------------

    @Test
    public void listEntrants_noArg_attachesListenerToWAITING() {
        when(eventNodeRef.child(EntrantStatus.WAITING.toString())).thenReturn(statusNodeRef);

        organizer.listEntrants(); // defaults to WAITING

        verify(waitingRootRef).child(EVENT_ID);
        verify(eventNodeRef).child("WAITING");
        verify(statusNodeRef).addValueEventListener(any(ValueEventListener.class));
    }

    @Test
    public void listEntrants_withStatus_attachesListenerToGivenStatus() {
        when(eventNodeRef.child(EntrantStatus.INVITED.toString())).thenReturn(statusNodeRef);

        organizer.listEntrants(EntrantStatus.INVITED);

        verify(waitingRootRef).child(EVENT_ID);
        verify(eventNodeRef).child("INVITED");
        verify(statusNodeRef).addValueEventListener(any(ValueEventListener.class));
    }

    // ----------------------------------------------------------------------
    // sendWaitingListNotification(...)
    // ----------------------------------------------------------------------

    @Test
    public void sendWaitingListNotification_WAITING_createsAndSendsForEachChild() {
        when(eventNodeRef.child("WAITING")).thenReturn(statusNodeRef);

        // Capture the listener using an AtomicReference (captor with when() is awkward)
        AtomicReference<ValueEventListener> listenerRef = new AtomicReference<>();
        when(statusNodeRef.addValueEventListener(any(ValueEventListener.class)))
                .thenAnswer(inv -> {
                    ValueEventListener l = inv.getArgument(0);
                    listenerRef.set(l);
                    return l; // IMPORTANT: method returns the listener
                });

        DataSnapshot root = mock(DataSnapshot.class);
        DataSnapshot ch1  = mock(DataSnapshot.class);
        DataSnapshot ch2  = mock(DataSnapshot.class);
        when(root.getChildren()).thenReturn(Arrays.asList(ch1, ch2));
        when(ch1.getKey()).thenReturn("userA");
        when(ch2.getKey()).thenReturn("userB");

        try (MockedConstruction<Notification> notifConst = mockConstruction(
                Notification.class,
                (mock, ctx) -> doNothing().when(mock).createNotification()
        )) {
            organizer.sendWaitingListNotification(EntrantStatus.WAITING, "hello all");

            // Trigger the callback
            assertNotNull("ValueEventListener should be attached", listenerRef.get());
            listenerRef.get().onDataChange(root);

            // One Notification per child
            assertEquals(2, notifConst.constructed().size());
            verify(notifConst.constructed().get(0), times(1)).createNotification();
            verify(notifConst.constructed().get(1), times(1)).createNotification();
        }
    }

    @Test
    public void sendWaitingListNotification_INVITED_mapsStatusAndSends() {
        when(eventNodeRef.child("INVITED")).thenReturn(statusNodeRef);

        AtomicReference<ValueEventListener> listenerRef = new AtomicReference<>();
        when(statusNodeRef.addValueEventListener(any(ValueEventListener.class)))
                .thenAnswer(inv -> {
                    ValueEventListener l = inv.getArgument(0);
                    listenerRef.set(l);
                    return l;
                });

        DataSnapshot root = mock(DataSnapshot.class);
        DataSnapshot ch1  = mock(DataSnapshot.class);
        when(root.getChildren()).thenReturn(Collections.singletonList(ch1));
        when(ch1.getKey()).thenReturn("userX");

        try (MockedConstruction<Notification> notifConst = mockConstruction(
                Notification.class,
                (mock, ctx) -> doNothing().when(mock).createNotification()
        )) {
            organizer.sendSelectedNotification("only invited");

            assertNotNull(listenerRef.get());
            listenerRef.get().onDataChange(root);

            assertEquals(1, notifConst.constructed().size());
            verify(notifConst.constructed().get(0), times(1)).createNotification();
        }
    }

    @Test
    public void sendWaitingListNotification_noChildren_constructsZero() {
        when(eventNodeRef.child("WAITING")).thenReturn(statusNodeRef);

        AtomicReference<ValueEventListener> listenerRef = new AtomicReference<>();
        when(statusNodeRef.addValueEventListener(any(ValueEventListener.class)))
                .thenAnswer(inv -> {
                    ValueEventListener l = inv.getArgument(0);
                    listenerRef.set(l);
                    return l;
                });

        DataSnapshot root = mock(DataSnapshot.class);
        when(root.getChildren()).thenReturn(Collections.emptyList());

        try (MockedConstruction<Notification> notifConst = mockConstruction(
                Notification.class,
                (mock, ctx) -> {}
        )) {
            organizer.sendWaitingListNotification("empty list");

            assertNotNull(listenerRef.get());
            listenerRef.get().onDataChange(root);

            assertEquals(0, notifConst.constructed().size());
        }
    }

    // ----------------------------------------------------------------------
    // Convenience overload sendWaitingListNotification(String)
    // ----------------------------------------------------------------------

    @Test
    public void sendWaitingListNotification_overload_defaultsToWAITING() {
        when(eventNodeRef.child("WAITING")).thenReturn(statusNodeRef);

        organizer.sendWaitingListNotification("hi");
        verify(eventNodeRef).child("WAITING");
    }

    // ----------------------------------------------------------------------
    // Simple getters / role flags!
    // ----------------------------------------------------------------------

    @Test
    public void roles_and_getters() {
        assertEquals(ORG_ID, organizer.getOrganizerId());
        assertTrue(organizer.isOrganizer());
//        assertFalse(organizer.isAdmin());
    }
}
