package com.example.chicksevent;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

public class AdminTest {

    // Optional static mocking scaffold (not strictly needed for these tests,
    // but kept here in case Admin ever calls FirebaseDatabase.getInstance()).
    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference eventsRootRef;

    @Before
    public void setUpFirebaseStatic() {
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);
        eventsRootRef = mock(DatabaseReference.class);

        firebaseDbStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                .thenReturn(mockDb);
        when(mockDb.getReference("Event")).thenReturn(eventsRootRef);
    }

    @After
    public void tearDownFirebaseStatic() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // ---------- helpers ----------

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static DatabaseError mockDbError(String msg) {
        DatabaseError err = mock(DatabaseError.class);
        when(err.getMessage()).thenReturn(msg);
        when(err.toException()).thenReturn(new DatabaseException(msg));
        return err;
    }

    // ---------- deleteEvent tests ----------

//    @Test
//    public void deleteEvent_emptyId_completesExceptionally() {
//        Admin admin = new Admin("u1");
//
//        admin.deleteEvent("");
//    }
//
//    @Test
//    public void deleteEvent_success_callsCompletion_noError_completesSuccessfully() throws Exception {
//        FirebaseService eventsService = mock(FirebaseService.class);
//        DatabaseReference eventsRef   = mock(DatabaseReference.class);
//        DatabaseReference eventRef    = mock(DatabaseReference.class);
//
//        when(eventsService.getReference()).thenReturn(eventsRef);
//        when(eventsRef.child("E123")).thenReturn(eventRef);
//
//        // Simulate success callback
//        doAnswer(inv -> {
//            DatabaseReference.CompletionListener listener = inv.getArgument(0);
//            listener.onComplete(null, eventRef);
//            return null;
//        }).when(eventRef).removeValue(any(DatabaseReference.CompletionListener.class));
//
//        Admin admin = new Admin("u1");
//        setPrivateField(admin, "eventsService", eventsService);
//
//        admin.deleteEvent("E123");
//    }
//
//    @Test
//    public void deleteEvent_failure_callsCompletion_withError_completesExceptionally() throws Exception {
//        FirebaseService eventsService = mock(FirebaseService.class);
//        DatabaseReference eventsRef   = mock(DatabaseReference.class);
//        DatabaseReference eventRef    = mock(DatabaseReference.class);
//
//        when(eventsService.getReference()).thenReturn(eventsRef);
//        when(eventsRef.child("E123")).thenReturn(eventRef);
//
//        DatabaseError err = mockDbError("boom");
//        // Simulate failure callback
//        doAnswer(inv -> {
//            DatabaseReference.CompletionListener listener = inv.getArgument(0);
//            listener.onComplete(err, eventRef);
//            return null;
//        }).when(eventRef).removeValue(any(DatabaseReference.CompletionListener.class));
//
//        Admin admin = new Admin("u1");
//        setPrivateField(admin, "eventsService", eventsService);
//
//        admin.deleteEvent("E123");
//    }
//
//    // ---------- browseEvents tests ----------
//
//    @Test
//    @SuppressWarnings({"rawtypes","unchecked"})
//    public void browseEvents_success_materializesEvents_andSetsIdFromKey() throws Exception {
//        FirebaseService eventsService = mock(FirebaseService.class);
//        DatabaseReference eventsRef   = mock(DatabaseReference.class);
//        Task<DataSnapshot> getTask    = mock(Task.class);
//        DataSnapshot rootSnapshot     = mock(DataSnapshot.class);
//
//        when(eventsService.getReference()).thenReturn(eventsRef);
//        when(eventsRef.get()).thenReturn(getTask);
//
//        // Immediately invoke success listener with a fake root snapshot
//        when(getTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(inv -> {
//            OnSuccessListener<DataSnapshot> l = (OnSuccessListener<DataSnapshot>) inv.getArgument(0);
//            l.onSuccess(rootSnapshot);
//            return getTask; // fluent chain
//        });
//        // Make addOnFailureListener a no-op chain
//        when(getTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(getTask);
//
//        // children
//        DataSnapshot ch1 = mock(DataSnapshot.class);
//        DataSnapshot ch2 = mock(DataSnapshot.class);
//        when(rootSnapshot.getChildren()).thenReturn(Arrays.asList(ch1, ch2));
//        when(ch1.getKey()).thenReturn("E1");
//        when(ch2.getKey()).thenReturn("E2");
//
//        Event e1 = mock(Event.class);
//        Event e2 = mock(Event.class);
//        when(ch1.getValue(Event.class)).thenReturn(e1);
//        when(ch2.getValue(Event.class)).thenReturn(e2);
//
//        Admin admin = new Admin("u1");
//        setPrivateField(admin, "eventsService", eventsService);
//
//        Task<?> task = admin.browseEvents();
//
//        assertTrue(task.isComplete());
//        assertTrue(task.isSuccessful());
//
//        @SuppressWarnings("unchecked")
//        List<Event> result = (List<Event>) task.getResult();
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        verify(e1).setId("E1");
//        verify(e2).setId("E2");
//    }
//
//    @Test
//    @SuppressWarnings({"rawtypes","unchecked"})
//    public void browseEvents_failure_propagatesException() throws Exception {
//        FirebaseService eventsService = mock(FirebaseService.class);
//        DatabaseReference eventsRef   = mock(DatabaseReference.class);
//        Task<DataSnapshot> getTask    = mock(Task.class);
//
//        when(eventsService.getReference()).thenReturn(eventsRef);
//        when(eventsRef.get()).thenReturn(getTask);
//
//        // Success listener: return chain unchanged
//        when(getTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(getTask);
//
//        // Failure listener: immediately invoke with an exception
//        when(getTask.addOnFailureListener(any(OnFailureListener.class))).thenAnswer(inv -> {
//            OnFailureListener l = (OnFailureListener) inv.getArgument(0);
//            l.onFailure(new RuntimeException("read failed"));
//            return getTask;
//        });
//
//        Admin admin = new Admin("u1");
//        setPrivateField(admin, "eventsService", eventsService);
//
//        Task<?> task = admin.browseEvents();
//
//        assertTrue(task.isComplete());
//        assertFalse(task.isSuccessful());
//        Throwable ex = task.getException();
//        assertNotNull(ex);
//        assertTrue(String.valueOf(ex.getMessage()).contains("read failed"));
//    }
}
