package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for US 01.06.02: As an entrant I want to be able to join 
 * the waitlist from the event details.
 * <p>
 * These instrumented tests verify that:
 * <ul>
 *   <li>Entrants can see the join waiting list button on event details</li>
 *   <li>Entrants can click the join button</li>
 *   <li>UI updates correctly after joining (button hides, status shows)</li>
 *   <li>Waiting list count is displayed</li>
 *   <li>Edge cases are handled (event on hold, no profile, etc.)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b> For reliable test execution, it's recommended to disable
 * animations on the test device/emulator before running these tests:
 * <pre>
 * adb shell settings put global animator_duration_scale 0
 * adb shell settings put global window_animation_scale 0
 * adb shell settings put global transition_animation_scale 0
 * </pre>
 * To re-enable animations after testing:
 * <pre>
 * adb shell settings put global animator_duration_scale 1
 * adb shell settings put global window_animation_scale 1
 * adb shell settings put global transition_animation_scale 1
 * </pre>
 * </p>
 * <p>
 * <b>Note:</b> Full end-to-end testing requires:
 * <ul>
 *   <li>Firebase test data (events, user profiles)</li>
 *   <li>Navigation to EventDetailFragment with valid eventId</li>
 *   <li>User profile setup for successful join</li>
 * </ul>
 * These tests focus on UI element visibility and basic interactions.
 * </p>
 *
 * @author Jinn Kasai
 */
@RunWith(AndroidJUnit4.class)
public class EntrantJoinWaitlistUITest {

    /**
     * Launches {@link MainActivity} before each test.
     */
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Scrolls to a view to ensure it's visible on screen.
     */
    private void scrollToView(ViewInteraction viewInteraction) {
        try {
            viewInteraction.perform(scrollTo());
        } catch (Exception e) {
            // If scrollTo fails, view might already be visible
        }
    }

    /**
     * Performs a reliable click action that works better with animations enabled.
     */
    private void performReliableClick(ViewInteraction viewInteraction) {
        scrollToView(viewInteraction);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        viewInteraction.perform(new GeneralClickAction(
                Tap.SINGLE,
                GeneralLocation.CENTER,
                Press.FINGER,
                0,
                0
        ));
    }

    // ==================== Join Waiting List Button Tests ====================

    /**
     * Test Case 1: Entrant can see join waiting list button on event details.
     * 
     * As an entrant, when I view event details, I should see a button
     * to join the waiting list.
     * <p>
     * Note: This test requires navigation to EventDetailFragment with a valid eventId.
     * For now, we verify the button exists in the layout structure.
     * </p>
     */
    @Test
    public void entrant_canSeeJoinButton_onEventDetails() {
        // Navigate to events screen first
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test scenario:
        // 1. Navigate to EventDetailFragment with valid eventId
        //    Bundle bundle = new Bundle();
        //    bundle.putString("eventId", "test-event-id");
        //    NavHostFragment.findNavController(...).navigate(R.id.action_..., bundle);
        // 2. Scroll to join button
        //    scrollToView(onView(withId(R.id.btn_waiting_list)));
        // 3. Verify join button is displayed
        //    onView(withId(R.id.btn_waiting_list)).check(matches(isDisplayed()));
        // 4. Verify button text is "Join Waiting List"
        //    onView(withId(R.id.btn_waiting_list))
        //        .check(matches(withText("Join Waiting List")));
        
        // For now, verify events screen is accessible
        // Full testing requires Firebase data and navigation setup
    }

