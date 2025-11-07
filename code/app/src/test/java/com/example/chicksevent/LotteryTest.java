package com.example.chicksevent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.chicksevent.misc.Lottery;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Map;

public class LotteryTest {

    private MockedStatic<FirebaseDatabase> firebaseDbStatic;
    private FirebaseDatabase mockDb;
    private DatabaseReference mockEventRef;
    private DatabaseReference mockWaitingRef;

    @Before
    public void setUpFirebaseStatic() {
        // Mock Firebase static getInstance() calls
        firebaseDbStatic = mockStatic(FirebaseDatabase.class);
        mockDb = mock(FirebaseDatabase.class);
        mockEventRef = mock(DatabaseReference.class);
        mockWaitingRef = mock(DatabaseReference.class);

        // Return the mock references for each root
        when(FirebaseDatabase.getInstance(anyString())).thenReturn(mockDb);
        when(mockDb.getReference("Event")).thenReturn(mockEventRef);
        when(mockDb.getReference("WaitingList")).thenReturn(mockWaitingRef);

        // Always allow .child() chaining to return itself
        when(mockEventRef.child(anyString())).thenReturn(mockEventRef);
        when(mockWaitingRef.child(anyString())).thenReturn(mockWaitingRef);
    }

    @After
    public void tearDownFirebaseStatic() {
        if (firebaseDbStatic != null) firebaseDbStatic.close();
    }

    // ---------------------------------------------------------------
    // SUCCESS CASE: entrantLimit exists, waiting list has entrants
    // ---------------------------------------------------------------
    @Test
    public void testRunLottery_success() {
        // Arrange
        Lottery lottery = new Lottery("event123");

        // Mock event limit snapshot
        DataSnapshot mockLimitSnap = mock(DataSnapshot.class);
        when(mockLimitSnap.exists()).thenReturn(true);
        when(mockLimitSnap.getValue(Integer.class)).thenReturn(2);

        // Mock waiting list snapshot with 3 waiting users
        DataSnapshot mockWaitingSnap = mock(DataSnapshot.class);
        when(mockWaitingSnap.exists()).thenReturn(true);

        DataSnapshot child1 = mock(DataSnapshot.class);
        DataSnapshot child2 = mock(DataSnapshot.class);
        DataSnapshot child3 = mock(DataSnapshot.class);
        when(child1.getKey()).thenReturn("uid1");
        when(child2.getKey()).thenReturn("uid2");
        when(child3.getKey()).thenReturn("uid3");
        when(mockWaitingSnap.getChildren()).thenReturn(
                java.util.Arrays.asList(child1, child2, child3)
        );

        // Stub eventService listener
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockLimitSnap);
            return null;
        }).when(mockEventRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Stub waitingListService listener
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockWaitingSnap);
            return null;
        }).when(mockWaitingRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        // Capture updateChildren
        doAnswer(invocation -> {
            Map<String, Object> updates = invocation.getArgument(0);
            DatabaseReference.CompletionListener listener = invocation.getArgument(1);
            listener.onComplete(null, mockWaitingRef); // simulate success
            Assert.assertFalse(updates.isEmpty());
            return null;
        }).when(mockWaitingRef).updateChildren(anyMap(), any());

        // Act
        lottery.runLottery();

        // Assert: updateChildren should be called once
        verify(mockWaitingRef, times(1))
                .updateChildren(anyMap(), any(DatabaseReference.CompletionListener.class));
    }

    // ---------------------------------------------------------------
    // FAILURE CASE: entrantLimit missing → should log error, no update
    // ---------------------------------------------------------------
    @Test
    public void testRunLottery_missingEntrantLimit() {
        Lottery lottery = new Lottery("event123");

        DataSnapshot mockLimitSnap = mock(DataSnapshot.class);
        when(mockLimitSnap.exists()).thenReturn(false); // simulate missing limit

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockLimitSnap);
            return null;
        }).when(mockEventRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        lottery.runLottery();

        verify(mockWaitingRef, never())
                .updateChildren(anyMap(), any(DatabaseReference.CompletionListener.class));
    }

    // ---------------------------------------------------------------
    // FAILURE CASE: Firebase read cancelled (DatabaseError)
    // ---------------------------------------------------------------
    @Test
    public void testRunLottery_databaseErrorOnEntrantLimit() {
        Lottery lottery = new Lottery("event123");

        DatabaseError mockError = mock(DatabaseError.class);
        when(mockError.getMessage()).thenReturn("Network error");

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onCancelled(mockError);
            return null;
        }).when(mockEventRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        lottery.runLottery();

        verify(mockWaitingRef, never())
                .updateChildren(anyMap(), any(DatabaseReference.CompletionListener.class));
    }
}
