package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.chicksevent.misc.Event;

import org.junit.Test;

/**
 * Unit tests for the Event POJO.
 */
public class EventTest {

    @Test
    public void testSettersAndGetters() {
        Event e = new Event();

        e.setId("abc123");
        e.setName("Swimming Lessons");
        e.setEventDetails("Kids learn freestyle and backstroke");
        e.setEventStartDate("2026-01-01");
        e.setEventEndDate("2026-02-01");
        e.setRegistrationStartDate("2025-11-13");
        e.setRegistrationEndDate("2025-12-30");
        e.setEntrantLimit(30);
        e.setOrganizer("org123");
        e.setPoster(null);
        e.setTag("sports kids swimming");
        e.setWaitingList("WaitingList");
        e.setLotteryWaitingList("LotteryList");
        e.setCancelledEntrants("Cancelled");
        e.setFinalEntrants("Final");

        assertEquals("abc123", e.getId());
        assertEquals("Swimming Lessons", e.getName());
        assertEquals("Kids learn freestyle and backstroke", e.getEventDetails());
        assertEquals("2026-01-01", e.getEventStartDate());
        assertEquals("2026-02-01", e.getEventEndDate());
        assertEquals("2025-11-13", e.getRegistrationStartDate());
        assertEquals("2025-12-30", e.getRegistrationEndDate());
        assertEquals(30, e.getEntrantLimit());
        assertEquals("org123", e.getOrganizer());
        assertNull(e.getPoster());
        assertEquals("sports kids swimming", e.getTag());
        assertEquals("WaitingList", e.getWaitingList());
        assertEquals("LotteryList", e.getLotteryWaitingList());
        assertEquals("Cancelled", e.getCancelledEntrants());
        assertEquals("Final", e.getFinalEntrants());
    }


}
