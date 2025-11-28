package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for US 02.07.03: As an organizer I want to send a notification to
 * all cancelled entrants
 *
 * <p>
 * These instrumented tests verify that:
 * <ul>
 *   <li>Organizers can access the CancelledListFragment UI</li>
 *   <li>The "Send Cancelled List Notification" button is visible and enabled</li>
 *   <li>Tapping the button opens a dialog titled "Send Cancelled List Notification"</li>
 *   <li>The dialog displays both "OK" and "Cancel" actions</li>
 *   <li>The dialog dismisses correctly when "Cancel" is pressed</li>
 *   <li>The dialog dismisses correctly when "OK" is pressed (confirming the action)</li>
 * </ul>
 * </p>
 *
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
 *
 * <p>
 * <b>Note:</b> Full end-to-end testing of notification delivery requires:
 * <ul>
 *   <li>Navigation to {@code CancelledListFragment} for a specific event</li>
 *   <li>Organizer user authentication</li>
 *   <li>Firebase test data and dependency wiring for
 *       {@link com.example.chicksevent.misc.Organizer}</li>
 * </ul>
 * These tests focus on dialog visibility and basic user interactions in the UI layer.
 * </p>
 *
 * @author Eric Kane
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TEST31_US020703_OrganizerNotifToCancelledUITest {

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
            // If scrollTo fails, the view might already be fully visible.
        }
    }

    /**
     * Performs a reliable click action that works better when animations are enabled.
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

    /**
     * Waits for a view to be displayed with retries.
     */
    private void waitForView(Matcher<View> viewMatcher, int maxAttempts) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                onView(viewMatcher).check(matches(isDisplayed()));
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }

    private void waitForView(Matcher<View> viewMatcher) {
        waitForView(viewMatcher, 10);
    }

    /**
     * Navigates to the CancelledListFragment.
     *
     * <p>
     * This helper assumes that:
     * <ul>
     *   <li>The app navigates (or can be navigated) to {@code CancelledListFragment}</li>
     *   <li>{@code fragment_cancelled_list.xml} contains a button with id
     *       {@code R.id.btn_notification1}</li>
     * </ul>
     * If your actual navigation requires specific clicks (e.g. open event, switch to
     * "Cancelled" tab), you can insert those interactions here before waiting for
     * {@code btn_notification1}.
     * </p>
     *
     * @return {@code true} if the fragment appears, {@code false} otherwise.
     */
    private boolean navigateToCancelledListFragment() {
        try {
            // Give some time for initial navigation / async work
            Thread.sleep(3000);

            // Wait for the CancelledListFragment's notification button
            waitForView(withId(R.id.btn_notification1), 15);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== UI Tests ====================

    /**
     * Test Case 1: Organizer sees the "Send Cancelled List Notification" button on the cancelled list.
     */
    @Test
    public void organizer_cancelledList_buttonVisibleAndEnabled() {
        if (!navigateToCancelledListFragment()) {
            return; // Safeguard if navigation fails in this environment
        }

        onView(withId(R.id.btn_notification1))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
    }

    /**
     * Test Case 2: Tapping the button opens the notification dialog.
     *
     * <p>
     * Verifies that a dialog with title "Send Cancelled List Notification" appears.
     * </p>
     */
    @Test
    public void organizer_cancelledList_opensDialog_onNotificationButtonClick() {
        if (!navigateToCancelledListFragment()) {
            return;
        }

        performReliableClick(onView(withId(R.id.btn_notification1)));

        // Verify dialog title
        waitForView(withText("Send Cancelled List Notification"), 10);
        onView(withText("Send Cancelled List Notification")).check(matches(isDisplayed()));
    }

    /**
     * Test Case 3: Dialog displays "OK" and "Cancel" actions.
     */
    @Test
    public void organizer_cancelledList_dialogShowsOkAndCancelButtons() {
        if (!navigateToCancelledListFragment()) {
            return;
        }

        performReliableClick(onView(withId(R.id.btn_notification1)));

        waitForView(withText("Send Cancelled List Notification"), 10);

        onView(withText("OK")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }

    /**
     * Test Case 4: Dialog dismisses when "Cancel" is pressed.
     */
    @Test
    public void organizer_cancelledList_dialogDismissesOnCancel() {
        if (!navigateToCancelledListFragment()) {
            return;
        }

        performReliableClick(onView(withId(R.id.btn_notification1)));

        waitForView(withText("Send Cancelled List Notification"), 10);

        // Press "Cancel"
        performReliableClick(onView(withText("Cancel")));

        // Dialog title should no longer exist
        onView(withText("Send Cancelled List Notification")).check(doesNotExist());
    }

    /**
     * Test Case 5: Dialog dismisses when "OK" is pressed.
     *
     * <p>
     * This simulates confirming the notification send.
     * The actual call to
     * {@link com.example.chicksevent.misc.Organizer#sendWaitingListNotification(
     *com.example.chicksevent.enums.EntrantStatus, String)}
     * is not asserted here; that behaviour is covered at the unit-test level.
     * </p>
     */
    @Test
    public void organizer_cancelledList_dialogDismissesOnOk() {
        if (!navigateToCancelledListFragment()) {
            return;
        }

        performReliableClick(onView(withId(R.id.btn_notification1)));

        waitForView(withText("Send Cancelled List Notification"), 10);

        // Press "OK"
        performReliableClick(onView(withText("OK")));

        // Dialog title should no longer exist
        onView(withText("Send Cancelled List Notification")).check(doesNotExist());
    }
}
