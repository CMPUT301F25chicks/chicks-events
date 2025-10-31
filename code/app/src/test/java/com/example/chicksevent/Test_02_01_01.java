package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** US 02.01.01: "As an organizer I want to create a new event" */
public class Test_02_01_01 {

    // Pure JVM fake – does NOT extend FirebaseService
    static class FakeStore implements EntryStore {
        Map<String, Object> lastData;
        String keyToReturn = "fakeKey123";

        @Override
        public String addEntry(HashMap<String, Object> data) {
            this.lastData = data;
            return keyToReturn;
        }
    }

    @Test
    public void createEvent_buildsExpectedMap_andReturnsKey() throws Exception {

        // ✅ create the fake store (no Firebase, pure JVM)
        FakeStore fake = new FakeStore();

        // ✅ pass the fake into Organizer to avoid calling Firebase
        Organizer organizer = new Organizer(fake);

        // prepare test dates
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date start = fmt.parse("2025-11-13");
        Date end   = fmt.parse("2025-12-30");

        // act
        String key = organizer.createEvent(
                "jlk389fha98wh34awe",
                "TungTungTungSahur",
                "in this event we will tung tung tung sahur music",
                30, start, end, null
        );

        // assertions
        assertEquals("fakeKey123", key);
        Map<String, Object> data = fake.lastData;

        assertEquals("TungTungTungSahur", data.get("name"));
        assertEquals("jlk389fha98wh34awe", data.get("organizer"));
        assertEquals("in this event we will tung tung tung sahur music", data.get("eventDetails"));
        assertEquals(30, data.get("entrantLimit"));
        assertEquals("2025-11-13", data.get("registrationStartDate"));
        assertEquals("2025-12-30", data.get("registrationEndDate"));
        assertEquals("null", data.get("poster"));
        assertEquals("WaitingList", data.get("waitingList"));
        assertEquals("final list les go", data.get("finalEntrants"));
        assertEquals("List of Cancelled Entrnats (accepted by lottery but cancelled)", data.get("cancelledEntrants"));
        assertEquals("list of entrants in waiting list that got accepted by lottery", data.get("lotteryWaitingList"));
    }

}
