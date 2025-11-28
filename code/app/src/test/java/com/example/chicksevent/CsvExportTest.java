package com.example.chicksevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.chicksevent.fragment_org.CsvExportHelper; // Import the new helper
import org.junit.Test;

/**
 * A PURE and SIMPLE unit test for the CSV export URL generation logic.
 * This test validates the CsvExportHelper class directly.
 * It has NO Android dependencies and NO Mockito.
 * @author Juan Rea
 */
public class CsvExportTest {

    @Test
    public void buildUrl_withValidEventId_returnsCorrectUrl() {
        // Arrange: Define the input and the expected correct output
        String eventId = "testEvent123";
        String expectedUrl = "https://us-central1-listycity-friedchicken.cloudfunctions.net/exportFinalEntrants?eventId=testEvent123";

        // Act: Call the method being tested
        String actualUrl = CsvExportHelper.buildUrl(eventId);

        // Assert: Check that the actual output matches the expected output
        assertEquals("The generated URL should be correct for a valid event ID.", expectedUrl, actualUrl);
    }

    @Test
    public void buildUrl_withNullEventId_returnsNull() {
        // Arrange: The input is null
        // Act: Call the method with the null input
        String actualUrl = CsvExportHelper.buildUrl(null);

        // Assert: Check that the output is null, as expected
        assertNull("The URL should be null when the event ID is null.", actualUrl);
    }

    @Test
    public void buildUrl_withEmptyEventId_returnsNull() {
        // Arrange: The input is an empty string
        // Act: Call the method with the empty input
        String actualUrl = CsvExportHelper.buildUrl("");

        // Assert: Check that the output is null, as expected
        assertNull("The URL should be null when the event ID is empty.", actualUrl);
    }
}
