package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.chicksevent.misc.Event;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Unit tests for {@link Event} geolocation functionality.
 * <p>
 * Tests validate that the geolocationRequired field is properly stored,
 * retrieved, and persisted to Firebase.
 * </p>
 *
 * @author Jinn Kasai
 */
public class EventGeolocationTest {

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockPushedRef;

    @Before
    public void setUp() {
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

    @Test
    public void createEvent_withGeolocationRequired_includesInFirebase() throws Exception {
        Event e = new Event(
                "U1", null, "Test Event", "Desc", "2025-01-01",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", true  // geolocationRequired = true
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(e, "eventService", mockService);

        e.createEvent();

        // Verify that geolocationRequired is included in the map sent to Firebase
        verify(mockService, times(1)).addEntry(argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(true);
        }), eq("E123"));
    }

    @Test
    public void createEvent_withoutGeolocationRequired_defaultsToFalse() throws Exception {
        Event e = new Event(
                "U1", null, "Test Event", "Desc", "2025-01-01",
                "2025-01-01", "2025-01-02",
                "2024-12-01", "2024-12-15",
                50, "poster.png", "fun", false  // geolocationRequired = false
        );

        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockEventRef);
        when(mockService.addEntry(any(HashMap.class), eq("E123"))).thenReturn("E123");
        setPrivate(e, "eventService", mockService);

        e.createEvent();

        // Verify that geolocationRequired is false
        verify(mockService, times(1)).addEntry(argThat(map -> {
            return map.containsKey("geolocationRequired") && 
                   map.get("geolocationRequired").equals(false);
        }), eq("E123"));
    }

    @Test
    public void geolocationRequired_getterAndSetter_workCorrectly() {
        Event e = new Event(
                "U1", "E1", "Test", "Desc", null,
                null, null, null, null,
                10, null, null, false
        );

        // Test default value
        assertFalse(e.isGeolocationRequired());

        // Test setter
        e.setGeolocationRequired(true);
        assertTrue(e.isGeolocationRequired());

        // Test setter again
        e.setGeolocationRequired(false);
        assertFalse(e.isGeolocationRequired());
    }

    @Test
    public void constructor_withGeolocationRequired_preservesValue() {
        Event e1 = new Event(
                "U1", "E1", "Event1", "Desc", null,
                null, null, null, null,
                10, null, null, true
        );
        assertTrue(e1.isGeolocationRequired());

        Event e2 = new Event(
                "U1", "E2", "Event2", "Desc", null,
                null, null, null, null,
                10, null, null, false
        );
        assertFalse(e2.isGeolocationRequired());
    }

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

