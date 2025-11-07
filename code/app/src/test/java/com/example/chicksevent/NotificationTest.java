package com.example.chicksevent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Unit tests for {@link Notification}.
 *
 * These tests never touch the real DB. We:
 *  - Mock FirebaseDatabase.getInstance(...) statically to avoid FirebaseApp initialization.
 *  - Inject a mocked FirebaseService into Notification for createNotification() behavior checks.
 */
public class NotificationTest {

    // --- static mocking to stop Firebase from initializing in JVM unit tests ---
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    private FirebaseService mockService; // injected for behavior tests

    @Before
    public void setup() {
        // 1) Block Firebase initialization at the source
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        // Cover both overloads, just in case your FirebaseService uses either
        firebaseDbStatic.when(FirebaseDatabase::getInstance).thenReturn(mockDb);
        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(mockDb);

        // 2) Prepare the service mock we will inject
        mockService = mock(FirebaseService.class);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // --- tiny helper to inject into the private field in Notification
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ---------------------- SUCCESS CASES ----------------------

    @Test
    @SuppressWarnings({"rawtypes","unchecked"})
    public void createNotification_success_sendsHashMapWithMessage() throws Exception {
        String userId = "U1";
        String eventId = "E1";
        NotificationType type = NotificationType.WAITING; // use an enum constant that exists in your project
        String message = "this is a message to all waiting list people";

        // Constructor will call new FirebaseService("Notification"), which calls FirebaseDatabase.getInstance(...)
        // Our static mock prevents FirebaseApp initialization.
        Notification n = new Notification(userId, eventId, type, message);

        // Replace real service with our mock
        setPrivateField(n, "notificationService", mockService);

        // Act
        n.createNotification();

        // Assert: capture exact HashMap payload
        ArgumentCaptor<HashMap<String, Object>> mapCaptor =
                ArgumentCaptor.forClass((Class) HashMap.class);

        verify(mockService, times(1)).updateSubCollectionEntry(
                eq(userId),
                eq(eventId),
                eq(type.toString()),
                mapCaptor.capture()
        );

        HashMap<String, Object> sent = mapCaptor.getValue();
        assertNotNull(sent);
        assertTrue(sent.containsKey("message"));
        assertEquals(message, sent.get("message"));
    }

    @Test
    @SuppressWarnings({"rawtypes","unchecked"})
    public void createNotification_nullMessage_stillSendsMessageKeyWithNull() throws Exception {
        String userId = "U2";
        String eventId = "E2";
        NotificationType type = NotificationType.WAITING;
        String message = null;

        Notification n = new Notification(userId, eventId, type, message);
        setPrivateField(n, "notificationService", mockService);

        n.createNotification();

        ArgumentCaptor<HashMap<String, Object>> mapCaptor =
                ArgumentCaptor.forClass((Class) HashMap.class);

        verify(mockService).updateSubCollectionEntry(
                eq(userId),
                eq(eventId),
                eq(type.toString()),
                mapCaptor.capture()
        );

        HashMap<String, Object> sent = mapCaptor.getValue();
        assertNotNull(sent);
        assertTrue(sent.containsKey("message"));
        assertNull(sent.get("message"));
    }

    @Test
    public void getters_returnCtorValues() {
        String userId = "U3";
        String eventId = "E3";
        NotificationType type = NotificationType.WAITING;
        String message = "hello";

        // This used to crash before we mocked FirebaseDatabase.getInstance()
        Notification n = new Notification(userId, eventId, type, message);

        assertEquals(type, n.getNotificationType());
        assertEquals(eventId, n.getEventId());
    }

    // ---------------------- FAILURE CASE ----------------------

    @Test
    @SuppressWarnings({"rawtypes"})
    public void createNotification_serviceThrows_propagatesException() throws Exception {
        String userId = "U4";
        String eventId = "E4";
        NotificationType type = NotificationType.WAITING;
        String message = "boom-test";

        Notification n = new Notification(userId, eventId, type, message);
        setPrivateField(n, "notificationService", mockService);

        // Make the service throw when called
        doThrow(new RuntimeException("boom"))
                .when(mockService)
                .updateSubCollectionEntry(anyString(), anyString(), anyString(), any(HashMap.class));

        try {
            n.createNotification();
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            String msg = ex.getMessage();
            assertNotNull(msg);                  // avoid NPE on contains
            assertTrue(msg.contains("boom"));
        }
    }
}
