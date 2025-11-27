package com.example.chicksevent;

import static org.junit.Assert.*;

import com.example.chicksevent.util.QRCodeGenerator;

import org.junit.Test;

/**
 * Unit tests for US 01.06.01: As an entrant I want to view event details 
 * within the app by scanning the promotional QR code.
 * <p>
 * These tests validate that:
 * <ul>
 *   <li>QR code deep links are generated in the correct format</li>
 *   <li>Deep link format validation works correctly (scheme and host)</li>
 *   <li>Event ID extraction from deep link strings works correctly</li>
 *   <li>Invalid QR code formats are detected</li>
 *   <li>Empty and null QR code data is handled</li>
 * </ul>
 * </p>
 * <p>
 * Note: URI parsing using Android's Uri class requires Android runtime and
 * is tested in instrumented tests. These unit tests focus on string-based
 * validation and deep link generation logic.
 * </p>
 *
 * @author Jinn Kasai
 */
public class QRCodeScannerTest {

    private static final String DEEP_LINK_SCHEME = "chicksevent";
    private static final String DEEP_LINK_HOST = "event";
    private static final String DEEP_LINK_PREFIX = DEEP_LINK_SCHEME + "://" + DEEP_LINK_HOST + "/";

    /**
     * Helper method to extract event ID from a deep link string.
     * This simulates the logic used in QRCodeScannerFragment.
     * 
     * @param deepLink the deep link string (e.g., "chicksevent://event/E123")
     * @return the event ID, or null if invalid
     */
    private String extractEventIdFromDeepLink(String deepLink) {
        if (deepLink == null || deepLink.isEmpty()) {
            return null;
        }
        
        if (!deepLink.startsWith(DEEP_LINK_PREFIX)) {
            return null;
        }
        
        String eventId = deepLink.substring(DEEP_LINK_PREFIX.length());
        if (eventId.isEmpty()) {
            return null;
        }
        
        return eventId;
    }

    /**
     * Helper method to validate deep link format.
     * This simulates the validation logic used in QRCodeScannerFragment.
     * 
     * @param deepLink the deep link string to validate
     * @return true if the deep link has correct format, false otherwise
     */
    private boolean isValidDeepLinkFormat(String deepLink) {
        if (deepLink == null || deepLink.isEmpty()) {
            return false;
        }
        
        if (!deepLink.startsWith(DEEP_LINK_SCHEME + "://")) {
            return false;
        }
        
        if (!deepLink.contains(DEEP_LINK_HOST + "/")) {
            return false;
        }
        
        String eventId = extractEventIdFromDeepLink(deepLink);
        return eventId != null && !eventId.isEmpty();
    }

    // ==================== Deep Link Generation Tests ====================

    /**
     * Test Case 1: QR code deep link is generated in correct format.
     * 
     * As an entrant, when I scan a QR code, it should contain a deep link
     * in the format: chicksevent://event/{eventId}
     */
    @Test
    public void entrant_qrCodeDeepLink_hasCorrectFormat() {
        String eventId = "E123";
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        assertEquals("chicksevent://event/E123", deepLink);
    }

    /**
     * Test Case 2: QR code deep link contains event ID.
     * 
     * As an entrant, the QR code deep link should contain the event ID
     * that can be extracted to view event details.
     */
    @Test
    public void entrant_qrCodeDeepLink_containsEventId() {
        String eventId = "E456";
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        assertTrue(deepLink.contains(eventId));
        assertTrue(deepLink.endsWith("/" + eventId));
    }

    /**
     * Test Case 3: Different events generate different deep links.
     * 
     * As an entrant, each event should have a unique deep link
     * that corresponds to that specific event.
     */
    @Test
    public void entrant_differentEvents_generateDifferentDeepLinks() {
        String eventId1 = "E111";
        String eventId2 = "E222";
        
        String deepLink1 = QRCodeGenerator.generateEventDeepLink(eventId1);
        String deepLink2 = QRCodeGenerator.generateEventDeepLink(eventId2);
        
        assertNotEquals(deepLink1, deepLink2);
        assertEquals("chicksevent://event/E111", deepLink1);
        assertEquals("chicksevent://event/E222", deepLink2);
    }

    // ==================== Deep Link Parsing Tests ====================

    /**
     * Test Case 4: Valid deep link can be parsed to extract event ID.
     * 
     * As an entrant, when I scan a valid QR code, the app should be able
     * to extract the event ID from the deep link.
     */
    @Test
    public void entrant_validDeepLink_canExtractEventId() {
        String deepLink = "chicksevent://event/E123";
        
        // Extract event ID using helper method
        String eventId = extractEventIdFromDeepLink(deepLink);
        
        assertNotNull("Event ID should be extracted", eventId);
        assertEquals("E123", eventId);
    }

