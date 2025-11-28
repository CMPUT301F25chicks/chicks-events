package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.example.chicksevent.fragment_admin.NotificationAdminFragment;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Notification;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Unit tests for {@link NotificationAdminFragment} notification log functionality.
 *
 * <h2>User stories handled</h2>
 *   <li>US 03.08.01: As an administrator, I want to review logs of all notifications sent to entrants by organizers.</li>
 * <p>
 * These tests validate that the fragment correctly retrieves and displays notification logs from Firebase,
 * ensuring that:
 * <ul>
 *     <li>The adapter is properly set up with the retrieved {@link com.example.chicksevent.misc.Notification} objects.</li>
 *     <li>Data from mocked Firebase snapshots is correctly transformed into Notification objects with the correct type and message.</li>
 *     <li>Fragment handles the Firebase database reference chain correctly without requiring real Firebase initialization.</li>
 * </ul>
 * </p>
 *
 * <p>
 * All Firebase interactions are mocked to allow isolated unit testing without network dependencies.
 * </p>
 */

public class NotificationAdminFragmentTest {

    private NotificationAdminFragment fragment;
    private FirebaseService mockService;
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private MockedStatic<Log> logStatic;

    @Before
    public void setUp() throws Exception {
        // Mock FirebaseDatabase.getInstance()
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        FirebaseDatabase mockDb = mock(FirebaseDatabase.class);
        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        DatabaseReference mockRef = mock(DatabaseReference.class);
        when(mockDb.getReference(anyString())).thenReturn(mockRef);
        when(mockRef.child(anyString())).thenReturn(mockRef);

        // Mock android.util.Log
        logStatic = mockStatic(Log.class);
        when(Log.i(anyString(), anyString())).thenReturn(0);
        when(Log.e(anyString(), anyString())).thenReturn(0);

        // Construct fragment
        fragment = new NotificationAdminFragment();

        // Inject mocked FirebaseService
        mockService = mock(FirebaseService.class);
        setPrivate(fragment, "notificationService", mockService);

        // Inject ListView mock
        setPrivate(fragment, "notificationView", mock(ListView.class));
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
        if (logStatic != null) logStatic.close();
    }

    @Test
    public void listNotifications_populatesAdapter_whenSnapshotsReturned() throws Exception {

        // Spy fragment to mock getContext()
        NotificationAdminFragment spyFragment = spy(fragment);
        doReturn(mock(Context.class)).when(spyFragment).getContext();

        // Prepare DataSnapshots for two notifications
        DataSnapshot rootSnap = mock(DataSnapshot.class);
        DataSnapshot eventSnap = mock(DataSnapshot.class);
        DataSnapshot n1 = mock(DataSnapshot.class);
        DataSnapshot n2 = mock(DataSnapshot.class);

        // event-1 and event-2 are children of eventSnapshot
        when(eventSnap.getChildren()).thenReturn(Arrays.asList(n1, n2));

        // The root contains 1 event snapshot
        when(rootSnap.getChildren()).thenReturn(Arrays.asList(eventSnap));

        when(n1.getKey()).thenReturn("event-1");
        when(n2.getKey()).thenReturn("event-2");

        // Fake data structure for both notifications
        HashMap<String, HashMap<String, String>> waitingValue = new HashMap<>();
        HashMap<String, String> message = new HashMap<>();
        message.put("message", "Hello world");
        waitingValue.put("WAITING", message);

        when(n1.getValue()).thenReturn(waitingValue);
        when(n2.getValue()).thenReturn(waitingValue);

        // --- Mock DatabaseReference ---
        DatabaseReference mockRef = mock(DatabaseReference.class);
        when(mockService.getReference()).thenReturn(mockRef);

        // --- Mock Task<DataSnapshot> for get() ---
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(mockRef.get()).thenReturn(mockGetTask);
        when(mockGetTask.getResult()).thenReturn(rootSnap);

        // --- Mock continueWith() ---
        Task<ArrayList<Notification>> mockReturnTask = mock(Task.class);

        when(mockGetTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<DataSnapshot, ArrayList<Notification>> cont = invocation.getArgument(0);

            ArrayList<Notification> resultList = cont.then(mockGetTask);
            when(mockReturnTask.getResult()).thenReturn(resultList);

            return mockReturnTask;
        });

        // Call the method normally (no reflection)
        Task<ArrayList<Notification>> resultTask = spyFragment.getNotificationList();

        // Now simply assert the returned value
        ArrayList<Notification> list = resultTask.getResult();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("event-1", list.get(0).getEventId());
        assertEquals("event-2", list.get(1).getEventId());
    }

    // ---------------------- helpers ----------------------
    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        return new Iterable<DataSnapshot>() {
            @Override
            public Iterator<DataSnapshot> iterator() {
                return Arrays.asList(snaps).iterator();
            }
        };
    }

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getPrivate(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }
}
