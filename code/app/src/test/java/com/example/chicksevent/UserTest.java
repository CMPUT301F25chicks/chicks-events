package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Minimal, synchronous unit tests for {@link User}.
 * These avoid any Task/async Firebase reads so they pass reliably.
 */
public class UserTest {

    private static final String UID = "uid123";

    // Static mock for FirebaseDatabase.getInstance(String) so User() doesn't init real Firebase
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    // Refs used by FirebaseService constructors inside User()
    private DatabaseReference mockUserRef;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockNotifRef;
    private DatabaseReference mockAdminRef;

    private User user;

    // Service mocks we inject to intercept writes
    private FirebaseService mockUserSvc;
    private FirebaseService mockEventSvc;
    private FirebaseService mockNotifSvc;
    private FirebaseService mockAdminSvc;

    @Before
    public void setUp() throws Exception {
        // Mock static getInstance(url) BEFORE constructing User
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        mockUserRef  = mock(DatabaseReference.class);
        mockEventRef = mock(DatabaseReference.class);
        mockNotifRef = mock(DatabaseReference.class);
        mockAdminRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("User")).thenReturn(mockUserRef);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
        when(mockDb.getReference("Notification")).thenReturn(mockNotifRef);
        when(mockDb.getReference("Admin")).thenReturn(mockAdminRef);

        // Construct user safely (no real Firebase)
        user = new User(UID);

        // Prepare service mocks and inject
        mockUserSvc  = mock(FirebaseService.class);
        mockEventSvc = mock(FirebaseService.class);
        mockNotifSvc = mock(FirebaseService.class);
        mockAdminSvc = mock(FirebaseService.class);

        setPrivate(user, "userService",         mockUserSvc);
        setPrivate(user, "eventService",        mockEventSvc);
        setPrivate(user, "notificationService", mockNotifSvc);
        setPrivate(user, "adminService",        mockAdminSvc);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // -------------------- Tests (all synchronous) ----------------------------

    @Test
    public void getUserId_returnsConstructorValue() {
        assertEquals(UID, user.getUserId());
    }

    @Test
    public void isOrganizer_returnsFalse() {
        assertFalse(user.isOrganizer());
    }

    @Test
    public void updateProfile_success_callsEditEntryWithTrimmedValues() {
        boolean ok = user.updateProfile(" Alice ", " alice@example.com ", " 555-0100 ", true);
        assertTrue(ok);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HashMap<String, Object>> cap =
                ArgumentCaptor.forClass((Class) HashMap.class);

        verify(mockUserSvc).editEntry(eq(UID), cap.capture());

        HashMap<String, Object> sent = cap.getValue();
        assertEquals("Alice", sent.get("name"));
        assertEquals("alice@example.com", sent.get("email"));
        assertEquals("555-0100", sent.get("phoneNumber"));
        assertEquals(UID, sent.get("uid"));
        assertEquals(true, sent.get("notificationsEnabled"));
    }

    @Test
    public void updateProfile_failsWhenNameOrEmailEmpty_neverWrites() {
        assertFalse(user.updateProfile(" ", "x@x", null, true));
        assertFalse(user.updateProfile("Bob", "   ", null, true));

        verify(mockUserSvc, never()).editEntry(anyString(), any(HashMap.class));
    }

    @Test
    public void updateProfile_failsWhenUserIdEmpty_neverWrites() throws Exception {
        // Force userId empty via reflection to trigger guard
        setPrivate(user, "userId", "");
        assertFalse(user.updateProfile("Alice", "a@b", null, true));
        verify(mockUserSvc, never()).editEntry(anyString(), any(HashMap.class));
    }

    @Test
    public void createMockUser_writesExpectedProfile() {
        user.createMockUser();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HashMap<String, Object>> cap =
                ArgumentCaptor.forClass((Class) HashMap.class);

        // createMockUser sets userId to "test-user-id" and calls updateProfile(...)
        verify(mockUserSvc).editEntry(eq("test-user-id"), cap.capture());

        HashMap<String, Object> sent = cap.getValue();
        assertEquals("test-user", sent.get("name"));
        assertEquals("test-email@gmail.com", sent.get("email"));
        assertEquals("123-456-7890", sent.get("phoneNumber"));
        assertEquals(false, sent.get("notificationsEnabled"));
        assertEquals("test-user-id", sent.get("uid"));
    }

    // -------------------- helpers --------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
