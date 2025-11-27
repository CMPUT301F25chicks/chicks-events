
package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.enums.NotificationType;
import com.example.chicksevent.misc.Notification;
import com.example.chicksevent.misc.Organizer;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * Unit tests for {@link Organizer} related to
 * user story US 02.07.02: "As an organizer I want to send notifications to all selected entrants."
 *
 * <p>
 * These tests are designed to run synchronously
 * By mocking Firebase Realtime Database
 * we ensure deterministic behaviour of
 * {@link Organizer#sendSelectedNotification(String)} and
 * {@link Organizer#sendWaitingListNotificationHelper(
 * com.example.chicksevent.enums.EntrantStatus, String)}
 * without awaiting asynchronous callbacks.
 * </p>
 *
 * <h2>Key Behaviours Verified</h2>
 * <ul>
 *   <li>{@code sendSelectedNotification(String)} delegates correctly to the INVITED bucket</li>
 *   <li>{@code sendCancelledNotification(String)} delegates correctly to the CANCELLED bucket</li>
 *   <li>{@code sendWaitingListNotificationHelper(...)} builds a {@link java.util.List}
 *       of {@link Notification} objects, one per entrant
 *       in the requested waiting-list bucket</li>
 *   <li>Each constructed {@link Notification} carries the correct
 *       {@code userId}, {@code eventId}, {@code message}, and {@link
 *       NotificationType} derived from the {@link
 *       EntrantStatus}</li>
 *   <li>Graceful handling of empty buckets (returns an empty list when no entrants exist)</li>
 * </ul>
 *
 * <h2>Testing Approach</h2>
 * <ul>
 *   <li>Mocks {@link FirebaseDatabase#getInstance(String)} to prevent
 *       real Firebase initialization</li>
 *   <li>Provides mocked {@link DatabaseReference} chains for the
 *       {@code "WaitingList"}, {@code "Organizer"}, {@code "Event"}, and {@code "User"} roots
 *       used inside {@link Organizer}</li>
 *   <li>Replaces {@link Task#continueWith(
 *       Continuation)} with synchronous lambda evaluation to
 *       immediately execute continuations with controlled {@link
 *       DataSnapshot} trees</li>
 * </ul>
 *
 * <p>
 * All Firebase references and snapshot traversals are simulated to guarantee full control
 * over test inputs and outputs, ensuring safe and deterministic verification of the organizer's
 * notification-broadcast logic in a local JVM environment.
 * </p>
 *
 * @author Eric Kane
 */
public class OrganizerNotifTest {

    private static final String ORG_ID  = "org-1";
    private static final String EVENT_ID = "e-1";
    private static final String MSG      = "Hello invited entrants";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    // Roots for each FirebaseService("...") in Organizer
    private DatabaseReference waitingRootRef;
    private DatabaseReference organizerRootRef;
    private DatabaseReference eventRootRef;
    private DatabaseReference userRootRef;

    // Under test
    private Organizer organizer;

    @Before
    public void setUp() {
        // Block real Firebase initialisation
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        waitingRootRef   = mock(DatabaseReference.class);
        organizerRootRef = mock(DatabaseReference.class);
        eventRootRef     = mock(DatabaseReference.class);
        userRootRef      = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("WaitingList")).thenReturn(waitingRootRef);
        when(mockDb.getReference("Organizer")).thenReturn(organizerRootRef);
        when(mockDb.getReference("Event")).thenReturn(eventRootRef);
        when(mockDb.getReference("User")).thenReturn(userRootRef);

        // Safe to construct Organizer (uses mocked FirebaseDatabase)
        organizer = new Organizer(ORG_ID, EVENT_ID);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    @Test
    public void sendSelectedNotification_delegatesToInvitedBucket() {
        Organizer spyOrg = spy(organizer);

        doNothing().when(spyOrg).sendWaitingListNotification(
                eq(EntrantStatus.INVITED), anyString());

        spyOrg.sendSelectedNotification(MSG);

        verify(spyOrg, times(1))
                .sendWaitingListNotification(EntrantStatus.INVITED, MSG);
    }

    @Test
    public void sendCancelledNotification_delegatesToCancelledBucket() {
        Organizer spyOrg = spy(organizer);

        doNothing().when(spyOrg).sendWaitingListNotification(
                eq(EntrantStatus.CANCELLED), anyString());

        spyOrg.sendCancelledNotification(MSG);

        verify(spyOrg, times(1))
                .sendWaitingListNotification(EntrantStatus.CANCELLED, MSG);
    }

    // ---------------------------------------------------------------------
    // sendWaitingListNotificationHelper - core user story behaviour
    // ---------------------------------------------------------------------

