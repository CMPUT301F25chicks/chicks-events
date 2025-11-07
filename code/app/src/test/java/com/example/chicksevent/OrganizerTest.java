package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Organizer;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

/**
 * Unit tests for {@link Organizer}.
 *
 * <p>
 * These tests provide minimal, synchronous verification of the {@link Organizer}
 * class, ensuring that no asynchronous Firebase {@code Task} behaviour is invoked.
 * Firebase interactions are mocked using Mockito, and {@link FirebaseDatabase#getInstance(String)}
 * is statically stubbed to prevent any real network or SDK initialization.
 * </p>
 *
 * <h2>Key Behaviours Verified</h2>
 * <ul>
 *   <li>Organizer ID getters and setters behave consistently</li>
 *   <li>{@code isOrganizer()} always returns {@code true}</li>
 *   <li>{@code listEntrants()} correctly attaches a {@link ValueEventListener}
 *       to the expected Realtime Database path for both WAITING and INVITED statuses</li>
 *   <li>{@code sendSelectedNotification()} correctly delegates to the
 *       {@code INVITED} branch of {@code sendWaitingListNotification()}</li>
 *   <li>Overloaded methods that default to WAITING behaviour are verified</li>
 * </ul>
 *
 * <p>
 * All Firebase service references inside {@link Organizer} are replaced via reflection
 * to ensure isolated, deterministic test execution with no dependency on network or
 * Android main-thread context.
 * </p>
 *
 * @author Jinn Kasai
 */
public class OrganizerTest {

    private static final String UID = "org-123";
    private static final String EVENT_ID = "event-xyz";

    // Static mock for FirebaseDatabase.getInstance(String) so Organizer()/User() don't init real Firebase
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;

    // Refs used by FirebaseService constructors inside Organizer/User
    private DatabaseReference mockUserRef;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockNotifRef;
    private DatabaseReference mockAdminRef;
    private DatabaseReference mockWaitingListRef;
    private DatabaseReference mockOrganizerRef;

    // Under test
    private Organizer organizer;

    // Service mocks we inject to intercept calls
    private FirebaseService mockWaitingListSvc;
    private FirebaseService mockUserSvc;
    private FirebaseService mockOrganizerSvc;
    private FirebaseService mockEventSvc;

    @Before
    public void setUp() throws Exception {
        // 1) Prevent real Firebase initialization
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);

        mockUserRef        = mock(DatabaseReference.class);
        mockEventRef       = mock(DatabaseReference.class);
        mockNotifRef       = mock(DatabaseReference.class);
        mockAdminRef       = mock(DatabaseReference.class);
        mockWaitingListRef = mock(DatabaseReference.class);
        mockOrganizerRef   = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        // All FirebaseService(...) created in Organizer/User() will request these roots
        when(mockDb.getReference("User")).thenReturn(mockUserRef);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
        when(mockDb.getReference("Notification")).thenReturn(mockNotifRef);
        when(mockDb.getReference("Admin")).thenReturn(mockAdminRef);
        when(mockDb.getReference("WaitingList")).thenReturn(mockWaitingListRef);
        when(mockDb.getReference("Organizer")).thenReturn(mockOrganizerRef);

        // 2) Construct Organizer safely (no real Firebase usage)
        organizer = new Organizer(UID, EVENT_ID);

        // 3) Prepare service mocks and inject them so we fully control RTDB calls
        mockWaitingListSvc = mock(FirebaseService.class);
        mockUserSvc        = mock(FirebaseService.class);
        mockOrganizerSvc   = mock(FirebaseService.class);
        mockEventSvc       = mock(FirebaseService.class);

        when(mockWaitingListSvc.getReference()).thenReturn(mockWaitingListRef);
        when(mockUserSvc.getReference()).thenReturn(mockUserRef);
        when(mockOrganizerSvc.getReference()).thenReturn(mockOrganizerRef);
        when(mockEventSvc.getReference()).thenReturn(mockEventRef);

        // Inject Organizer's private services (these are NOT the ones in User)
        setPrivate(organizer, "waitingListService", mockWaitingListSvc);
        setPrivate(organizer, "userService",        mockUserSvc);
        setPrivate(organizer, "organizerService",   mockOrganizerSvc);
        setPrivate(organizer, "eventService",       mockEventSvc);
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // -------------------- Synchronous tests --------------------

    @Test
    public void getOrganizerId_returnsConstructorValue() {
        assertEquals(UID, organizer.getOrganizerId());
    }

    @Test
    public void setOrganizerId_updatesValue() {
        organizer.setOrganizerId("new-id");
        assertEquals("new-id", organizer.getOrganizerId());
    }

    @Test
    public void isOrganizer_returnsTrue() {
        assertTrue(organizer.isOrganizer());
    }

    @Test
    public void listEntrants_default_waiting_attachesListenerOnCorrectPath() {
        // Build the child chain: /WaitingList/{eventId}/WAITING
        DatabaseReference eventNode  = mock(DatabaseReference.class);
        DatabaseReference statusNode = mock(DatabaseReference.class);

        when(mockWaitingListRef.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child(EntrantStatus.WAITING.toString())).thenReturn(statusNode);

        organizer.listEntrants(); // default is WAITING

        verify(statusNode, times(1)).addValueEventListener(any(ValueEventListener.class));
    }

    @Test
    public void listEntrants_withInvited_attachesListenerOnCorrectPath() {
        DatabaseReference eventNode  = mock(DatabaseReference.class);
        DatabaseReference statusNode = mock(DatabaseReference.class);

        when(mockWaitingListRef.child(EVENT_ID)).thenReturn(eventNode);
        when(eventNode.child(EntrantStatus.INVITED.toString())).thenReturn(statusNode);

        organizer.listEntrants(EntrantStatus.INVITED);

        verify(statusNode, times(1)).addValueEventListener(any(ValueEventListener.class));
    }

    @Test
    public void sendSelectedNotification_delegatesToInvited() throws Exception {
        // Spy to observe which status is passed to the status-specific method
        Organizer spyOrg = spy(organizer);

        // We only want to intercept the status-specific overload; leave others real
        doNothing().when(spyOrg).sendWaitingListNotification(eq(EntrantStatus.INVITED), anyString());

        spyOrg.sendSelectedNotification("msg");
        verify(spyOrg, times(1))
                .sendWaitingListNotification(eq(EntrantStatus.INVITED), eq("msg"));
    }

    @Test
    public void sendWaitingListNotification_defaultOverload_usesWAITING() throws Exception {
        Organizer spyOrg = spy(organizer);

        doNothing().when(spyOrg).sendWaitingListNotification(eq(EntrantStatus.WAITING), anyString());

        spyOrg.sendWaitingListNotification("hello");
        verify(spyOrg, times(1))
                .sendWaitingListNotification(eq(EntrantStatus.WAITING), eq("hello"));
    }

    // -------------------- helpers --------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
