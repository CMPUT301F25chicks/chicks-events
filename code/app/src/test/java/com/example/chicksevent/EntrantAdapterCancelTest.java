package com.example.chicksevent;

import static org.mockito.Mockito.*;

import android.content.Context;

import com.example.chicksevent.adapter.EntrantAdapter;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;

/**
 * Unit tests for {@link EntrantAdapter} cancel logic.
 *
 * <h2>User stories handled</h2>
 *   <li>US 02.06.04: As an organizer I want to cancel entrants that did not sign up for the event</li>
 * <p>
 * These tests validate that the adapter correctly identifies and handles cancelled entrants,
 * ensuring that:
 * <ul>
 *     <li>Invited entrants that are marked as cancelled are correctly recognized as cancelled.</li>
 *     <li>Adapter data updates properly reflect cancellation status in the UI.</li>
 *     <li>No real Firebase interactions are required; all entrant data is mocked.</li>
 * </ul>
 * </p>
 *
 * <p>
 * All interactions with entrant data are mocked to allow isolated unit testing without network dependencies.
 * </p>
 */

public class EntrantAdapterCancelTest {

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDB;
    private DatabaseReference mockRootRef;
    private DatabaseReference mockInvitedRef;
    private DatabaseReference mockCancelledRef;

    @Before
    public void setup() {
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);

        mockDB = mock(FirebaseDatabase.class);
        mockRootRef = mock(DatabaseReference.class);
        mockInvitedRef = mock(DatabaseReference.class);
        mockCancelledRef = mock(DatabaseReference.class);

        // IMPORTANT: mock both overloads of getInstance()
        firebaseDbStatic.when(FirebaseDatabase::getInstance)
                .thenReturn(mockDB);
        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDB);

        when(mockDB.getReference("WaitingList")).thenReturn(mockRootRef);
        when(mockRootRef.child("E1")).thenReturn(mockRootRef);
        when(mockRootRef.child("INVITED")).thenReturn(mockInvitedRef);
        when(mockRootRef.child("CANCELLED")).thenReturn(mockCancelledRef);

        when(mockInvitedRef.child("U1")).thenReturn(mockInvitedRef);
        when(mockCancelledRef.child("U1")).thenReturn(mockCancelledRef);
    }

    @After
    public void teardown() {
        firebaseDbStatic.close();
    }

    @Test
    public void invitedEntrant_isCancelled_correctly() {
        Context mockContext = mock(Context.class);

        ArrayList<Entrant> list = new ArrayList<>();
        Entrant invited = new Entrant("U1", "E1");
        invited.setStatus(EntrantStatus.INVITED);
        list.add(invited);

        EntrantAdapter adapter = new EntrantAdapter(mockContext, list);

        adapter.cancelEntrantForTest(invited);

        verify(mockInvitedRef, times(1)).removeValue();
        verify(mockCancelledRef, times(1)).setValue(true);
    }
}