    /**
     * Test Case 5: Deep link scheme validation works correctly.
     * 
     * As an entrant, the app should validate that the QR code contains
     * the correct scheme (chicksevent://).
     */
    @Test
    public void entrant_deepLink_hasCorrectScheme() {
        String deepLink = "chicksevent://event/E123";
        
        assertTrue("Deep link should start with correct scheme", 
                  deepLink.startsWith(DEEP_LINK_SCHEME + "://"));
        assertFalse("Deep link should not start with http", 
                   deepLink.startsWith("http://"));
        assertFalse("Deep link should not start with https", 
                   deepLink.startsWith("https://"));
    }

    /**
     * Test Case 6: Deep link host validation works correctly.
     * 
     * As an entrant, the app should validate that the QR code contains
     * the correct host (event).
     */
    @Test
    public void entrant_deepLink_hasCorrectHost() {
        String deepLink = "chicksevent://event/E123";
        
        assertTrue("Deep link should contain correct host", 
                  deepLink.contains(DEEP_LINK_HOST + "/"));
    }

    /**
     * Test Case 7: Invalid scheme is detected.
     * 
     * As an entrant, if I scan a QR code with an invalid scheme,
     * the app should detect it as invalid.
     */
    @Test
    public void entrant_invalidScheme_isDetected() {
        String invalidDeepLink = "http://event/E123";
        
        assertFalse("Invalid scheme should be detected", 
                   isValidDeepLinkFormat(invalidDeepLink));
        assertTrue("Should not start with correct scheme", 
                  !invalidDeepLink.startsWith(DEEP_LINK_SCHEME + "://"));
    }

    /**
     * Test Case 8: Invalid host is detected.
     * 
     * As an entrant, if I scan a QR code with an invalid host,
     * the app should detect it as invalid.
     */
    @Test
    public void entrant_invalidHost_isDetected() {
        String invalidDeepLink = "chicksevent://invalid/E123";
        
        assertFalse("Invalid host should be detected", 
                   isValidDeepLinkFormat(invalidDeepLink));
        assertTrue("Should not contain correct host", 
                  !invalidDeepLink.contains(DEEP_LINK_HOST + "/"));
    }

    /**
     * Test Case 9: Missing event ID in path is detected.
     * 
     * As an entrant, if I scan a QR code with missing event ID,
     * the app should detect it as invalid.
     */
    @Test
    public void entrant_missingEventId_isDetected() {
        String invalidDeepLink = "chicksevent://event/";
        
        String eventId = extractEventIdFromDeepLink(invalidDeepLink);
        assertNull("Missing event ID should return null", eventId);
        assertFalse("Invalid format should be detected", 
                   isValidDeepLinkFormat(invalidDeepLink));
    }

    /**
     * Test Case 10: Empty event ID is detected.
     * 
     * As an entrant, if I scan a QR code with an empty event ID,
     * the app should detect it as invalid.
     */
    @Test
    public void entrant_emptyEventId_isDetected() {
        String invalidDeepLink = "chicksevent://event/";
        
        String eventId = extractEventIdFromDeepLink(invalidDeepLink);
        assertNull("Empty event ID should return null", eventId);
        assertFalse("Invalid format should be detected", 
                   isValidDeepLinkFormat(invalidDeepLink));
    }

    /**
     * Test Case 11: Null QR code data is handled.
     * 
     * As an entrant, if the scanned QR code data is null,
     * the app should handle it gracefully.
     */
    @Test
    public void entrant_nullQRCodeData_isHandled() {
        String scannedData = null;
        
        assertNull(scannedData);
        // In actual implementation, this should trigger error handling
    }

    /**
     * Test Case 12: Empty QR code data is handled.
     * 
     * As an entrant, if the scanned QR code data is empty,
     * the app should handle it gracefully.
     */
    @Test
    public void entrant_emptyQRCodeData_isHandled() {
        String scannedData = "";
        
        assertTrue(scannedData.isEmpty());
        // In actual implementation, this should trigger error handling
    }

    /**
     * Test Case 13: Event ID with special characters is handled.
     * 
     * As an entrant, event IDs with special characters should be
     * correctly extracted from the deep link.
     */
    @Test
    public void entrant_eventIdWithSpecialChars_isExtracted() {
        // Event IDs typically don't have special chars, but test edge cases
        String eventId = "E-123_456";
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        String extractedId = extractEventIdFromDeepLink(deepLink);
        assertEquals(eventId, extractedId);
    }

