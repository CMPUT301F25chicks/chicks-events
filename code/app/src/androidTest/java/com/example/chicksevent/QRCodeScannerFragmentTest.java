package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test for {@link com.example.chicksevent.fragment.QRCodeScannerFragment} in the Chicksevent app.
 * <p>
 * This test verifies that:
 * <ul>
 *   <li>UI elements in the QR scanner fragment are displayed correctly</li>
 *   <li>Navigation buttons function as expected</li>
 *   <li>The start scan button is available for manual scanning</li>
 *   <li>Auto-start behavior is triggered when fragment opens (permission-dependent)</li>
 * </ul>
 * </p>
 *
 * @author Jinn Kasai
 */
@RunWith(AndroidJUnit4.class)
public class QRCodeScannerFragmentTest {

    /**
     * Launches {@link MainActivity} before each test.
     */
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Verifies that all main UI elements in the QRCodeScannerFragment are displayed
     * after navigating to the scanner fragment.
     */
    @Test
    public void testUIElementsDisplayed() {
        // Navigate to QR scanner fragment
        onView(withId(R.id.btn_scan)).perform(click());
        
        // Verify instruction text is displayed
        onView(withId(R.id.tv_instructions)).check(matches(isDisplayed()));
        onView(withText("Point camera at QR code")).check(matches(isDisplayed()));
        
        // Verify start scan button is displayed
        onView(withId(R.id.btn_start_scan)).check(matches(isDisplayed()));
        onView(withText("START SCANNING")).check(matches(isDisplayed()));
    }

    /**
     * Tests navigation buttons in the QR scanner fragment.
     * <p>
     * Verifies that all bottom navigation buttons are displayed and functional.
     * </p>
     */
    @Test
    public void testNavigationButtons() {
        // Navigate to QR scanner fragment
        onView(withId(R.id.btn_scan)).perform(click());
        
        // Verify all navigation buttons are displayed
        onView(withId(R.id.btn_notification)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_events)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_scan)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_addEvent)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_profile)).check(matches(isDisplayed()));
    }

    /**
     * Tests navigation from QR scanner to other fragments.
     * <p>
     * Clicks navigation buttons and verifies the correct fragments are shown.
     * </p>
     */
    @Test
    public void testNavigationFromScanner() {
        // Navigate to QR scanner fragment
        onView(withId(R.id.btn_scan)).perform(click());
        
        // Navigate to Events fragment
        onView(withId(R.id.btn_events)).perform(click());
        onView(withId(R.id.button_row)).check(matches(isDisplayed())); // EventFragment UI
        
        // Navigate back to scanner
        onView(withId(R.id.btn_scan)).perform(click());
        
        // Navigate to Notification fragment
        onView(withId(R.id.btn_notification)).perform(click());
        onView(withId(R.id.recycler_notifications)).check(matches(isDisplayed())); // NotificationFragment UI
    }

    /**
     * Tests that the start scan button is clickable and available for manual scanning.
     * <p>
     * This button should be available even if auto-start is enabled, allowing users
     * to manually restart scanning if needed.
     * </p>
     */
    @Test
    public void testStartScanButtonAvailable() {
        // Navigate to QR scanner fragment
        onView(withId(R.id.btn_scan)).perform(click());
        
        // Verify start scan button is displayed and clickable
        onView(withId(R.id.btn_start_scan)).check(matches(isDisplayed()));
        
        // Button should be clickable (this will trigger permission request or scanner if permission granted)
        // Note: Actual scanner launch depends on camera permission, which may vary in test environment
        onView(withId(R.id.btn_start_scan)).perform(click());
        
        // After clicking, the button should still be visible (scanner may open in separate activity)
        // or permission dialog may appear
        onView(withId(R.id.btn_start_scan)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the scanner fragment can be accessed from the main navigation.
     * <p>
     * Verifies the fragment is reachable and displays correctly when navigated to.
     * </p>
     */
    @Test
    public void testFragmentAccessibility() {
        // Start from any fragment and navigate to scanner
        onView(withId(R.id.btn_scan)).perform(click());
        
        // Verify we're on the scanner fragment by checking for its unique elements
        onView(withId(R.id.tv_instructions)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_start_scan)).check(matches(isDisplayed()));
    }
}

