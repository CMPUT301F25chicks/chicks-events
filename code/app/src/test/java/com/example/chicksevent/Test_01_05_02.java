package com.example.chicksevent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Test_01_05_02 {

    @Test
    public void TestAcceptInvitation() {
        FirebaseService mockService = mock(FirebaseService.class);

        Participation participation = new Participation(
                "entrant123",
                "event456",
                EntrantStatus.INVITED
        );

        participation.acceptInvitation(mockService);

        assertEquals(EntrantStatus.ACCEPTED, participation.getStatus());

        HashMap<String, Object> expectedUpdate = new HashMap<>();
        expectedUpdate.put("status", "ACCEPTED");

        verify(mockService, times(1)).updateSubCollectionEntry(
                eq("entrant123"),
                eq("participation"),
                eq("event456"),
                eq(expectedUpdate)
        );

        Participation uninvitedParticipation = new Participation(
                "entrant789",
                "eventABC",
                EntrantStatus.UNINVITED
        );

        uninvitedParticipation.acceptInvitation(mockService);

        assertEquals(EntrantStatus.UNINVITED, uninvitedParticipation.getStatus());

        verify(mockService, never()).updateSubCollectionEntry(
                eq("entrant789"),
                eq("participation"),
                eq("eventABC"),
                eq(expectedUpdate)
        );
    }
}