    @Test
    public void sendWaitingListNotificationHelper_buildsNotificationsForEachInvitedEntrant() {
        // Fake /WaitingList/{eventId}/INVITED snapshot with two entrants: u-1, u-2
        DataSnapshot root  = mock(DataSnapshot.class);
        DataSnapshot child1 = mock(DataSnapshot.class);
        DataSnapshot child2 = mock(DataSnapshot.class);

        when(child1.getKey()).thenReturn("u-1");
        when(child2.getKey()).thenReturn("u-2");
        when(root.getChildren()).thenAnswer(i -> iterable(child1, child2));

        // Set up the reference chain:
        // WaitingList -> {eventId} -> INVITED -> get()
        DatabaseReference eventRef   = mock(DatabaseReference.class);
        DatabaseReference invitedRef = mock(DatabaseReference.class);
        when(waitingRootRef.child(EVENT_ID)).thenReturn(eventRef);
        when(eventRef.child(EntrantStatus.INVITED.toString())).thenReturn(invitedRef);

        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(invitedRef.get()).thenReturn(mockGetTask);

        // Make continueWith(...) run synchronously and return a completed Task<ArrayList<Notification>>
        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, ArrayList<Notification>> cont =
                    (Continuation<DataSnapshot, ArrayList<Notification>>) inv.getArgument(0);

            ArrayList<Notification> out = cont.then(Tasks.forResult(root));
            return Tasks.forResult(out);
        });

        Task<ArrayList<Notification>> t =
                organizer.sendWaitingListNotificationHelper(EntrantStatus.INVITED, MSG);

        assertTrue(t.isComplete());
        ArrayList<Notification> notifications = t.getResult();
        assertNotNull(notifications);
        assertEquals(2, notifications.size());

        Notification n1 = notifications.get(0);
        Notification n2 = notifications.get(1);

        // Verify each notification was built correctly for this user story
        assertEquals("u-1", n1.getUserId());
        assertEquals(EVENT_ID, n1.getEventId());
        assertEquals(NotificationType.INVITED, n1.getNotificationType());
        assertEquals(MSG, n1.getMessage());

        assertEquals("u-2", n2.getUserId());
        assertEquals(EVENT_ID, n2.getEventId());
        assertEquals(NotificationType.INVITED, n2.getNotificationType());
        assertEquals(MSG, n2.getMessage());
    }

    @Test
    public void sendWaitingListNotificationHelper_returnsEmptyListWhenNoEntrants() {
        // Fake /WaitingList/{eventId}/INVITED snapshot with no children
        DataSnapshot root = mock(DataSnapshot.class);
        when(root.getChildren()).thenAnswer(i -> iterable()); // empty iterable

        DatabaseReference eventRef   = mock(DatabaseReference.class);
        DatabaseReference invitedRef = mock(DatabaseReference.class);
        when(waitingRootRef.child(EVENT_ID)).thenReturn(eventRef);
        when(eventRef.child(EntrantStatus.INVITED.toString())).thenReturn(invitedRef);

        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(invitedRef.get()).thenReturn(mockGetTask);

        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, ArrayList<Notification>> cont =
                    (Continuation<DataSnapshot, ArrayList<Notification>>) inv.getArgument(0);

            ArrayList<Notification> out = cont.then(Tasks.forResult(root));
            return Tasks.forResult(out);
        });

        Task<ArrayList<Notification>> t =
                organizer.sendWaitingListNotificationHelper(EntrantStatus.INVITED, MSG);

        assertTrue(t.isComplete());
        ArrayList<Notification> notifications = t.getResult();
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    public void sendWaitingListNotificationHelper_mapsWaitingStatusToWaitingNotificationType() {
        // Fake /WaitingList/{eventId}/WAITING snapshot with one entrant
        DataSnapshot root  = mock(DataSnapshot.class);
        DataSnapshot child = mock(DataSnapshot.class);
        when(child.getKey()).thenReturn("u-1");
        when(root.getChildren()).thenAnswer(i -> iterable(child));

        DatabaseReference eventRef   = mock(DatabaseReference.class);
        DatabaseReference waitingRef = mock(DatabaseReference.class);
        when(waitingRootRef.child(EVENT_ID)).thenReturn(eventRef);
        when(eventRef.child(EntrantStatus.WAITING.toString())).thenReturn(waitingRef);

        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(waitingRef.get()).thenReturn(mockGetTask);

        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, ArrayList<Notification>> cont =
                    (Continuation<DataSnapshot, ArrayList<Notification>>) inv.getArgument(0);

            ArrayList<Notification> out = cont.then(Tasks.forResult(root));
            return Tasks.forResult(out);
        });

        Task<ArrayList<Notification>> t =
                organizer.sendWaitingListNotificationHelper(EntrantStatus.WAITING, MSG);

        assertTrue(t.isComplete());
        ArrayList<Notification> notifications = t.getResult();
        assertEquals(1, notifications.size());

        Notification n = notifications.get(0);
        assertEquals(NotificationType.WAITING, n.getNotificationType());
    }

    // -------------------- helpers --------------------

    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        List<DataSnapshot> list = Arrays.asList(snaps);
        return new Iterable<DataSnapshot>() {
            @Override public Iterator<DataSnapshot> iterator() {
                return list.iterator();
            }
        };
    }
}
