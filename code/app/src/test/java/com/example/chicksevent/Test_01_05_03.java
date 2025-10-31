package com.example.chicksevent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Test_01_05_03 {

    @Test
    public void TestRejoinWaitingList() {
        FirebaseService mockService = mock(FirebaseService.class);

        Participation participation = new Participation(
                "entrant123",
                "event456",
                EntrantStatus.INVITED
        );

        participation.declineInvitation(mockService);

        assertEquals(EntrantStatus.DECLINED, participation.getStatus());

        HashMap<String, Object> expectedUpdate = new HashMap<>();
        expectedUpdate.put("status", "DECLINED");

        verify(mockService, times(1)).updateSubCollectionEntry(
                eq("entrant123"),
                eq("participation"),
                eq("event456"),
                eq(expectedUpdate)
        );

        Participation invitedParticipation = new Participation(
                "entrant789",
                "eventABC",
                EntrantStatus.UNINVITED
        );

        invitedParticipation.declineInvitation(mockService);

        assertEquals(EntrantStatus.UNINVITED, invitedParticipation.getStatus());

        verify(mockService, never()).updateSubCollectionEntry(
                eq("entrant789"),
                eq("participation"),
                eq("eventABC"),
                eq(expectedUpdate)
        );
    }
}
