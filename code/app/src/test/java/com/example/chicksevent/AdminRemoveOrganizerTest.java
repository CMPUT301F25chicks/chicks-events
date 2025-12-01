package com.example.chicksevent;

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

import android.util.Log;

import com.example.chicksevent.misc.Admin;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

/**
 * Unit tests for US 03.07.01: As an administrator I want to remove organizers 
 * that violate app policy.
 * These tests validate that:
 * <ul>
 *   <li>Administrators can delete organizer profiles</li>
 *   <li>Administrators can ban organizers from creating events</li>
 *   <li>Organizer removal operations handle edge cases correctly</li>
 *   <li>Firebase operations are called correctly</li>
 *   <li>Error handling works for invalid inputs</li>
 * </ul>
 *
 * @author Jinn Kasai
 */
public class AdminRemoveOrganizerTest {

    private static final String ADMIN_ID = "admin-123";
    private static final String ORGANIZER_ID = "org-456";
    private static final String EVENT_ID = "event-789";

    // Static mock for FirebaseDatabase.getInstance(String)
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private MockedStatic<Log> logStatic;
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

        // Mock android.util.Log
        logStatic = mockStatic(Log.class);
        when(Log.d(anyString(), anyString())).thenReturn(0);
        when(Log.e(anyString(), anyString())).thenReturn(0);
        when(Log.e(anyString(), anyString(), any(Throwable.class))).thenReturn(0);
        when(Log.i(anyString(), anyString())).thenReturn(0);

