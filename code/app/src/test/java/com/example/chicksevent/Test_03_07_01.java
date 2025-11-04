package com.example.chicksevent;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.example.chicksevent.Admin;
import com.example.chicksevent.FirebaseService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)

public class Test_03_07_01 {
    @Mock
    FirebaseService mockOrganizerService;
    @Mock DatabaseReference mockRootRef;
    @Mock DatabaseReference mockChildRef;

    @Test
    public void removeOrganizerRemovesValue() throws Exception {
        String organizerId = "organizer_to_remove";

        when(mockOrganizerService.getReference()).thenReturn(mockRootRef);
        when(mockRootRef.child(organizerId)).thenReturn(mockChildRef);
        when(mockChildRef.removeValue()).thenReturn(Tasks.forResult(null));

        try (MockedConstruction<FirebaseService> ignored =
                     mockConstruction(FirebaseService.class, (mock, context) -> {
                     })) {

        Admin admin = new Admin(mockOrganizerService);



        Task<Void> t = admin.removeOrganizer(organizerId);
        Tasks.await(t);

        verify(mockOrganizerService, times(1)).getReference();
        verify(mockRootRef, times(1)).child(organizerId);
        verify(mockChildRef, times(1)).removeValue();

        assertTrue(t.isSuccessful());

    }
}}
