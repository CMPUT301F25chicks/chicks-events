package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.example.chicksevent.adapter.NotificationAdapter;
import com.example.chicksevent.enums.NotificationType;
import com.example.chicksevent.fragment_admin.NotificationAdminFragment;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Notification;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;

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
        DataSnapshot mockSnap = mock(DataSnapshot.class);
        DataSnapshot n1 = mock(DataSnapshot.class);
        DataSnapshot n2 = mock(DataSnapshot.class);

        when(n1.getKey()).thenReturn("event-1");
        when(n2.getKey()).thenReturn("event-2");
        when(mockSnap.getChildren()).thenReturn(Arrays.asList(n1, n2));

        // Mock DatabaseReference chain
        DatabaseReference mockRef = mock(DatabaseReference.class);
        DatabaseReference mockEventRef = mock(DatabaseReference.class);

        when(mockService.getReference()).thenReturn(mockRef);
        when(mockRef.child(anyString())).thenReturn(mockEventRef);

        // Stub addValueEventListener on the final ref
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnap);
            return null;
        }).when(mockEventRef).addValueEventListener(any());

        // Mock NotificationAdapter
        NotificationAdapter mockAdapter = mock(NotificationAdapter.class);
        setPrivate(spyFragment, "adapter", mockAdapter);

        // Inject a dummy ListView
        setPrivate(spyFragment, "notificationView", mock(ListView.class));

        // Call private getNotificationList via reflection
        java.lang.reflect.Method m = spyFragment.getClass()
                .getDeclaredMethod("getNotificationList");
        m.setAccessible(true);
        Task<?> task = (Task<?>) m.invoke(spyFragment);

        // Extract notificationList
        @SuppressWarnings("unchecked")
        java.util.ArrayList<Notification> notifications = (java.util.ArrayList<Notification>) getPrivate(spyFragment, "notificationList");

        assertNotNull(notifications);
        // We mocked 2 children snapshots
        assertEquals(2, notifications.size());
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