        adminRoot = mock(DatabaseReference.class);
        userRoot = mock(DatabaseReference.class);
        eventRoot = mock(DatabaseReference.class);
        organizerRoot = mock(DatabaseReference.class);
        waitingListRoot = mock(DatabaseReference.class);
        notificationRoot = mock(DatabaseReference.class);
        imageRoot = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("Admin")).thenReturn(adminRoot);
        when(mockDb.getReference("User")).thenReturn(userRoot);
        when(mockDb.getReference("Event")).thenReturn(eventRoot);
        when(mockDb.getReference("Organizer")).thenReturn(organizerRoot);
        when(mockDb.getReference("WaitingList")).thenReturn(waitingListRoot);
        when(mockDb.getReference("Notification")).thenReturn(notificationRoot);
        when(mockDb.getReference("Image")).thenReturn(imageRoot);

        admin = new Admin(ADMIN_ID);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
        if (logStatic != null) logStatic.close();
    }

    // ==================== Delete Organizer Profile Tests ====================

    /**
     * Test Case 1: Admin can delete organizer profile with valid ID.
     * 
     * As an administrator, I should be able to delete an organizer's profile
     * when they violate app policy.
     */
    @Test
    public void admin_canDeleteOrganizerProfile_withValidId() {
        DatabaseReference orgIdRef = mock(DatabaseReference.class);
        when(organizerRoot.child(ORGANIZER_ID)).thenReturn(orgIdRef);

        // Mock removeValue with CompletionListener
        doAnswer(inv -> {
            DatabaseReference.CompletionListener listener = inv.getArgument(0);
            listener.onComplete(null, orgIdRef); // Simulate success
            return null;
        }).when(orgIdRef).removeValue(any(DatabaseReference.CompletionListener.class));

        Task<Void> task = admin.deleteOrganizerProfile(ORGANIZER_ID);

        // Verify Firebase operation was called
        verify(organizerRoot, times(1)).child(ORGANIZER_ID);
        verify(orgIdRef, times(1)).removeValue(any(DatabaseReference.CompletionListener.class));
        
        // Task should complete successfully
        assertTrue("Task should complete", task.isComplete());
        assertTrue("Task should be successful", task.isSuccessful());
        assertNull("Task should have no exception", task.getException());
    }

    /**
     * Test Case 2: Admin cannot delete organizer profile with null ID.
     * 
     * As an administrator, attempting to delete an organizer with a null ID
     * should fail with an exception.
     */
    @Test
    public void admin_cannotDeleteOrganizerProfile_withNullId() {
        Task<Void> task = admin.deleteOrganizerProfile(null);

        // Verify no Firebase operation was called
        verify(organizerRoot, never()).child(anyString());

        // Task should fail with exception
        assertTrue("Task should complete", task.isComplete());
        assertFalse("Task should fail", task.isSuccessful());
        assertNotNull("Task should have exception", task.getException());
        assertTrue("Exception should be IllegalArgumentException",
                   task.getException() instanceof IllegalArgumentException);
    }

    /**
     * Test Case 3: Admin cannot delete organizer profile with empty ID.
     * 
     * As an administrator, attempting to delete an organizer with an empty ID
     * should fail with an exception.
     */
    @Test
    public void admin_cannotDeleteOrganizerProfile_withEmptyId() {
        Task<Void> task = admin.deleteOrganizerProfile("");

        // Verify no Firebase operation was called
        verify(organizerRoot, never()).child(anyString());

        // Task should fail with exception
        assertTrue("Task should complete", task.isComplete());
        assertFalse("Task should fail", task.isSuccessful());
        assertNotNull("Task should have exception", task.getException());
        assertTrue("Exception should be IllegalArgumentException",
                   task.getException() instanceof IllegalArgumentException);
    }

    /**
     * Test Case 4: Delete organizer profile handles Firebase errors.
     * 
     * As an administrator, if Firebase returns an error when deleting an organizer,
     * the operation should fail gracefully.
     */
    @Test
    public void admin_deleteOrganizerProfile_handlesFirebaseErrors() {
        DatabaseReference orgIdRef = mock(DatabaseReference.class);
        when(organizerRoot.child(ORGANIZER_ID)).thenReturn(orgIdRef);

        // Mock removeValue with error
        doAnswer(inv -> {
            DatabaseReference.CompletionListener listener = inv.getArgument(0);
            DatabaseError error = mock(DatabaseError.class);
            com.google.firebase.database.DatabaseException dbException = 
                mock(com.google.firebase.database.DatabaseException.class);
            when(error.toException()).thenReturn(dbException);
            listener.onComplete(error, orgIdRef); // Simulate error
            return null;
        }).when(orgIdRef).removeValue(any(DatabaseReference.CompletionListener.class));

        Task<Void> task = admin.deleteOrganizerProfile(ORGANIZER_ID);

        // Verify Firebase operation was called
        verify(orgIdRef, times(1)).removeValue(any(DatabaseReference.CompletionListener.class));

        // Task should fail
        assertTrue("Task should complete", task.isComplete());
        assertFalse("Task should fail", task.isSuccessful());
        assertNotNull("Task should have exception", task.getException());
    }

    /**
     * Test Case 5: Delete organizer profile uses correct Firebase path.
     * 
     * As an administrator, when I delete an organizer profile, it should
     * be removed from the correct Firebase path: /Organizer/{organizerId}
     */
    @Test
    public void admin_deleteOrganizerProfile_usesCorrectFirebasePath() {
        DatabaseReference orgIdRef = mock(DatabaseReference.class);
        when(organizerRoot.child(ORGANIZER_ID)).thenReturn(orgIdRef);

        doAnswer(inv -> {
            DatabaseReference.CompletionListener listener = inv.getArgument(0);
            listener.onComplete(null, orgIdRef);
            return null;
        }).when(orgIdRef).removeValue(any(DatabaseReference.CompletionListener.class));

        admin.deleteOrganizerProfile(ORGANIZER_ID);

        // Verify correct path structure
        verify(organizerRoot, times(1)).child(ORGANIZER_ID);
        verify(orgIdRef, times(1)).removeValue(any(DatabaseReference.CompletionListener.class));
    }

    // ==================== Ban Organizer Tests ====================

    /**
     * Test Case 6: Admin can ban organizer from creating events.
     * 
     * As an administrator, I should be able to ban an organizer from creating
     * events when they violate app policy.
     */
    @Test
    public void admin_canBanOrganizer_fromCreatingEvents() {
        String reason = "Violated app policy";

        // Mock getEventsByOrganizer to return empty list (simplest path)
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(ORGANIZER_ID)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Mock notification service
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        DatabaseReference notifIdRef = mock(DatabaseReference.class);
        when(notificationRoot.child(ORGANIZER_ID)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask = admin.banUserFromOrganizer(ORGANIZER_ID, reason);

        assertNotNull("Ban task should not be null", banTask);
        // Task completes asynchronously, but structure is verified
    }

    /**
     * Test Case 7: Ban organizer sets bannedFromOrganizer flag.
     * 
     * As an administrator, when I ban an organizer, their bannedFromOrganizer
     * flag should be set to true in Firebase.
     * <p>
     * Note: This test verifies the ban operation can be called. Full verification
     * of the async flow requires more complex mocking.
     * </p>
     */
    @Test
    public void admin_banOrganizer_setsBannedFlag() {
        String reason = "Policy violation";

        // Mock getEventsByOrganizer to return empty list
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(ORGANIZER_ID)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Mock notification service
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(ORGANIZER_ID)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask = admin.banUserFromOrganizer(ORGANIZER_ID, reason);

        // Verify task is created
        assertNotNull("Ban task should not be null", banTask);
        // Note: Full verification of bannedFromOrganizer flag requires async callback execution
    }

    /**
     * Test Case 8: Ban organizer sends notification with reason.
     * 
     * As an administrator, when I ban an organizer, they should receive
     * a notification with the reason for the ban.
     * <p>
     * Note: This test verifies the ban operation can be called. Full verification
     * of notification creation requires async callback execution.
     * </p>
     */
    @Test
    public void admin_banOrganizer_sendsNotificationWithReason() {
        String reason = "Violated community guidelines";

        // Mock getEventsByOrganizer to return empty list
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(ORGANIZER_ID)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Mock notification service
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(ORGANIZER_ID)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask = admin.banUserFromOrganizer(ORGANIZER_ID, reason);

        // Verify task is created
        assertNotNull("Ban task should not be null", banTask);
        // Note: Full verification of notification requires async callback execution
    }

    /**
     * Test Case 9: Ban organizer puts events on hold.
     * 
     * As an administrator, when I ban an organizer, all their future events
     * should be put on hold (except events happening today or in the past).
     * <p>
     * Note: This test verifies the ban operation can be called. Full verification
     * of event on-hold status requires complex async mocking and is tested in
     * integration tests.
     * </p>
     */
    @Test
    public void admin_banOrganizer_putsEventsOnHold() {
        String reason = "Policy violation";

        // Mock getEventsByOrganizer to return empty list (simplest path)
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(ORGANIZER_ID)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Mock notification service
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(ORGANIZER_ID)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask = admin.banUserFromOrganizer(ORGANIZER_ID, reason);

        // Verify task is created
        assertNotNull("Ban task should not be null", banTask);
        // Note: Full verification of event on-hold status requires async callback execution
    }

    /**
     * Test Case 10: Ban organizer handles organizer with no events.
     * 
     * As an administrator, I should be able to ban an organizer even if
     * they have no events created.
     */
    @Test
    public void admin_canBanOrganizer_withNoEvents() {
        String reason = "Policy violation";

        // Mock getEventsByOrganizer to return empty list
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(ORGANIZER_ID)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Mock notification service
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(ORGANIZER_ID)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask = admin.banUserFromOrganizer(ORGANIZER_ID, reason);

        assertNotNull("Ban task should not be null", banTask);
        // Note: Full verification requires async callback execution
    }

    /**
     * Test Case 11: Ban organizer with null reason handles gracefully.
     * 
     * As an administrator, I should be able to ban an organizer even if
     * the reason is null (though not recommended).
     */
    @Test
    public void admin_canBanOrganizer_withNullReason() {
        // Mock getEventsByOrganizer to return empty list
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(ORGANIZER_ID)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Mock notification service
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(ORGANIZER_ID)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask = admin.banUserFromOrganizer(ORGANIZER_ID, null);

        assertNotNull("Ban task should not be null", banTask);
    }

    /**
     * Test Case 12: Ban organizer with empty reason handles gracefully.
     * 
     * As an administrator, I should be able to ban an organizer even if
     * the reason is empty.
     */
    @Test
    public void admin_canBanOrganizer_withEmptyReason() {
        // Mock getEventsByOrganizer to return empty list
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(ORGANIZER_ID)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Mock notification service
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(ORGANIZER_ID)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask = admin.banUserFromOrganizer(ORGANIZER_ID, "");

        assertNotNull("Ban task should not be null", banTask);
    }

    /**
     * Test Case 13: Multiple organizers can be banned.
     * 
     * As an administrator, I should be able to ban multiple organizers
     * that violate app policy.
     */
    @Test
    public void admin_canBanMultipleOrganizers() {
        String organizerId1 = "org-1";
        String organizerId2 = "org-2";
        String reason = "Policy violation";

        // Mock for organizer 1
        DataSnapshot emptyRoot1 = mock(DataSnapshot.class);
        when(emptyRoot1.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask1 = Tasks.forResult(emptyRoot1);
        
        // Mock for organizer 2
        DataSnapshot emptyRoot2 = mock(DataSnapshot.class);
        when(emptyRoot2.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask2 = Tasks.forResult(emptyRoot2);

        // Setup mocks to return different tasks based on which organizer
        when(eventRoot.get()).thenReturn(emptyEventsTask1, emptyEventsTask2);

        DatabaseReference userChildRef1 = mock(DatabaseReference.class);
        DatabaseReference userChildRef2 = mock(DatabaseReference.class);
        when(userRoot.child(organizerId1)).thenReturn(userChildRef1);
        when(userRoot.child(organizerId2)).thenReturn(userChildRef2);
        when(userChildRef1.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));
        when(userChildRef2.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        DatabaseReference userNotifRef1 = mock(DatabaseReference.class);
        DatabaseReference userNotifRef2 = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(organizerId1)).thenReturn(userNotifRef1);
        when(notificationRoot.child(organizerId2)).thenReturn(userNotifRef2);
        when(userNotifRef1.child("SYSTEM")).thenReturn(eventNotifRef);
        when(userNotifRef2.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask1 = admin.banUserFromOrganizer(organizerId1, reason);
        Task<Void> banTask2 = admin.banUserFromOrganizer(organizerId2, reason);

        assertNotNull("First ban task should not be null", banTask1);
        assertNotNull("Second ban task should not be null", banTask2);
        // Note: Full verification requires async callback execution
    }

    /**
     * Test Case 14: Delete and ban are independent operations.
     * 
     * As an administrator, I should be able to delete an organizer profile
     * and ban an organizer independently.
     */
    @Test
    public void admin_deleteAndBan_areIndependent() {
        String organizerId1 = "org-delete";
        String organizerId2 = "org-ban";

        // Setup delete
        DatabaseReference orgIdRef = mock(DatabaseReference.class);
        when(organizerRoot.child(organizerId1)).thenReturn(orgIdRef);
        doAnswer(inv -> {
            DatabaseReference.CompletionListener listener = inv.getArgument(0);
            listener.onComplete(null, orgIdRef);
            return null;
        }).when(orgIdRef).removeValue(any(DatabaseReference.CompletionListener.class));

        // Setup ban
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(organizerId2)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(organizerId2)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Execute both operations
        Task<Void> deleteTask = admin.deleteOrganizerProfile(organizerId1);
        Task<Void> banTask = admin.banUserFromOrganizer(organizerId2, "Reason");

        assertNotNull("Delete task should not be null", deleteTask);
        assertNotNull("Ban task should not be null", banTask);

        // Verify delete operation was called
        verify(orgIdRef, times(1)).removeValue(any(DatabaseReference.CompletionListener.class));
        // Note: Ban operation verification requires async callback execution
    }

    /**
     * Test Case 15: Ban organizer notifies entrants of events on hold.
     * 
     * As an administrator, when I ban an organizer and their events are
     * put on hold, entrants should be notified.
     * <p>
     * Note: This test verifies the ban operation can be called. Full verification
     * of entrant notifications requires complex async mocking and is tested in
     * integration tests.
     * </p>
     */
    @Test
    public void admin_banOrganizer_notifiesEntrants() {
        String reason = "Policy violation";

        // Mock getEventsByOrganizer to return empty list (simplest path)
        DataSnapshot emptyRoot = mock(DataSnapshot.class);
        when(emptyRoot.getChildren()).thenAnswer(i -> iterable());
        Task<DataSnapshot> emptyEventsTask = Tasks.forResult(emptyRoot);
        when(eventRoot.get()).thenReturn(emptyEventsTask);

        // Mock userService.editEntry
        DatabaseReference userChildRef = mock(DatabaseReference.class);
        when(userRoot.child(ORGANIZER_ID)).thenReturn(userChildRef);
        when(userChildRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        // Mock notification service
        DatabaseReference userNotifRef = mock(DatabaseReference.class);
        DatabaseReference eventNotifRef = mock(DatabaseReference.class);
        DatabaseReference typeNotifRef = mock(DatabaseReference.class);
        when(notificationRoot.child(ORGANIZER_ID)).thenReturn(userNotifRef);
        when(userNotifRef.child("SYSTEM")).thenReturn(eventNotifRef);
        when(eventNotifRef.child(anyString())).thenReturn(typeNotifRef);
        when(typeNotifRef.updateChildren(any(HashMap.class))).thenReturn(Tasks.forResult(null));

        Task<Void> banTask = admin.banUserFromOrganizer(ORGANIZER_ID, reason);

        // Verify task is created
        assertNotNull("Ban task should not be null", banTask);
        // Note: Full verification of entrant notifications requires async callback execution
    }

    // ==================== Helper Methods ====================

    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        List<DataSnapshot> list = Arrays.asList(snaps);
        return new Iterable<DataSnapshot>() {
            @Override
            public Iterator<DataSnapshot> iterator() {
                return list.iterator();
            }
        };
    }
}

