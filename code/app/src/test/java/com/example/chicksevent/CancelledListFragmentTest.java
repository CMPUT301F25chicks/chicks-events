package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.example.chicksevent.adapter.EntrantAdapter;
import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.fragment.CancelledListFragment;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Unit tests for {@link CancelledListFragment} cancel list functionality.
 *
 * <h2>User stories handled</h2>
 *   <p>US 02.06.02: As an organizer I want to see a list of all the cancelled entrants.</p>
 * These tests validate that the fragment correctly populates the list of cancelled entrants
 * from Firebase data snapshots, ensuring that:
 * <ul>
 *     <li>The adapter is properly set up with the retrieved entrants.</li>
 *     <li>Data from mocked Firebase snapshots is correctly transformed into {@link com.example.chicksevent.misc.Entrant} objects.</li>
 *     <li>Fragment handles the Firebase database reference chain correctly without requiring real Firebase initialization.</li>
 * </ul>
 *
 * <p>
 * All Firebase interactions are mocked to allow isolated unit testing without network dependencies.
 * </p>
 */

public class CancelledListFragmentTest {

    private CancelledListFragment fragment;
    private FirebaseService mockWaitingSvc;
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private MockedStatic<Log> logStatic;

    @Before
    public void setUp() throws Exception {
        // Mock FirebaseDatabase.getInstance()
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        FirebaseDatabase mockDb = mock(FirebaseDatabase.class);
        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        DatabaseReference mockRef = mock(DatabaseReference.class);
        when(mockDb.getReference(anyString())).thenReturn(mockRef);
        when(mockRef.child(anyString())).thenReturn(mockRef);

        // Mock android.util.Log
        logStatic = mockStatic(Log.class);
        when(Log.i(anyString(), anyString())).thenReturn(0);
        when(Log.e(anyString(), anyString())).thenReturn(0);

        // Construct fragment
        fragment = new CancelledListFragment();

        // Inject mocks
        mockWaitingSvc = mock(FirebaseService.class);
        setPrivate(fragment, "waitingListService", mockWaitingSvc);

        // Inject ListView mock
        setPrivate(fragment, "userView", mock(ListView.class));
        // Inject eventId
        setPrivate(fragment, "eventId", "evt-1");
    }

    @After
    public void tearDown() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
        if (logStatic != null) logStatic.close();
    }

    @Test
    public void listEntrants_populatesAdapter_whenSnapshotsReturned() throws Exception {
        // 1️⃣ Spy fragment to mock getContext()
        CancelledListFragment spyFragment = spy(fragment);

        // Provide a mock Context
        Context mockContext = mock(Context.class);
        doReturn(mockContext).when(spyFragment).getContext();

        // 2️⃣ Prepare DataSnapshots for two cancelled entrants
        DataSnapshot mockSnap = mock(DataSnapshot.class);
        DataSnapshot u1 = mock(DataSnapshot.class);
        DataSnapshot u2 = mock(DataSnapshot.class);
        when(u1.getKey()).thenReturn("u1");
        when(u2.getKey()).thenReturn("u2");
        when(mockSnap.getChildren()).thenReturn(Arrays.asList(u1, u2));

        // 3️⃣ Mock DatabaseReference chain
        DatabaseReference mockRef = mock(DatabaseReference.class);
        DatabaseReference mockEventRef = mock(DatabaseReference.class);
        DatabaseReference mockStatusRef = mock(DatabaseReference.class);

        when(mockWaitingSvc.getReference()).thenReturn(mockRef);
        when(mockRef.child("evt-1")).thenReturn(mockEventRef);
        when(mockEventRef.child("CANCELLED")).thenReturn(mockStatusRef);

        // 4️⃣ Stub addValueEventListener on the final ref to fire immediately
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnap);
            return null;
        }).when(mockStatusRef).addValueEventListener(any());

        // 5️⃣ Mock EntrantAdapter constructor so it won't fail
        EntrantAdapter mockAdapter = mock(EntrantAdapter.class);
        setPrivate(spyFragment, "entrantAdapter", mockAdapter);

        // 6️⃣ Inject a dummy ListView
        setPrivate(spyFragment, "userView", mock(ListView.class));

        // 7️⃣ Call private listEntrants via reflection
        java.lang.reflect.Method m = spyFragment.getClass()
                .getDeclaredMethod("listEntrants", EntrantStatus.class);
        m.setAccessible(true);
        m.invoke(spyFragment, EntrantStatus.CANCELLED);

        // 8️⃣ Verify the entrantDataList contains the two mock entrants
        @SuppressWarnings("unchecked")
        java.util.List<Entrant> entrants = (java.util.List<Entrant>) getPrivate(spyFragment, "entrantDataList");

        assertEquals(2, entrants.size());
        assertEquals("u1", entrants.get(0).getEntrantId());
        assertEquals("u2", entrants.get(1).getEntrantId());
    }




    // ---------------------- helpers ----------------------
    private static Iterable<DataSnapshot> iterable(DataSnapshot... snaps) {
        return new Iterable<DataSnapshot>() {
            @Override
            public Iterator<DataSnapshot> iterator() {
                return Arrays.asList(snaps).iterator();
            }
        };
    }

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getPrivate(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }
}
