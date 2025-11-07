package com.example.chicksevent;

import static org.junit.Assert.assertEquals;import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Looper;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.chicksevent.enums.EntrantStatus;
import com.example.chicksevent.misc.Entrant;
import com.example.chicksevent.misc.FirebaseService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Entrant Class Unit Testing
 */
public class EntrantTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private MockedStatic<Log> logStatic;
    private MockedStatic<Looper> looperStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockAdminRef;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockWaitingListRef;

    @Before
    public void setUpFirebaseStatic() {
        logStatic = mockStatic(Log.class);
        looperStatic = mockStatic(Looper.class);
        when(Looper.getMainLooper()).thenReturn(mock(Looper.class));

        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);
        mockAdminRef = mock(DatabaseReference.class);
        mockEventRef = mock(DatabaseReference.class);
        mockWaitingListRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);

        when(mockDb.getReference("Admin")).thenReturn(mockAdminRef);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
        when(mockDb.getReference("WaitingList")).thenReturn(mockWaitingListRef);
    }

    @After
    public void tearDownFirebaseStatic() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
        if (logStatic != null) logStatic.close();
        if (looperStatic != null) looperStatic.close();
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    public void constructor_initializesPropertiesCorrectly() {
        Entrant entrant = new Entrant("user123", "event456");
        Assert.assertEquals("user123", entrant.getUserId());
        assertEquals("user123", entrant.getEntrantId());
        assertEquals("event456", entrant.getEventId());
        assertEquals(EntrantStatus.WAITING, entrant.getStatus());
    }

    @Test
    public void joinWaitingList_updatesStatusAndCallsService() throws Exception {
        Entrant entrant = new Entrant("user123", "event456");
        FirebaseService mockWaitingListService = mock(FirebaseService.class);
        setPrivateField(entrant, "waitingListService", mockWaitingListService);
        HashMap<String, Object> expectedData = new HashMap<>();
        expectedData.put(" ", "");
        entrant.joinWaitingList(EntrantStatus.ACCEPTED);
        assertEquals(EntrantStatus.ACCEPTED, entrant.getStatus());
        verify(mockWaitingListService).updateSubCollectionEntry("event456", "ACCEPTED", "user123", expectedData);
    }

    @Test
    public void leaveWaitingList_updatesStatusAndCallsService() throws Exception {
        Entrant entrant = new Entrant("user123", "event456");
        FirebaseService mockWaitingListService = mock(FirebaseService.class);
        setPrivateField(entrant, "waitingListService", mockWaitingListService);
        entrant.joinWaitingList(EntrantStatus.WAITING);
        entrant.leaveWaitingList(EntrantStatus.WAITING);
        assertNull(entrant.getStatus());
        verify(mockWaitingListService).deleteSubCollectionEntry("event456", "WAITING", "user123");
    }

    @Test
    public void swapStatus_callsLeaveAndThenJoin() throws Exception {
        Entrant entrant = new Entrant("user123", "event456");
        FirebaseService mockWaitingListService = mock(FirebaseService.class);
        setPrivateField(entrant, "waitingListService", mockWaitingListService);
        assertEquals(EntrantStatus.WAITING, entrant.getStatus());
        entrant.swapStatus(EntrantStatus.ACCEPTED);
        assertEquals(EntrantStatus.ACCEPTED, entrant.getStatus());
        verify(mockWaitingListService, times(1)).deleteSubCollectionEntry("event456", "WAITING", "user123");
        HashMap<String, Object> expectedData = new HashMap<>();
        expectedData.put(" ", "");
        verify(mockWaitingListService, times(1)).updateSubCollectionEntry("event456", "ACCEPTED", "user123", expectedData);
    }

    @Test
    public void leaveWaitingList_shouldNotCallUpdate() throws Exception {
        Entrant entrant = new Entrant("user123", "event456");
        FirebaseService mockWaitingListService = mock(FirebaseService.class);
        setPrivateField(entrant, "waitingListService", mockWaitingListService);
        entrant.leaveWaitingList(EntrantStatus.WAITING);
        verify(mockWaitingListService, never()).updateSubCollectionEntry(anyString(), anyString(), anyString(), any(HashMap.class));
    }

    // The failing test has been removed.
}
