package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.misc.FirebaseService;
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
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Unit tests for {@link User}.
 *
 * <p>
 * These tests are designed to verify the synchronous logic within {@link User}
 * while avoiding any real Firebase network calls or asynchronous {@code Task} behaviour.
 * Firebase Realtime Database interactions are fully mocked using Mockito.
 * </p>
 *
 * <h2>Test coverage includes:</h2>
 * <ul>
 *   <li>Validation and trimming of profile update fields</li>
 *   <li>Ensuring no writes occur when required fields are invalid</li>
 *   <li>Static mocking of {@link FirebaseDatabase#getInstance(String)} to prevent real initialization</li>
 *   <li>Reflection-based injection of {@link FirebaseService} mocks</li>
 *   <li>Verification of expected write payloads for {@code updateProfile()} and {@code createMockUser()}</li>
 * </ul>
 *
 * <p>
 * All tests execute synchronously and deterministically — no reliance on {@code Tasks.await()}
 * or Android main-thread components.
 * </p>
 *
 * @author Jinn Kasai
 * @author Dung
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
        assertEquals(false, sent.get("bannedFromOrganizer")); // default should be false
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
        assertEquals(false, sent.get("bannedFromOrganizer")); // default should be false
    }

    // -------------------- isBannedFromOrganizer --------------------

    @Test
    public void isBannedFromOrganizer_whenBanned_returnsTrue() {
        // Mock the user data snapshot with bannedFromOrganizer = true
        DataSnapshot userSnapshot = mock(DataSnapshot.class);
        DataSnapshot bannedField = mock(DataSnapshot.class);
        
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.child("bannedFromOrganizer")).thenReturn(bannedField);
        when(bannedField.getValue()).thenReturn(true);
        
        // Mock the get() call chain
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(mockUserRef.child(UID)).thenReturn(userChildRef);
        
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(userChildRef.get()).thenReturn(mockGetTask);
        
        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, Boolean> cont =
                    (Continuation<DataSnapshot, Boolean>) inv.getArgument(0);
            return cont.then(Tasks.forResult(userSnapshot));
        });
        
        Task<Boolean> result = user.isBannedFromOrganizer();
        assertTrue(result.isComplete());
        assertTrue(result.isSuccessful());
        assertTrue(result.getResult());
    }

    @Test
    public void isBannedFromOrganizer_whenNotBanned_returnsFalse() {
        // Mock the user data snapshot with bannedFromOrganizer = false
        DataSnapshot userSnapshot = mock(DataSnapshot.class);
        DataSnapshot bannedField = mock(DataSnapshot.class);
        
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.child("bannedFromOrganizer")).thenReturn(bannedField);
        when(bannedField.getValue()).thenReturn(false);
        
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(mockUserRef.child(UID)).thenReturn(userChildRef);
        
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(userChildRef.get()).thenReturn(mockGetTask);
        
        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, Boolean> cont =
                    (Continuation<DataSnapshot, Boolean>) inv.getArgument(0);
            return cont.then(Tasks.forResult(userSnapshot));
        });
        
        Task<Boolean> result = user.isBannedFromOrganizer();
        assertTrue(result.isComplete());
        assertTrue(result.isSuccessful());
        assertFalse(result.getResult());
    }

    @Test
    public void isBannedFromOrganizer_whenFieldMissing_returnsFalse() {
        // Mock the user data snapshot without bannedFromOrganizer field
        DataSnapshot userSnapshot = mock(DataSnapshot.class);
        DataSnapshot bannedField = mock(DataSnapshot.class);
        
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.child("bannedFromOrganizer")).thenReturn(bannedField);
        when(bannedField.getValue()).thenReturn(null); // field doesn't exist
        
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(mockUserRef.child(UID)).thenReturn(userChildRef);
        
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> mockGetTask = mock(Task.class);
        when(userChildRef.get()).thenReturn(mockGetTask);
        
        when(mockGetTask.continueWith(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Continuation<DataSnapshot, Boolean> cont =
                    (Continuation<DataSnapshot, Boolean>) inv.getArgument(0);
            return cont.then(Tasks.forResult(userSnapshot));
        });
        
        Task<Boolean> result = user.isBannedFromOrganizer();
        assertTrue(result.isComplete());
        assertTrue(result.isSuccessful());
        assertFalse(result.getResult()); // defaults to false when field missing
    }

    @Test
    public void setBannedFromOrganizer_updatesLocalField() throws Exception {
        user.setBannedFromOrganizer(true);
        Field field = user.getClass().getDeclaredField("bannedFromOrganizer");
        field.setAccessible(true);
        assertTrue((Boolean) field.get(user));
        
        user.setBannedFromOrganizer(false);
        assertFalse((Boolean) field.get(user));
    }

    // -------------------- helpers --------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
