package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.enums.NotificationType;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Notification;
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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Unit tests for {@link Notification}.
 *
 * <p>
 * These tests are designed to run entirely synchronously and without any main-thread dependency.
 * By mocking Firebase Realtime Database and the {@link com.google.android.gms.tasks.Task} API,
 * we ensure deterministic behaviour of {@link Notification#getEventName()} and
 * {@link Notification#createNotification()} methods without awaiting asynchronous callbacks.
 * </p>
 *
 * <h2>Key Behaviours Verified</h2>
 * <ul>
 *   <li>{@code createNotification()} correctly writes to the expected Firebase path and payload</li>
 *   <li>Constructor getters return consistent values for all fields</li>
 *   <li>{@code getEventName()} properly resolves the event name when found in mock snapshots</li>
 *   <li>Graceful handling of missing or unmatched event IDs (returns "NO NAME")</li>
 * </ul>
 *
 * <h2>Testing Approach</h2>
 * <ul>
 *   <li>Mocks {@link FirebaseDatabase#getInstance(String)} to prevent real Firebase initialization</li>
 *   <li>Injects {@link FirebaseService} mocks using reflection to isolate test scope</li>
 *   <li>Replaces {@link Task#continueWith(Continuation)} with synchronous lambda evaluation</li>
 * </ul>
 *
 * <p>
 * All Firebase references and snapshot traversals are simulated to guarantee full control
 * over test inputs and outputs, ensuring safe execution in local JVM environments.
 * </p>
 *
 * @author Jinn Kasai
 * @author Dung
 */
public class NotificationTest {

    private static final String UID = "u-1";
    private static final String EID = "e-1";
    private static final String MSG = "Hello there";

    // Static mock for FirebaseDatabase.getInstance(String)
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    // Refs used when FirebaseService("Notification"/"Event") is constructed
    private DatabaseReference mockNotifRef;
    private DatabaseReference mockEventRef;

    // Under test
    private Notification notification;

    // Service mocks to inject
    private FirebaseService mockNotifSvc;
    private FirebaseService mockEventSvc;

    @Before
    public void setUp() {
        // Prevent real Firebase init
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        mockNotifRef = mock(DatabaseReference.class);
        mockEventRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("Notification")).thenReturn(mockNotifRef);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);

        // Safe to construct (no real Firebase)
        notification = new Notification(UID, EID, NotificationType.INVITED, MSG);

        // Inject controllable service mocks
        mockNotifSvc = mock(FirebaseService.class);
        mockEventSvc = mock(FirebaseService.class);

        when(mockNotifSvc.getReference()).thenReturn(mockNotifRef);
        when(mockEventSvc.getReference()).thenReturn(mockEventRef);

        setPrivate(notification, "notificationService", mockNotifSvc);
        setPrivate(notification, "eventService",        mockEventSvc);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // -------------------- createNotification (sync) --------------------

    @Test
    public void createNotification_writesMessageUnderCorrectPath() {
        doNothing().when(mockNotifSvc).updateSubCollectionEntry(
                anyString(), anyString(), anyString(), any(HashMap.class));

        notification.createNotification();

        // Explicitly typed captor to match method signature
        org.mockito.ArgumentCaptor<HashMap<String, Object>> cap =
                org.mockito.ArgumentCaptor.forClass((Class) (Class<?>) HashMap.class);

        verify(mockNotifSvc, times(1)).updateSubCollectionEntry(
                eq(UID), eq(EID), eq(NotificationType.INVITED.toString()), cap.capture());

        HashMap<String, Object> sent = cap.getValue();
        assertEquals(MSG, sent.get("message"));
    }

    // -------------------- getters (sync) --------------------

    @Test
    public void getters_returnConstructorValues() {
        assertEquals(UID, notification.getUserId());
        assertEquals(EID, notification.getEventId());
        assertEquals(NotificationType.INVITED, notification.getNotificationType());
        assertEquals(MSG, notification.getMessage());
    }

    // -------------------- getEventName (no main-thread await) --------------------

    @Test
    public void getEventName_returnsNameWhenPresent() {
        // Build a fake /Event snapshot with a child matching EID that has {"name":"Party"}
        DataSnapshot root  = mock(DataSnapshot.class);
        DataSnapshot child = mock(DataSnapshot.class);

        when(child.getKey()).thenReturn(EID);
        HashMap<String, String> value = new HashMap<>();
        value.put("name", "Party");
        when(child.getValue()).thenReturn(value);
        when(root.getChildren()).thenAnswer(i -> iterable(child));

        // Return a mocked Task<DataSnapshot> so we can short-circuit continueWith(...)
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(mockEventRef.get()).thenReturn(mockGetTask);

        // When Notification calls mockGetTask.continueWith(...),
        // run the continuation immediately with a completed Task(root),
        // and return a completed Task<String> with the continuation's result.
        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, String> cont = (Continuation<DataSnapshot, String>) inv.getArgument(0);
            String out = cont.then(Tasks.forResult(root));
            return Tasks.forResult(out);
        });

        Task<String> t = notification.getEventName();
        assertTrue(t.isComplete());
        assertEquals("Party", t.getResult());
    }

    @Test
    public void getEventName_returnsNoNameWhenMissing() {
        DataSnapshot root  = mock(DataSnapshot.class);
        DataSnapshot child = mock(DataSnapshot.class);

        when(child.getKey()).thenReturn("some-other-id");
        when(child.getValue()).thenReturn(new HashMap<String, String>());
        when(root.getChildren()).thenAnswer(i -> iterable(child));

        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(mockEventRef.get()).thenReturn(mockGetTask);

        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, String> cont = (Continuation<DataSnapshot, String>) inv.getArgument(0);
            String out = cont.then(Tasks.forResult(root));
            return Tasks.forResult(out);
        });

        Task<String> t = notification.getEventName();
        assertTrue(t.isComplete());
        assertEquals("NO NAME", t.getResult());
    }

    // -------------------- helpers --------------------

    private static void setPrivate(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Iterable wrapper so Mockito can iterate root.getChildren(). */
    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        List<DataSnapshot> list = Arrays.asList(snaps);
        return new Iterable<DataSnapshot>() {
            @Override public Iterator<DataSnapshot> iterator() {
                return list.iterator();
            }
        };
    }
}
