package com.example.chicksevent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Unit tests for the Event POJO.
 */
public class EventTest {

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
    public void testSettersAndGetters() {
        Event e = new Event("entr123", "id001", "Art Fair", "Local artists showcase",
                "2025-12-01", "2025-12-05",
                "2025-11-01", "2025-11-25",
                100, "poster.jpg", "art culture");

        // Check all getters
        assertEquals("id001", e.getId());
        assertEquals("Art Fair", e.getName());
        assertEquals("Local artists showcase", e.getEventDetails());
        assertEquals("2025-12-01", e.getEventStartDate());
        assertEquals("2025-12-05", e.getEventEndDate());
        assertEquals("2025-11-01", e.getRegistrationStartDate());
        assertEquals("2025-11-25", e.getRegistrationEndDate());
        assertEquals(100, e.getEntrantLimit());
        assertEquals("poster.jpg", e.getPoster());
        assertEquals("art culture", e.getTag());
        assertNotNull(e.getOrganizer());
    }

    // --- TEST 2: Successful createEvent ---
    @Test
    public void testCreateEvent_Success() throws Exception {
        // Arrange
        Event e = new Event("entr123", null, "Hackathon", "Code for fun",
                "2025-11-10", "2025-11-11",
                "2025-10-01", "2025-10-31",
                50, "poster.png", "tech");

        // Mock DatabaseReference push/getKey
        when(mockRef.push()).thenReturn(mockRef);
        when(mockRef.getKey()).thenReturn("generated123");

        // Inject mock FirebaseService inside Event using reflection
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockRef);
        when(mockService.addEntry(any(HashMap.class), anyString()))
                .thenReturn("generated123");

        Field f = Event.class.getDeclaredField("eventService");
        f.setAccessible(true);
        f.set(e, mockService);

        // Act
        e.createEvent();

        // Assert
        assertEquals("generated123", e.getId());
        verify(mockService, times(1)).addEntry(any(HashMap.class), eq("generated123"));
        verify(mockRef, times(1)).push();
    }

    // --- TEST 3: Failed createEvent (simulate Firebase failure) ---
    @Test
    public void testCreateEvent_Failure() throws Exception {
        Event e = new Event("entr123", null, "Hackathon", "Code for fun",
                "2025-11-10", "2025-11-11",
                "2025-10-01", "2025-10-31",
                50, "poster.png", "tech");

        // Mock Firebase references
        when(mockRef.push()).thenReturn(mockRef);
        when(mockRef.getKey()).thenReturn("generated123");

        // Mock FirebaseService throwing
        FirebaseService mockService = mock(FirebaseService.class);
        when(mockService.getReference()).thenReturn(mockRef);
        when(mockService.addEntry(any(HashMap.class), anyString()))
                .thenThrow(new RuntimeException("Firebase write failed"));

        Field f = Event.class.getDeclaredField("eventService");
        f.setAccessible(true);
        f.set(e, mockService);

        // Act + Assert
        try {
            e.createEvent();
            fail("Expected RuntimeException not thrown");
        } catch (RuntimeException ex) {
            assertEquals("Firebase write failed", ex.getMessage());
        }

        verify(mockService, times(1)).addEntry(any(HashMap.class), eq("generated123"));
    }
}


