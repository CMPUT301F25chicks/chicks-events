package com.example.chicksevent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
* Unit tests for Story 01_01_01
* As an entrant, I want to join the waiting list for a specific event
*
* Tests verify that when entrant joins waiting list,
* Participation object stores right data
* And Firebase update works (using mock database)
* */


@RunWith(MockitoJUnitRunner.class)
public class Test_01_01_01 {

    @Test
    public void testJoinWaitingListCorrectIds() {
        // Create mock Firebase Service
        FirebaseService mockService = mock(FirebaseService.class);

        // Create new participant and join waiting list
        String entrantId = "AnEntrant";
        String eventId = "birthdayParty";
        Participation p = new Participation(eventId, entrantId);
        p.joinWaitingList(mockService);

        // Make sure all info is correct
        assertEquals(eventId, p.getEventId());
        assertEquals(entrantId, p.getEntrantId());
        assertEquals(EntrantStatus.WAITING, p.getStatus());
    }

    @Test
    public void testJoinWaitingListFirebase() {

        FirebaseService mockService = mock(FirebaseService.class);

        // Create new participant
        String entrantId = "AnEntrant";
        String eventId = "birthdayParty";
        Participation p = new Participation(eventId, entrantId);

        p.joinWaitingList(mockService);

        // Status should now be WAITING
        assertEquals(EntrantStatus.WAITING, p.getStatus());

        // Build expected Firebase
        HashMap<String, Object> expectedData = new HashMap<>();
        expectedData.put("status", "WAITING");
        expectedData.put("eventId", eventId);
        expectedData.put("entrantId", entrantId);

        // verify firebase was updated once with right data
        verify(mockService, times(1)).updateSubCollectionEntry(
                eq(entrantId),
                eq("participation"),
                eq(eventId),
                eq(expectedData)
        );
    }
}
