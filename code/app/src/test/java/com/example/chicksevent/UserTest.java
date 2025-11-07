package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.chicksevent.enums.NotificationType;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Notification;
import com.example.chicksevent.misc.User;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserTest {

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockRef;

    @Before
    public void setUpFirebaseStatic() {
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);
        mockRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);
        when(mockDb.getReference("Event")).thenReturn(mockRef);
    }

    @After
    public void tearDownFirebaseStatic() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    @Test
    public void testUpdateProfile_Success() throws Exception {
        // Arrange
        User user = new User("u123");

        // Inject mock FirebaseService via reflection
        FirebaseService mockService = mock(FirebaseService.class);
        Field f = User.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(user, mockService);

        // Act
        boolean result = user.updateProfile("Alice", "alice@example.com", "1234567890", true);

        // Assert
        assertTrue(result);
        verify(mockService, times(1)).editEntry(eq("u123"), any(HashMap.class));

        // Verify that fields were updated locally
        Field nameField = User.class.getDeclaredField("name");
        nameField.setAccessible(true);
        assertEquals("Alice", nameField.get(user));

        Field emailField = User.class.getDeclaredField("email");
        emailField.setAccessible(true);
        assertEquals("alice@example.com", emailField.get(user));

        Field phoneField = User.class.getDeclaredField("phoneNumber");
        phoneField.setAccessible(true);
        assertEquals("1234567890", phoneField.get(user));

        Field notifField = User.class.getDeclaredField("notificationsEnabled");
        notifField.setAccessible(true);
        assertEquals(true, notifField.get(user));
    }

    // --- TEST 2: updateProfile fail - missing userId ---
    @Test
    public void testUpdateProfile_Fail_NoUserId() throws Exception {
        User user = new User(null);

        // Inject mock FirebaseService to ensure it’s not called
        FirebaseService mockService = mock(FirebaseService.class);
        Field f = User.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(user, mockService);

        boolean result = user.updateProfile("Bob", "bob@example.com", "555", false);

        assertFalse(result);
        verify(mockService, never()).editEntry(anyString(), any(HashMap.class));
    }

    // --- TEST 3: updateProfile fail - missing name/email ---
    @Test
    public void testUpdateProfile_Fail_MissingFields() throws Exception {
        User user = new User("u999");

        FirebaseService mockService = mock(FirebaseService.class);
        Field f = User.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(user, mockService);

        // Missing name
        boolean result1 = user.updateProfile("", "bob@example.com", "555", false);
        // Missing email
        boolean result2 = user.updateProfile("Bob", "", "555", false);

        assertFalse(result1);
        assertFalse(result2);

        verify(mockService, never()).editEntry(anyString(), any(HashMap.class));
    }

    // --- TEST 4: updateNotificationPreference ---
    @Test
    public void testUpdateNotificationPreference() throws Exception {
        User user = new User("user123");

        FirebaseService mockService = mock(FirebaseService.class);
        Field f = User.class.getDeclaredField("userService");
        f.setAccessible(true);
        f.set(user, mockService);

        user.setNotificationsEnabled(false);

        Field notifField = User.class.getDeclaredField("notificationsEnabled");
        notifField.setAccessible(true);
        assertEquals(false, notifField.get(user));

        verify(mockService, times(1))
                .editEntry(eq("user123"), argThat(map -> (Boolean) map.get("notificationsEnabled") == false));
    }

    @Test
    public void testSetNotificationsEnabled_UpdatesField() throws Exception {
        // Arrange
        User user = new User("user123");

        // Use reflection to check private field
        Field notifField = User.class.getDeclaredField("notificationsEnabled");
        notifField.setAccessible(true);
        assertTrue((Boolean) notifField.get(user)); // default is true

        // Act
        user.setNotificationsEnabled(false);

        // Assert
        assertFalse((Boolean) notifField.get(user));
    }

    @Test
    public void testGetNotificationList_Success() throws Exception {
        // Arrange
        User user = new User("userABC");

        FirebaseService mockService = mock(FirebaseService.class);
        Field f = User.class.getDeclaredField("notificationService");
        f.setAccessible(true);
        f.set(user, mockService);

        DatabaseReference mockRef = mock(DatabaseReference.class);
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockService.getReference()).thenReturn(mockRef);
        when(mockRef.child("userABC")).thenReturn(mockChildRef);

        // Mock snapshot data structure:
        // event1 -> INVITED -> id1 -> { "message": "Hi!" }
        DataSnapshot eventSnapshot = mock(DataSnapshot.class);
        DataSnapshot invitedSnapshot = mock(DataSnapshot.class);
        DataSnapshot id1Snapshot = mock(DataSnapshot.class);
        when(eventSnapshot.getKey()).thenReturn("event1");
        when(invitedSnapshot.getKey()).thenReturn("INVITED");
        when(id1Snapshot.getKey()).thenReturn("id1");

        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("message", "Hi!");
        when(id1Snapshot.getValue()).thenReturn(messageMap);

        when(invitedSnapshot.getChildren()).thenReturn(List.of(id1Snapshot));
        when(eventSnapshot.getChildren()).thenReturn(List.of(invitedSnapshot));

        DataSnapshot rootSnapshot = mock(DataSnapshot.class);
        when(rootSnapshot.getChildren()).thenReturn(List.of(eventSnapshot));

        Task<DataSnapshot> successTask = Tasks.forResult(rootSnapshot);
        when(mockChildRef.get()).thenReturn(successTask);

        // Act
        user.getNotificationList().continueWith(task -> {
            ArrayList<Notification> notifications = task.getResult();

            assertNotNull(notifications);
            assertEquals(1, notifications.size());
            Notification noti = notifications.get(0);
            assertEquals("event1", noti.getEventId());
            assertEquals(NotificationType.INVITED, noti.getNotificationType());
            return null;
        });
    }

    @Test
    public void testGetNotificationList_Failure() throws Exception {
        // Arrange
        User user = new User("userABC");

        FirebaseService mockService = mock(FirebaseService.class);
        Field f = User.class.getDeclaredField("notificationService");
        f.setAccessible(true);
        f.set(user, mockService);

        DatabaseReference mockRef = mock(DatabaseReference.class);
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockService.getReference()).thenReturn(mockRef);
        when(mockRef.child("userABC")).thenReturn(mockChildRef);

        Exception mockException = new RuntimeException("Firebase get() failed");
        Task<DataSnapshot> failedTask = Tasks.forException(mockException);
        when(mockChildRef.get()).thenReturn(failedTask);
    }
}