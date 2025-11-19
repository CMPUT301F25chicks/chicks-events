package com.example.chicksevent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.util.Log;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Unit tests for {@link Entrant} location functionality.
 * <p>
 * Tests validate that location data (latitude/longitude) is properly
 * stored when joining the waiting list with location information.
 * </p>
 *
 * @author Jinn Kasai
 */
public class EntrantLocationTest {

    private static final String EVENT_ID = "evt-123";
    private static final String ENTRANT_ID = "u-777";

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private MockedStatic<Log> logStatic;
    private FirebaseService mockWaitingSvc;
    private Entrant entrant;

    @Before
    public void setUp() throws Exception {
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        FirebaseDatabase mockDb = mock(FirebaseDatabase.class);
        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        logStatic = mockStatic(Log.class);
        when(Log.i(anyString(), anyString())).thenReturn(0);
        when(Log.d(anyString(), anyString())).thenReturn(0);
        when(Log.e(anyString(), anyString())).thenReturn(0);

        entrant = new Entrant(ENTRANT_ID, EVENT_ID);

        mockWaitingSvc = mock(FirebaseService.class);
        FirebaseService mockEntrantSvc = mock(FirebaseService.class);
        FirebaseService mockEventSvc = mock(FirebaseService.class);

        setPrivate(entrant, "waitingListService", mockWaitingSvc);
        setPrivate(entrant, "entrantService", mockEntrantSvc);
        setPrivate(entrant, "eventService", mockEventSvc);

        doNothing().when(mockWaitingSvc).updateSubCollectionEntry(
                anyString(), anyString(), anyString(), any(HashMap.class));
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
        if (logStatic != null) logStatic.close();
    }

    @Test
    public void joinWaitingList_withLocation_includesLatitudeAndLongitude() {
        double latitude = 53.5461;  // Edmonton coordinates
        double longitude = -113.4938;

        entrant.joinWaitingList(latitude, longitude);

        ArgumentCaptor<HashMap> mapCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), eq("WAITING"), eq(ENTRANT_ID), mapCaptor.capture());

        HashMap<String, Object> data = mapCaptor.getValue();
        assertTrue("Should contain latitude", data.containsKey("latitude"));
        assertTrue("Should contain longitude", data.containsKey("longitude"));
        assertEquals(latitude, data.get("latitude"));
        assertEquals(longitude, data.get("longitude"));
    }

    @Test
    public void joinWaitingList_withLocationAndStatus_includesLocationData() {
        double latitude = 40.7128;  // New York coordinates
        double longitude = -74.0060;
        EntrantStatus status = EntrantStatus.INVITED;

        entrant.joinWaitingList(status, latitude, longitude);

        ArgumentCaptor<HashMap> mapCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), eq("INVITED"), eq(ENTRANT_ID), mapCaptor.capture());

        HashMap<String, Object> data = mapCaptor.getValue();
        assertEquals(latitude, data.get("latitude"));
        assertEquals(longitude, data.get("longitude"));
        assertEquals(EntrantStatus.INVITED, entrant.getStatus());
    }

    @Test
    public void joinWaitingList_withoutLocation_doesNotIncludeLocationData() {
        entrant.joinWaitingList();

        ArgumentCaptor<HashMap> mapCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), eq("WAITING"), eq(ENTRANT_ID), mapCaptor.capture());

        HashMap<String, Object> data = mapCaptor.getValue();
        assertFalse("Should not contain latitude when no location provided", 
                   data.containsKey("latitude"));
        assertFalse("Should not contain longitude when no location provided", 
                   data.containsKey("longitude"));
    }

    @Test
    public void joinWaitingList_withNullLocation_doesNotIncludeLocationData() {
        entrant.joinWaitingList(EntrantStatus.WAITING, null, null);

        ArgumentCaptor<HashMap> mapCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), eq("WAITING"), eq(ENTRANT_ID), mapCaptor.capture());

        HashMap<String, Object> data = mapCaptor.getValue();
        assertFalse("Should not contain latitude when null", data.containsKey("latitude"));
        assertFalse("Should not contain longitude when null", data.containsKey("longitude"));
    }

    @Test
    public void joinWaitingList_withLocation_preservesBackwardCompatibility() {
        double latitude = 51.5074;  // London coordinates
        double longitude = -0.1278;

        entrant.joinWaitingList(latitude, longitude);

        ArgumentCaptor<HashMap> mapCaptor = ArgumentCaptor.forClass(HashMap.class);
        verify(mockWaitingSvc, times(1)).updateSubCollectionEntry(
                eq(EVENT_ID), eq("WAITING"), eq(ENTRANT_ID), mapCaptor.capture());

        HashMap<String, Object> data = mapCaptor.getValue();
        // Should still contain the placeholder for backward compatibility
        assertTrue("Should contain placeholder", data.containsKey(" "));
        // And also contain location data
        assertTrue("Should contain latitude", data.containsKey("latitude"));
        assertTrue("Should contain longitude", data.containsKey("longitude"));
    }

    @Test
    public void swapStatus_attemptsToReadLocationFromFirebase() throws Exception {
        // First join with location
        entrant.joinWaitingList(EntrantStatus.WAITING, 53.5461, -113.4938);
        
        // Mock the Firebase chain for async swapStatus
        com.google.firebase.database.DatabaseReference mockRef = mock(com.google.firebase.database.DatabaseReference.class);
        com.google.firebase.database.DatabaseReference mockEventRef = mock(com.google.firebase.database.DatabaseReference.class);
        com.google.firebase.database.DatabaseReference mockStatusRef = mock(com.google.firebase.database.DatabaseReference.class);
        com.google.firebase.database.DatabaseReference mockEntrantRef = mock(com.google.firebase.database.DatabaseReference.class);
        com.google.android.gms.tasks.Task<com.google.firebase.database.DataSnapshot> mockTask = mock(com.google.android.gms.tasks.Task.class);
        com.google.firebase.database.DataSnapshot mockSnapshot = mock(com.google.firebase.database.DataSnapshot.class);
        com.google.firebase.database.DataSnapshot mockLatSnapshot = mock(com.google.firebase.database.DataSnapshot.class);
        com.google.firebase.database.DataSnapshot mockLngSnapshot = mock(com.google.firebase.database.DataSnapshot.class);
        
        when(mockWaitingSvc.getReference()).thenReturn(mockRef);
        when(mockRef.child(EVENT_ID)).thenReturn(mockEventRef);
        when(mockEventRef.child("WAITING")).thenReturn(mockStatusRef);
        when(mockStatusRef.child(ENTRANT_ID)).thenReturn(mockEntrantRef);
        when(mockEntrantRef.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockSnapshot);
        when(mockSnapshot.child("latitude")).thenReturn(mockLatSnapshot);
        when(mockSnapshot.child("longitude")).thenReturn(mockLngSnapshot);
        when(mockLatSnapshot.getValue()).thenReturn(53.5461);
        when(mockLngSnapshot.getValue()).thenReturn(-113.4938);
        
        // Make the task complete immediately
        when(mockTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.database.DataSnapshot> listener = 
                invocation.getArgument(0);
            listener.onComplete(mockTask);
            return mockTask;
        });
        
        // Swap status - this should attempt to read location from Firebase
        entrant.swapStatus(EntrantStatus.INVITED);

        // Verify that getReference is called (indicating Firebase read attempt)
        verify(mockWaitingSvc, atLeastOnce()).getReference();
    }

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