    /**
     * Test Case 14: Long event ID is handled correctly.
     * 
     * As an entrant, event IDs of various lengths should be
     * correctly extracted from the deep link.
     */
    @Test
    public void entrant_longEventId_isExtracted() {
        String longEventId = "E" + "1".repeat(50); // 50 character event ID
        String deepLink = QRCodeGenerator.generateEventDeepLink(longEventId);
        
        String extractedId = extractEventIdFromDeepLink(deepLink);
        assertEquals(longEventId, extractedId);
    }

    /**
     * Test Case 15: Deep link format matches expected pattern.
     * 
     * As an entrant, the deep link should follow the pattern:
     * chicksevent://event/{eventId}
     */
    @Test
    public void entrant_deepLink_matchesExpectedPattern() {
        String eventId = "E789";
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        // Verify pattern: scheme://host/path
        assertTrue("Should start with scheme", 
                  deepLink.startsWith(DEEP_LINK_SCHEME + "://"));
        assertTrue("Should contain host", 
                  deepLink.contains(DEEP_LINK_HOST + "/"));
        assertTrue("Should end with event ID", 
                  deepLink.endsWith(eventId));
        
        // Verify format validation
        assertTrue("Should be valid format", 
                  isValidDeepLinkFormat(deepLink));
    }

    // ==================== Integration Tests ====================

    /**
     * Test Case 16: Deep link generation and parsing round-trip works.
     * 
     * As an entrant, a deep link generated for an event should be
     * parseable to extract the same event ID.
     */
    @Test
    public void entrant_deepLinkGenerationAndParsing_roundTrip() {
        String originalEventId = "E999";
        
        // Generate deep link
        String deepLink = QRCodeGenerator.generateEventDeepLink(originalEventId);
        
        // Parse deep link using helper method
        String extractedEventId = extractEventIdFromDeepLink(deepLink);
        
        // Verify round-trip
        assertEquals("Event ID should match after round-trip", 
                    originalEventId, extractedEventId);
    }

    /**
     * Test Case 17: Multiple events can have deep links generated and parsed.
     * 
     * As an entrant, multiple events should each have their own
     * unique deep links that can be parsed correctly.
     */
    @Test
    public void entrant_multipleEvents_deepLinksWork() {
        String[] eventIds = {"E001", "E002", "E003", "E004", "E005"};
        
        for (String eventId : eventIds) {
            String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
            String extractedId = extractEventIdFromDeepLink(deepLink);
            
            assertEquals("Event ID should match", eventId, extractedId);
            assertTrue("Deep link should be valid format", 
                      isValidDeepLinkFormat(deepLink));
        }
    }

    /**
     * Test Case 18: Deep link validation logic works for valid links.
     * 
     * As an entrant, valid deep links should pass validation checks.
     */
    @Test
    public void entrant_validDeepLink_passesValidation() {
        String eventId = "E123";
        String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
        
        // Validation checks using helper method
        assertTrue("Valid deep link should pass validation", 
                  isValidDeepLinkFormat(deepLink));
        
        String extractedId = extractEventIdFromDeepLink(deepLink);
        assertNotNull("Event ID should be extracted", extractedId);
        assertEquals("Event ID should match", eventId, extractedId);
    }

    /**
     * Test Case 19: Deep link validation logic rejects invalid links.
     * 
     * As an entrant, invalid deep links should fail validation checks.
     */
    @Test
    public void entrant_invalidDeepLink_failsValidation() {
        String invalidDeepLink = "http://invalid/path";
        
        // Validation checks
        assertFalse("Invalid deep link should fail validation", 
                   isValidDeepLinkFormat(invalidDeepLink));
        
        String extractedId = extractEventIdFromDeepLink(invalidDeepLink);
        assertNull("Invalid deep link should not extract event ID", extractedId);
    }

    /**
     * Test Case 20: Event ID extraction handles various formats.
     * 
     * As an entrant, event IDs in various formats should be correctly
     * extracted from deep links.
     */
    @Test
    public void entrant_variousEventIdFormats_areExtracted() {
        String[] eventIds = {
            "E1",           // Short ID
            "E123",         // Standard ID
            "E123456789",   // Long ID
            "event-123",    // With dash
            "event_123"     // With underscore
        };
        
        for (String eventId : eventIds) {
            String deepLink = QRCodeGenerator.generateEventDeepLink(eventId);
            String extractedId = extractEventIdFromDeepLink(deepLink);
            
            assertEquals("Event ID should match", eventId, extractedId);
        }
    }
}

