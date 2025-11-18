package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.misc.Admin;
import com.example.chicksevent.misc.User;
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

    private Admin admin;

    @Before
    public void setUp() {
        // Block real Firebase init
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        adminRoot     = mock(DatabaseReference.class); // "Admin"
        userRoot      = mock(DatabaseReference.class); // "User"
        eventRoot     = mock(DatabaseReference.class); // "Event"
        organizerRoot = mock(DatabaseReference.class); // "Organizer"

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("Admin")).thenReturn(adminRoot);
        when(mockDb.getReference("User")).thenReturn(userRoot);
        when(mockDb.getReference("Event")).thenReturn(eventRoot);
        when(mockDb.getReference("Organizer")).thenReturn(organizerRoot);

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

    // -------------------- helpers --------------------

    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        java.util.List<DataSnapshot> list = Arrays.asList(snaps);
        return new Iterable<DataSnapshot>() {
            @Override public Iterator<DataSnapshot> iterator() { return list.iterator(); }
        };
    }
}
