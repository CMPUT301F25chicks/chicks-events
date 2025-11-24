package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.chicksevent.misc.Admin;
import com.example.chicksevent.misc.Organizer;
import com.example.chicksevent.misc.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Admin}.
 *
 * <p>
 * These tests run fully on the JVM and synchronously. They prevent real Firebase
 * initialization by statically mocking {@link FirebaseDatabase#getInstance(String)},
 * stub RTDB references, and short-circuit {@link Task} continuations to avoid any
 * main-thread or network dependencies.
 * </p>
 *
 * <h2>Behaviours verified</h2>
 * <ul>
 *   <li>{@code deleteEvent} and {@code deleteUserProfile} issue deletes only for non-empty IDs</li>
 *   <li>{@code deleteOrganizerProfile} returns an exception task for empty IDs and completes on success</li>
 *   <li>{@code browseEntrants} builds lightweight {@link User} objects from snapshot keys</li>
 *   <li>{@code browseEvents} returns a list whose size matches snapshot children</li>
 * </ul>
 *
 * <h2>Technique</h2>
 * <ul>
 *   <li>Static mock of {@link FirebaseDatabase} to block SDK initialization</li>
 *   <li>Manual triggering of continuation lambdas for {@code continueWithTask}</li>
 *   <li>Mockito stubs for {@link DatabaseReference} chains and completion listeners</li>
 * </ul>
 *
 * <p>All assertions are synchronous and deterministic.</p>
 *
 * @author Jinn Kasai
 */
public class AdminTest {

    private static final String UID = "admin-1";

    // Static mock for FirebaseDatabase.getInstance(String)
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    // Root references returned by getReference(...)
    private DatabaseReference adminRoot;
    private DatabaseReference userRoot;
    private DatabaseReference eventRoot;
    private DatabaseReference organizerRoot;
    private DatabaseReference waitingListRoot;
    private DatabaseReference notificationRoot;
    private DatabaseReference imageRoot;

    private Admin admin;

    @Before
    public void setUp() {
        // Block real Firebase init
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        adminRoot        = mock(DatabaseReference.class); // "Admin"
        userRoot         = mock(DatabaseReference.class); // "User"
        eventRoot        = mock(DatabaseReference.class); // "Event"
        organizerRoot    = mock(DatabaseReference.class); // "Organizer"
        waitingListRoot  = mock(DatabaseReference.class); // "WaitingList"
        notificationRoot = mock(DatabaseReference.class); // "Notification"
        imageRoot        = mock(DatabaseReference.class); // "Image"

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("Admin")).thenReturn(adminRoot);
        when(mockDb.getReference("User")).thenReturn(userRoot);
        when(mockDb.getReference("Event")).thenReturn(eventRoot);
        when(mockDb.getReference("Organizer")).thenReturn(organizerRoot);
        when(mockDb.getReference("WaitingList")).thenReturn(waitingListRoot);
        when(mockDb.getReference("Notification")).thenReturn(notificationRoot);
        when(mockDb.getReference("Image")).thenReturn(imageRoot);

        // Safe to construct Admin (its FirebaseService members will bind to the mocked refs)
        admin = new Admin(UID);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // -------------------- deleteEvent --------------------

    @Test
    public void deleteEvent_nonEmpty_callsRemoveOnEventPath() {
        DatabaseReference eventIdRef = mock(DatabaseReference.class);
        when(eventRoot.child("E123")).thenReturn(eventIdRef);

        // return a completed Task from removeValue() so the success-listener chain doesn't NPE
        when(eventIdRef.removeValue()).thenReturn(Tasks.forResult(null));

        admin.deleteEvent("E123");

        verify(eventIdRef, times(1)).removeValue();
    }

    @Test
    public void deleteEvent_empty_isNoop() {
        admin.deleteEvent(null);
        admin.deleteEvent("");
        verify(eventRoot, never()).child(anyString());
    }

    // -------------------- deleteUserProfile --------------------

    @Test
    public void deleteUserProfile_nonEmpty_callsRemoveOnUserPath() {
        DatabaseReference userIdRef = mock(DatabaseReference.class);
        when(userRoot.child("U42")).thenReturn(userIdRef);

        // ensure removeValue() returns a completed Task
        when(userIdRef.removeValue()).thenReturn(Tasks.forResult(null));

        admin.deleteUserProfile("U42");

        verify(userIdRef, times(1)).removeValue();
    }

    @Test
    public void deleteUserProfile_empty_isNoop() {
        admin.deleteUserProfile(null);
        admin.deleteUserProfile("");
        verify(userRoot, never()).child(anyString());
    }

    // -------------------- deleteOrganizerProfile --------------------

    @Test
    public void deleteOrganizerProfile_empty_returnsExceptionTask() {
        Task<Void> t = admin.deleteOrganizerProfile("");
        assertTrue(t.isComplete());
        assertFalse(t.isSuccessful());
        assertNotNull(t.getException());
    }

    @Test
    public void deleteOrganizerProfile_valid_callsRemove_andCompletes() {
        DatabaseReference orgIdRef = mock(DatabaseReference.class);
        when(organizerRoot.child("ORG7")).thenReturn(orgIdRef);

        // Admin.deleteOrganizerProfile uses removeValue(CompletionListener) overload
        doAnswer(inv -> {
            DatabaseReference.CompletionListener cl = inv.getArgument(0);
            cl.onComplete(null, organizerRoot); // simulate success
            return null;
        }).when(orgIdRef).removeValue(any(DatabaseReference.CompletionListener.class));

        Task<Void> t = admin.deleteOrganizerProfile("ORG7");
        assertTrue(t.isComplete());
        assertTrue(t.isSuccessful());
        assertNull(t.getException());
    }

    // -------------------- browseEntrants --------------------

    @Test
    public void browseUsers_returnsUsersFromSnapshot() {
        // Fake /User snapshot: children with keys U1, U2
        DataSnapshot root = mock(DataSnapshot.class);
        DataSnapshot u1   = mock(DataSnapshot.class);
        DataSnapshot u2   = mock(DataSnapshot.class);
        when(u1.getKey()).thenReturn("U1");
        when(u2.getKey()).thenReturn("U2");
        when(root.getChildren()).thenAnswer(i -> iterable(u1, u2));

        // Return a mocked Task<DataSnapshot> so we can short-circuit continueWithTask(...)
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(userRoot.get()).thenReturn(mockGetTask);

        // Admin.browseEntrants() uses continueWithTask(...)
        when(mockGetTask.continueWithTask(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, Task<List<User>>> cont =
                    (Continuation<DataSnapshot, Task<List<User>>>) inv.getArgument(0);
            return cont.then(Tasks.forResult(root));
        });

        Task<List<User>> out = admin.browseUsers();
        assertTrue(out.isComplete());
        assertTrue(out.isSuccessful());
        assertEquals(2, out.getResult().size());
        assertEquals("U1", out.getResult().get(0).getUserId());
        assertEquals("U2", out.getResult().get(1).getUserId());
    }

    // -------------------- browseEvents --------------------

    @Test
    public void browseEvents_returnsListSizeMatchingChildren() {
        // Fake /Event snapshot with two children (each child value is a map)
        DataSnapshot root = mock(DataSnapshot.class);
        DataSnapshot e1   = mock(DataSnapshot.class);
        DataSnapshot e2   = mock(DataSnapshot.class);

        when(e1.getValue()).thenReturn(new java.util.HashMap<String,String>() {{
            put("id", "E1"); put("name", "Alpha");
        }});
        when(e2.getValue()).thenReturn(new java.util.HashMap<String,String>() {{
            put("id", "E2"); put("name", "Beta");
        }});
        when(root.getChildren()).thenAnswer(i -> iterable(e1, e2));

        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(eventRoot.get()).thenReturn(mockGetTask);

        when(mockGetTask.continueWithTask(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, Task<java.util.List<com.example.chicksevent.misc.Event>>> cont =
                    (Continuation<DataSnapshot, Task<java.util.List<com.example.chicksevent.misc.Event>>>) inv.getArgument(0);
            return cont.then(Tasks.forResult(root));
        });

        Task<java.util.List<com.example.chicksevent.misc.Event>> out = admin.browseEvents();
        assertTrue(out.isComplete());
        assertTrue(out.isSuccessful());
        assertEquals(2, out.getResult().size()); // size matches children
    }

    // -------------------- browseOrganizers --------------------

    @Test
    public void browseOrganizers_returnsOrganizersFromEvents() {
        // Create fake events with different organizers
        DataSnapshot root = mock(DataSnapshot.class);
        DataSnapshot e1 = mock(DataSnapshot.class);
        DataSnapshot e2 = mock(DataSnapshot.class);
        DataSnapshot e3 = mock(DataSnapshot.class);

        HashMap<String, Object> event1Data = new HashMap<>();
        event1Data.put("organizer", "org1");
        event1Data.put("name", "Event 1");
        
        HashMap<String, Object> event2Data = new HashMap<>();
        event2Data.put("organizer", "org1"); // same organizer
        event2Data.put("name", "Event 2");
        
        HashMap<String, Object> event3Data = new HashMap<>();
        event3Data.put("organizer", "org2"); // different organizer
        event3Data.put("name", "Event 3");

        when(e1.getValue()).thenReturn(event1Data);
        when(e2.getValue()).thenReturn(event2Data);
        when(e3.getValue()).thenReturn(event3Data);
        when(root.getChildren()).thenAnswer(i -> iterable(e1, e2, e3));

        // Mock the task and intercept addOnSuccessListener
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(eventRoot.get()).thenReturn(mockGetTask);
        
        // Intercept addOnSuccessListener and execute it immediately
        doAnswer(inv -> {
            OnSuccessListener<DataSnapshot> listener = inv.getArgument(0);
            listener.onSuccess(root);
            return mockGetTask;
        }).when(mockGetTask).addOnSuccessListener(any(OnSuccessListener.class));
        
        Task<List<Organizer>> out = admin.browseOrganizers();
        // addOnSuccessListener executes synchronously, so TCS should be set
        assertTrue("Task should be complete", out.isComplete());
        assertTrue("Task should be successful", out.isSuccessful());
        // Should return 2 unique organizers (org1 and org2)
        assertEquals(2, out.getResult().size());
        Set<String> organizerIds = new java.util.HashSet<>();
        for (Organizer org : out.getResult()) {
            organizerIds.add(org.getOrganizerId());
        }
        assertTrue(organizerIds.contains("org1"));
        assertTrue(organizerIds.contains("org2"));
    }

    @Test
    public void browseOrganizers_noEvents_returnsEmptyList() {
        DataSnapshot root = mock(DataSnapshot.class);
        when(root.getChildren()).thenAnswer(i -> iterable());

        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(eventRoot.get()).thenReturn(mockGetTask);
        
        doAnswer(inv -> {
            OnSuccessListener<DataSnapshot> listener = inv.getArgument(0);
            listener.onSuccess(root);
            return mockGetTask;
        }).when(mockGetTask).addOnSuccessListener(any(OnSuccessListener.class));
        
        Task<List<Organizer>> out = admin.browseOrganizers();
        assertTrue("Task should be complete", out.isComplete());
        assertTrue("Task should be successful", out.isSuccessful());
        assertEquals(0, out.getResult().size());
    }

    // -------------------- getEventsByOrganizer --------------------

    @Test
    public void getEventsByOrganizer_returnsEventIdsForOrganizer() {
        DataSnapshot root = mock(DataSnapshot.class);
        DataSnapshot e1 = mock(DataSnapshot.class);
        DataSnapshot e2 = mock(DataSnapshot.class);
        DataSnapshot e3 = mock(DataSnapshot.class);

        HashMap<String, Object> event1Data = new HashMap<>();
        event1Data.put("organizer", "org1");
        event1Data.put("name", "Event 1");
        
        HashMap<String, Object> event2Data = new HashMap<>();
        event2Data.put("organizer", "org2");
        event2Data.put("name", "Event 2");
        
        HashMap<String, Object> event3Data = new HashMap<>();
        event3Data.put("organizer", "org1");
        event3Data.put("name", "Event 3");

        when(e1.getKey()).thenReturn("event1");
        when(e2.getKey()).thenReturn("event2");
        when(e3.getKey()).thenReturn("event3");
        when(e1.getValue()).thenReturn(event1Data);
        when(e2.getValue()).thenReturn(event2Data);
        when(e3.getValue()).thenReturn(event3Data);
        when(root.getChildren()).thenAnswer(i -> iterable(e1, e2, e3));

        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(eventRoot.get()).thenReturn(mockGetTask);
        
        // Intercept continueWith and execute continuation immediately
        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, List<String>> cont = (Continuation<DataSnapshot, List<String>>) inv.getArgument(0);
            List<String> result = cont.then(Tasks.forResult(root));
            return Tasks.forResult(result);
        });
        
        Task<List<String>> out = admin.getEventsByOrganizer("org1");
        assertTrue("Task should be complete", out.isComplete());
        assertTrue("Task should be successful", out.isSuccessful());
        List<String> eventIds = out.getResult();
        assertEquals(2, eventIds.size());
        assertTrue(eventIds.contains("event1"));
        assertTrue(eventIds.contains("event3"));
        assertFalse(eventIds.contains("event2"));
    }

    // -------------------- banUserFromOrganizer --------------------

    @Test
    public void banUserFromOrganizer_returnsTask() {
        String userId = "user123";
        
        // Mock getEventsByOrganizer to return empty list (no events to delete)
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);
        
        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(userId)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));
        
        // Mock notification service
        DatabaseReference notifRef = mock(DatabaseReference.class);
        when(notificationRoot.push()).thenReturn(notifRef);
        when(notifRef.getKey()).thenReturn("N123");
        when(notifRef.setValue(any())).thenReturn(Tasks.forResult(null));
        
        Task<Void> banTask = admin.banUserFromOrganizer(userId, "violation");
        assertNotNull(banTask);
        // Task may not be complete immediately due to async operations
    }

    // -------------------- unbanUserFromOrganizer --------------------

    @Test
    public void unbanUserFromOrganizer_returnsTask() {
        String userId = "user123";
        
        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(userId)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));
        
        // Mock notification service for Notification.createNotification()
        // Notification uses updateSubCollectionEntry which calls child().child().updateChildren()
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(userId)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));
        
        Task<Void> unbanTask = admin.unbanUserFromOrganizer(userId);
        assertNotNull(unbanTask);
        // The task completes synchronously for unban
        assertTrue(unbanTask.isComplete());
        assertTrue(unbanTask.isSuccessful());
    }

    // -------------------- helpers --------------------

    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        java.util.List<DataSnapshot> list = Arrays.asList(snaps);
        return new Iterable<DataSnapshot>() {
            @Override public Iterator<DataSnapshot> iterator() { return list.iterator(); }
        };
    }
}