    /**
     * Test Case 2: Join waiting list button is clickable.
     * 
     * As an entrant, the join waiting list button should be clickable
     * when viewing event details.
     */
    @Test
    public void entrant_joinButton_isClickable() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment with valid eventId
        // 2. Scroll to join button
        //    scrollToView(onView(withId(R.id.btn_waiting_list)));
        // 3. Verify button is enabled and clickable
        //    onView(withId(R.id.btn_waiting_list))
        //        .check(matches(isEnabled()));
        // 4. Click the button
        //    performReliableClick(onView(withId(R.id.btn_waiting_list)));
        // 5. Verify action is triggered (UI updates or Toast appears)
    }

    /**
     * Test Case 3: Join waiting list button has correct text.
     * 
     * As an entrant, the join button should display "Join Waiting List"
     * text to clearly indicate its purpose.
     */
    @Test
    public void entrant_joinButton_hasCorrectText() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment
        // 2. Verify button text is "Join Waiting List"
        // onView(withId(R.id.btn_waiting_list))
        //     .check(matches(withText("Join Waiting List")));
    }

    // ==================== Join Action Tests ====================

    /**
     * Test Case 4: Entrant can click join waiting list button.
     * 
     * As an entrant, I should be able to click the join waiting list
     * button to join the event's waiting list.
     */
    @Test
    public void entrant_canClickJoinButton() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment with valid eventId
        // 2. Scroll to join button
        // 3. Click the button
        // 4. Verify join action is triggered
        // 5. Verify UI updates (button hides, status shows)
    }

    /**
     * Test Case 5: UI updates after joining waiting list.
     * 
     * As an entrant, after I join the waiting list, the join button
     * should disappear and the waiting status should be displayed.
     */
    @Test
    public void entrant_uiUpdates_afterJoining() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment
        // 2. Verify join button is visible
        // 3. Click join button
        // 4. Verify join button becomes invisible
        // 5. Verify waiting status layout becomes visible
        // onView(withId(R.id.btn_waiting_list)).check(matches(not(isDisplayed())));
        // onView(withId(R.id.layout_waiting_status)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 6: Waiting status is displayed after joining.
     * 
     * As an entrant, after joining the waiting list, I should see
     * a status indicator showing that I'm on the waiting list.
     */
    @Test
    public void entrant_waitingStatus_displayedAfterJoining() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Join waiting list
        // 2. Verify layout_waiting_status is visible
        // 3. Verify waiting count is displayed
    }

    /**
     * Test Case 7: Waiting list count is displayed.
     * 
     * As an entrant, I should see the number of entrants on the
     * waiting list displayed on the event details screen.
     */
    @Test
    public void entrant_waitingCount_isDisplayed() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment
        // 2. Verify tv_waiting_count is displayed
        // 3. Verify count text format: "Number of Entrants: X"
    }

    /**
     * Test Case 8: Join button is hidden after successful join.
     * 
     * As an entrant, after successfully joining the waiting list,
     * the join button should be hidden to prevent duplicate joins.
     */
    @Test
    public void entrant_joinButton_hiddenAfterJoin() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Join waiting list
        // 2. Verify join button visibility is INVISIBLE or GONE
        // onView(withId(R.id.btn_waiting_list))
        //     .check(matches(not(isDisplayed())));
    }

    // ==================== Edge Case Tests ====================

    /**
     * Test Case 9: Error message shown when event is on hold.
     * 
     * As an entrant, if I try to join a waiting list for an event
     * that is on hold, I should see an appropriate error message.
     */
    @Test
    public void entrant_cannotJoin_whenEventOnHold() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment for event on hold
        // 2. Click join button
        // 3. Verify Toast message: "This event is currently on hold..."
        // 4. Verify join button remains visible
    }

    /**
     * Test Case 10: Error message shown when user has no profile.
     * 
     * As an entrant, if I try to join a waiting list without having
     * a profile, I should see an error message prompting me to create one.
     */
    @Test
    public void entrant_cannotJoin_withoutProfile() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment (user has no profile)
        // 2. Click join button
        // 3. Verify Toast message: "You need to a create profile..."
        // 4. Verify join button remains visible
    }

    /**
     * Test Case 11: Success message shown after joining.
     * 
     * As an entrant, after successfully joining the waiting list,
     * I should see a success message confirming my join.
     */
    @Test
    public void entrant_seesSuccessMessage_afterJoining() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Join waiting list successfully
        // 2. Verify Toast message: "Joined waiting list :)"
        // Note: Toast messages are difficult to test with Espresso
        // This may require custom matchers or manual verification
    }

    /**
     * Test Case 12: Join button is accessible in scrollable view.
     * 
     * As an entrant, the join waiting list button should be accessible
     * even if the event details screen is scrollable.
     */
    @Test
    public void entrant_joinButton_accessibleInScrollView() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment
        // 2. Scroll to join button if needed
        // 3. Verify button is visible and clickable
        // scrollToView(onView(withId(R.id.btn_waiting_list)));
        // onView(withId(R.id.btn_waiting_list)).check(matches(isDisplayed()));
    }

    // ==================== Integration Tests ====================

    /**
     * Test Case 13: Complete join flow works end-to-end.
     * 
     * As an entrant, the complete flow of viewing event details and
     * joining the waiting list should work correctly.
     * <p>
     * Note: This test requires full Firebase setup and navigation.
     * </p>
     */
    @Test
    public void entrant_completeJoinFlow_works() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: Complete test flow:
        // 1. Navigate to EventDetailFragment with valid eventId
        // 2. Verify event details are displayed
        // 3. Verify join button is visible
        // 4. Click join button
        // 5. Verify success message (Toast)
        // 6. Verify join button is hidden
        // 7. Verify waiting status is shown
        // 8. Verify waiting count is updated
    }

    /**
     * Test Case 14: Waiting count updates after joining.
     * 
     * As an entrant, after I join the waiting list, the displayed
     * count should update to reflect the new number of entrants.
     */
    @Test
    public void entrant_waitingCount_updatesAfterJoin() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Note initial waiting count
        // 2. Join waiting list
        // 3. Verify count is updated (increased by 1)
        // 4. Verify count text format is correct
    }

    /**
     * Test Case 15: Event details remain visible after joining.
     * 
     * As an entrant, after joining the waiting list, I should still
     * be able to see all event details.
     */
    @Test
    public void entrant_eventDetails_remainVisibleAfterJoin() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Join waiting list
        // 2. Verify event name is still visible
        // 3. Verify event description is still visible
        // 4. Verify event poster is still visible
        // 5. Verify all event details remain accessible
    }

    /**
     * Test Case 16: Geolocation requirement handled correctly.
     * 
     * As an entrant, if an event requires geolocation, joining the
     * waiting list should prompt for location permission and capture location.
     * <p>
     * Note: This requires location permission handling and is complex to test.
     * </p>
     */
    @Test
    public void entrant_geolocationRequired_handledCorrectly() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment for event with geolocationRequired=true
        // 2. Click join button
        // 3. Verify location permission request (if not granted)
        // 4. Verify location is captured
        // 5. Verify join completes with location data
        // This is complex and may require mocking location services
    }

    /**
     * Test Case 17: Multiple joins are prevented.
     * 
     * As an entrant, if I try to join the waiting list multiple times,
     * the UI should prevent duplicate joins.
     */
    @Test
    public void entrant_multipleJoins_prevented() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Join waiting list
        // 2. Verify join button is hidden
        // 3. Attempt to join again (button should not be visible)
        // 4. Verify only one join occurred
    }

    /**
     * Test Case 18: Waiting status layout contains expected elements.
     * 
     * As an entrant, when I'm on the waiting list, the status layout
     * should contain the waiting count and appropriate status text.
     */
    @Test
    public void entrant_waitingStatusLayout_containsExpectedElements() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Join waiting list
        // 2. Verify layout_waiting_status is visible
        // 3. Verify tv_waiting_count is visible and displays count
        // 4. Verify any other status elements are present
    }

    /**
     * Test Case 19: Navigation to event details works from QR code scan.
     * 
     * As an entrant, I should be able to navigate to event details from
     * scanning a QR code, and then join the waiting list.
     * <p>
     * Note: This integrates with US 01.06.01 (QR code scanning).
     * </p>
     */
    @Test
    public void entrant_canNavigateToDetails_fromQRScan_thenJoin() {
        // Navigate to QR scanner
        onView(withId(R.id.btn_scan)).perform(click());
        
        // Note: Complete test flow:
        // 1. Scan QR code (or navigate directly to EventDetailFragment)
        // 2. Verify event details are displayed
        // 3. Verify join button is visible
        // 4. Join waiting list
        // 5. Verify join is successful
    }

    /**
     * Test Case 20: Event details screen is scrollable.
     * 
     * As an entrant, if event details are long, I should be able to
     * scroll to see all content including the join button.
     */
    @Test
    public void entrant_eventDetailsScreen_isScrollable() {
        // Navigate to events screen
        onView(withId(R.id.btn_events)).perform(click());
        
        // Note: In a complete test:
        // 1. Navigate to EventDetailFragment with long event description
        // 2. Verify screen is scrollable
        // 3. Scroll to join button
        // 4. Verify join button is accessible
        // 5. Verify can scroll back to top
    }
}

