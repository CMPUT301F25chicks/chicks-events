package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.example.chicksevent.misc.Organizer;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Unit tests for {@link Event}.
 *
 * <p>
 * These tests validate the behaviour of {@link Event} while ensuring that no real Firebase
 * network operations are triggered. By mocking {@link FirebaseDatabase} and replacing
 * {@link FirebaseService} instances through reflection, the test suite runs entirely
 * in a local JVM context.
 * </p>
 *
 * <h2>Key Behaviours Verified</h2>
 * <ul>
 *   <li>{@link Event#createEvent()} correctly pushes a new event and calls
 *       {@link FirebaseService#addEntry(HashMap, String)} with the expected key</li>
 *   <li>All getters and setters behave consistently and preserve state</li>
 *   <li>Graceful handling of {@code null} and optional fields in event creation</li>
 * </ul>
 *
 * <h2>Testing Strategy</h2>
 * <ul>
 *   <li>Static mocking of {@link FirebaseDatabase#getInstance(String)} prevents SDK initialisation</li>
 *   <li>All {@link DatabaseReference} chains are simulated, including {@code push()} and {@code getKey()}</li>
 *   <li>{@link FirebaseService} is injected via reflection to intercept Firebase writes</li>
 * </ul>
 *
 * <p>
 * This test suite ensures that {@link Event} can safely construct and persist metadata
 * to Firebase while correctly managing its internal state and data consistency.
 * </p>
 *
 * @author Jinn Kasai
 * @author Juan Rea
 */
public class EventTest {

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockPushedRef;

    @Before
    public void setUp() {
        // Block real Firebase initialization
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);
        mockEventRef = mock(DatabaseReference.class);
        mockPushedRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
        when(mockEventRef.push()).thenReturn(mockPushedRef);
        when(mockPushedRef.getKey()).thenReturn("E123");
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // -------------------- tests --------------------

    @Test
    public void createEvent_pushesAndAddsEntry() throws Exception {
        // Construct Event (its inline eventService will be replaced before use)
        Event e = new Event(
                "U1", null, "Test Event", "Desc",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", false
        );

        // Prepare a mock FirebaseService and inject it into the private field
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(e, "eventService", mockService);

        // Exercise
        e.createEvent();

        // Verify behaviour and state
        verify(mockEventRef, times(1)).push();
        verify(mockService, times(1))
                .addEntry(any(HashMap.class), eq("E123"));
        assertEquals("E123", e.getId());
        assertEquals("Test Event", e.getName());
        assertEquals("fun", e.getTag());
    }

    @Test
    public void gettersAndSetters_workConsistently() {
        Organizer org = new Organizer("U55", "E77");
        Event e = new Event("U55", "E77", "My Event", "Cool",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-10", 10, "poster.jpg", "tag1", false);

        e.setName("Updated");
        e.setEventDetails("NewDesc");
        e.setEventStartDate("2026-02-02");
        e.setEventEndDate("2026-02-05");
        e.setRegistrationStartDate("2025-11-11");
        e.setRegistrationEndDate("2025-11-12");
        e.setEntrantLimit(5);
        e.setPoster("new.png");
        e.setTag("fresh");
        e.setOrganizer(org);
        e.setId("EEE");

        assertEquals("EEE", e.getId());
        assertEquals("Updated", e.getName());
        assertEquals("NewDesc", e.getEventDetails());
        assertEquals("2026-02-02", e.getEventStartDate());
        assertEquals("2026-02-05", e.getEventEndDate());
        assertEquals("2025-11-11", e.getRegistrationStartDate());
        assertEquals("2025-11-12", e.getRegistrationEndDate());
        assertEquals(5, e.getEntrantLimit());
        assertEquals("new.png", e.getPoster());
        assertEquals("fresh", e.getTag());
        assertEquals(org, e.getOrganizer());
    }

    @Test
    public void createEvent_handlesNullsGracefully() throws Exception {
        Event e = new Event(
                "U1", null, "NullTest", null,
                null, null, null, null,
                0, null, null, false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockEventRef.push()).thenReturn(mockPushedRef);
        when(mockPushedRef.getKey()).thenReturn("E999");
        when(mockService.addEntry(any(HashMap.class), eq("E999"))).thenReturn("E999");
        setPrivate(e, "eventService", mockService);

        e.createEvent();

        verify(mockService, times(1)).addEntry(any(HashMap.class), eq("E999"));
        assertEquals("E999", e.getId());
    }

    // -------------------- helpers --------------------

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
