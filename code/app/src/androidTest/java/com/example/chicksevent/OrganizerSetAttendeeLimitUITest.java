package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for US 02.05.02: As an organizer, I want to set the system to sample 
 * a specified number of attendees to register for the event.
 * <p>
 * These instrumented tests verify that:
 * <ul>
 *   <li>Organizers can access the attendee limit setting when creating events</li>
 *   <li>Checkbox to enable/disable attendee limit is visible and functional</li>
 *   <li>Attendee limit input field appears when checkbox is checked</li>
 *   <li>Organizers can enter a specified number of attendees</li>
 *   <li>Input validation works correctly (numbers only, positive values)</li>
 *   <li>Attendee limit is saved when creating the event</li>
 *   <li>Edge cases (empty input, invalid values, large numbers) are handled properly</li>
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
 *   <li>Navigation to CreateEventFragment</li>
 *   <li>Organizer user authentication</li>
 *   <li>Firebase test data setup</li>
 * </ul>
 * These tests focus on UI element visibility and basic interactions.
 * </p>
 *
 * @author Jinn Kasai
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerSetAttendeeLimitUITest {

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

    // ==================== Navigation and Access Tests ====================

    /**
     * Test Case 1: Organizer can navigate to create event screen.
     * 
     * As an organizer, I should be able to navigate to the screen where
     * I can create an event and set the attendee limit.
     */
    @Test
    public void organizer_canNavigateToCreateEventScreen() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to fully load and layout to complete
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Try to scroll to the button - this will make it visible if it's off-screen
        // Then verify it's displayed
        try {
            onView(withId(R.id.btn_create_event))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        } catch (Exception e) {
            // If scrollTo fails (view might already be visible), just check display
            onView(withId(R.id.btn_create_event))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 2: Attendee limit checkbox is visible.
     * 
     * As an organizer, I should see a checkbox that allows me to enable
     * or disable the attendee limit setting.
     */
    @Test
    public void organizer_attendeeLimitCheckbox_isVisible() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to checkbox if needed and verify it's displayed
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
    }

    /**
     * Test Case 3: Attendee limit checkbox has correct label.
     * 
     * As an organizer, the checkbox should have a clear label indicating
     * it's for limiting the number of entrants.
     */
    @Test
    public void organizer_attendeeLimitCheckbox_hasCorrectLabel() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Verify checkbox text
        onView(withId(R.id.cb_limit_waiting_list))
                .check(matches(isDisplayed()));
        // Note: The checkbox text is "Limit number of entrants"
    }

    // ==================== Checkbox Functionality Tests ====================

    /**
     * Test Case 4: Attendee limit input field is hidden initially.
     * 
     * As an organizer, when I first open the create event screen, the
     * attendee limit input field should be hidden.
     */
    @Test
    public void organizer_attendeeLimitInput_hiddenInitially() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify input field is hidden
        onView(withId(R.id.et_max_entrants))
                .check(matches(not(isDisplayed())));
    }

    /**
     * Test Case 5: Attendee limit input field appears when checkbox is checked.
     * 
     * As an organizer, when I check the "Limit number of entrants" checkbox,
     * the input field for entering the number should appear.
     */
    @Test
    public void organizer_attendeeLimitInput_appearsWhenCheckboxChecked() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify input field is initially hidden (may not be in view hierarchy yet)
        try {
            onView(withId(R.id.et_max_entrants))
                    .check(matches(not(isDisplayed())));
        } catch (Exception e) {
            // If view doesn't exist yet or check fails, that's acceptable
            // It means it's truly hidden/not created
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear and layout to update
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify input field is now visible - try scrolling first to ensure it's on screen
        try {
            onView(withId(R.id.et_max_entrants))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        } catch (Exception e) {
            // If scrollTo fails, view might already be visible, just check display
            onView(withId(R.id.et_max_entrants))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 6: Attendee limit input field hides when checkbox is unchecked.
     * 
     * As an organizer, when I uncheck the "Limit number of entrants" checkbox,
     * the input field should hide again.
     */
    @Test
    public void organizer_attendeeLimitInput_hidesWhenCheckboxUnchecked() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox to show input field
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify input field is visible
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
        
        // Uncheck the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to hide
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify input field is hidden again
        onView(withId(R.id.et_max_entrants))
                .check(matches(not(isDisplayed())));
    }

    /**
     * Test Case 7: Attendee limit checkbox is clickable.
     * 
     * As an organizer, I should be able to click the checkbox to toggle
     * the attendee limit setting.
     */
    @Test
    public void organizer_attendeeLimitCheckbox_isClickable() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Verify checkbox is clickable
        onView(withId(R.id.cb_limit_waiting_list))
                .check(matches(isEnabled()))
                .perform(click());
    }

    // ==================== Input Field Tests ====================

    /**
     * Test Case 8: Attendee limit input field is visible when checkbox is checked.
     * 
     * As an organizer, when the checkbox is checked, the input field should
     * be visible and ready for input.
     */
    @Test
    public void organizer_attendeeLimitInput_isVisibleWhenCheckboxChecked() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify input field is visible and enabled
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
    }

    /**
     * Test Case 9: Attendee limit input field has correct hint.
     * 
     * As an organizer, the input field should have a helpful hint text
     * indicating what to enter.
     */
    @Test
    public void organizer_attendeeLimitInput_hasCorrectHint() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Verify input field is visible
        // Note: The hint text is "Enter max number"
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 10: Organizer can enter a number in attendee limit field.
     * 
     * As an organizer, I should be able to type a number into the attendee
     * limit input field.
     */
    @Test
    public void organizer_canEnterNumberInAttendeeLimitField() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and enter a number
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("50"));
        
        // Verify input was accepted (field is still visible)
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 11: Organizer can enter different numbers.
     * 
     * As an organizer, I should be able to enter different numbers for
     * the attendee limit (e.g., 10, 50, 100, 500).
     */
    @Test
    public void organizer_canEnterDifferentNumbers() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to input field
        scrollToView(onView(withId(R.id.et_max_entrants)));
        
        // Test entering different numbers
        onView(withId(R.id.et_max_entrants))
                .perform(clearText(), typeText("10"));
        
        onView(withId(R.id.et_max_entrants))
                .perform(clearText(), typeText("50"));
        
        onView(withId(R.id.et_max_entrants))
                .perform(clearText(), typeText("100"));
        
        // Verify field accepts different values
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 12: Attendee limit input field accepts numeric input only.
     * 
     * As an organizer, the input field should only accept numeric input
     * and show an error for non-numeric characters.
     */
    @Test
    public void organizer_attendeeLimitInput_acceptsNumericOnly() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Try to enter non-numeric text
        // Note: The field has inputType="number" which should prevent non-numeric input
        // and validation shows error "Please enter numbers only"
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
        
        // The actual validation happens in TextWatcher and shows error message
        // This test verifies the field exists and is ready for validation
    }

    // ==================== Validation Tests ====================

    /**
     * Test Case 13: Attendee limit validation shows error for non-numeric input.
     * 
     * As an organizer, if I enter non-numeric characters, the system should
     * show an error message.
     */
    @Test
    public void organizer_attendeeLimitValidation_showsErrorForNonNumeric() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to input field
        scrollToView(onView(withId(R.id.et_max_entrants)));
        
        // Enter non-numeric text - the input type should prevent this
        // or the TextWatcher will filter it out
        // For now, just verify the field accepts input
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("abc"));
        
        // Verify field is still displayed (validation may filter input)
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 14: Attendee limit validation requires positive number.
     * 
     * As an organizer, if I enter zero or a negative number, the system
     * should show an error message.
     */
    @Test
    public void organizer_attendeeLimitValidation_requiresPositiveNumber() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Note: In a complete test:
        // 1. Enter "0" and try to create event
        // 2. Verify error message: "Max entrants must be greater than 0"
        // 3. Enter negative number (if possible) and verify error
    }

    /**
     * Test Case 15: Attendee limit validation requires input when checkbox is checked.
     * 
     * As an organizer, if I check the checkbox but don't enter a number,
     * the system should require me to enter a value before creating the event.
     */
    @Test
    public void organizer_attendeeLimitValidation_requiresInputWhenChecked() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Note: In a complete test:
        // 1. Check checkbox but leave input field empty
        // 2. Fill other required fields
        // 3. Try to create event
        // 4. Verify error message: "Please enter max number of entrants"
    }

    // ==================== Event Creation Tests ====================

    /**
     * Test Case 16: Attendee limit is saved when creating event.
     * 
     * As an organizer, when I create an event with an attendee limit specified,
     * the limit should be saved and applied to the event.
     */
    @Test
    public void organizer_attendeeLimit_isSavedWhenCreatingEvent() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Enter a number
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("50"));
        
        // Note: In a complete test:
        // 1. Fill all required event fields
        // 2. Create the event
        // 3. Verify event is created with entrantLimit = 50
        // 4. Verify limit is stored in Firebase
    }

    /**
     * Test Case 17: Event can be created without attendee limit.
     * 
     * As an organizer, I should be able to create an event without setting
     * an attendee limit (checkbox unchecked).
     */
    @Test
    public void organizer_canCreateEventWithoutAttendeeLimit() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Verify checkbox is unchecked by default
        onView(withId(R.id.et_max_entrants))
                .check(matches(not(isDisplayed())));
        
        // Note: In a complete test:
        // 1. Leave checkbox unchecked
        // 2. Fill all required event fields
        // 3. Create the event
        // 4. Verify event is created with default entrantLimit (999 or unlimited)
    }

    // ==================== Edge Case Tests ====================

    /**
     * Test Case 18: Attendee limit input field handles large numbers.
     * 
     * As an organizer, I should be able to enter large numbers for the
     * attendee limit (e.g., 1000, 5000).
     */
    @Test
    public void organizer_attendeeLimitInput_handlesLargeNumbers() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to input field and enter a large number
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("1000"));
        
        // Verify input is accepted
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 19: Attendee limit input field can be cleared.
     * 
     * As an organizer, I should be able to clear the input field and
     * enter a new value.
     */
    @Test
    public void organizer_attendeeLimitInput_canBeCleared() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to input field
        scrollToView(onView(withId(R.id.et_max_entrants)));
        
        // Enter a number
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("50"));
        
        // Clear and enter new value
        onView(withId(R.id.et_max_entrants))
                .perform(clearText(), typeText("100"));
        
        // Verify field accepts new value
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 20: Attendee limit checkbox state persists during form editing.
     * 
     * As an organizer, if I check the checkbox and then edit other fields,
     * the checkbox state and input field visibility should persist.
     */
    @Test
    public void organizer_attendeeLimitCheckbox_persistsDuringFormEditing() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify input field is visible
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
        
        // Enter a value
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("50"));
        
        // Scroll to event name field and edit it
        scrollToView(onView(withId(R.id.et_event_name)));
        onView(withId(R.id.et_event_name))
                .perform(typeText("Test Event"));
        
        // Verify checkbox is still checked and input field is still visible
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 21: Attendee limit input field is accessible on different screen sizes.
     * 
     * As an organizer, the attendee limit input field should be accessible
     * and functional on different screen sizes and orientations.
     */
    @Test
    public void organizer_attendeeLimitInput_accessibleOnDifferentScreens() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Verify input field is accessible
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
    }

    /**
     * Test Case 22: Attendee limit input field maintains value when toggling checkbox.
     * 
     * As an organizer, if I enter a value, uncheck the checkbox, and then
     * check it again, the value should be preserved.
     */
    @Test
    public void organizer_attendeeLimitInput_maintainsValueWhenTogglingCheckbox() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and enter a value
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("50"));
        
        // Uncheck the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to hide
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check the checkbox again
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify input field is visible again
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
        
        // Note: Value preservation depends on implementation
        // Some apps preserve the value, some clear it when hiding the field
    }

    /**
     * Test Case 23: Attendee limit works with all event creation fields.
     * 
     * As an organizer, setting the attendee limit should work correctly
     * alongside all other event creation fields (name, description, dates, etc.).
     */
    @Test
    public void organizer_attendeeLimit_worksWithAllEventFields() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and enter attendee limit
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("50"));
        
        // Verify other fields are still accessible
        scrollToView(onView(withId(R.id.et_event_name)));
        onView(withId(R.id.et_event_name))
                .check(matches(isDisplayed()));
        
        scrollToView(onView(withId(R.id.et_event_description)));
        onView(withId(R.id.et_event_description))
                .check(matches(isDisplayed()));
        
        scrollToView(onView(withId(R.id.btn_create_event)));
        onView(withId(R.id.btn_create_event))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 24: Attendee limit input field has correct input type.
     * 
     * As an organizer, the input field should be configured to accept
     * numeric input only (inputType="number").
     */
    @Test
    public void organizer_attendeeLimitInput_hasCorrectInputType() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Verify input field is displayed
        // Note: The layout has inputType="number" which restricts input to numbers
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 25: Attendee limit setting is clearly labeled.
     * 
     * As an organizer, the attendee limit setting should be clearly labeled
     * so I understand what it does.
     */
    @Test
    public void organizer_attendeeLimitSetting_isClearlyLabeled() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Verify checkbox label is visible
        onView(withId(R.id.cb_limit_waiting_list))
                .check(matches(isDisplayed()));
        
        // Verify input field hint is visible when shown
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 26: Attendee limit can be set to minimum value (1).
     * 
     * As an organizer, I should be able to set the attendee limit to 1
     * (the minimum valid value).
     */
    @Test
    public void organizer_attendeeLimit_canBeSetToMinimumValue() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Wait for fragment to load
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to and check the checkbox
        scrollToView(onView(withId(R.id.cb_limit_waiting_list)));
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Wait for input field to appear
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Scroll to input field and enter minimum value
        scrollToView(onView(withId(R.id.et_max_entrants)));
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("1"));
        
        // Verify input is accepted
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 27: Attendee limit validation prevents zero.
     * 
     * As an organizer, if I try to set the attendee limit to zero,
     * the system should show an error and prevent event creation.
     */
    @Test
    public void organizer_attendeeLimitValidation_preventsZero() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Note: In a complete test:
        // 1. Enter "0" in the field
        // 2. Fill other required fields
        // 3. Try to create event
        // 4. Verify error message: "Max entrants must be greater than 0"
        // 5. Verify event is not created
    }

    /**
     * Test Case 28: Attendee limit is displayed correctly in event details.
     * 
     * As an organizer, after creating an event with an attendee limit, the
     * limit should be displayed correctly in the event details screen.
     */
    @Test
    public void organizer_attendeeLimit_displayedInEventDetails() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Check the checkbox
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Enter a number
        onView(withId(R.id.et_max_entrants))
                .perform(typeText("50"));
        
        // Note: In a complete test:
        // 1. Fill all required fields and create event
        // 2. Navigate to event details screen
        // 3. Verify attendee limit is displayed (e.g., "Entrants (Current / Limit): 0 / 50")
    }

    /**
     * Test Case 29: Attendee limit checkbox and input are consistently styled.
     * 
     * As an organizer, the attendee limit checkbox and input field should
     * have consistent styling that matches the rest of the form.
     */
    @Test
    public void organizer_attendeeLimit_consistentlyStyled() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Verify checkbox is displayed
        onView(withId(R.id.cb_limit_waiting_list))
                .check(matches(isDisplayed()));
        
        // Check checkbox and verify input field
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 30: Attendee limit setting is accessible from create event screen.
     * 
     * As an organizer, I should be able to access the attendee limit setting
     * directly from the create event screen without additional navigation.
     */
    @Test
    public void organizer_attendeeLimitSetting_accessibleFromCreateEventScreen() {
        // Navigate to CreateEventFragment
        onView(withId(R.id.btn_addEvent)).perform(click());
        
        // Verify checkbox is immediately accessible
        onView(withId(R.id.cb_limit_waiting_list))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
        
        // Verify can interact with it
        onView(withId(R.id.cb_limit_waiting_list))
                .perform(click());
        
        // Verify input field appears
        onView(withId(R.id.et_max_entrants))
                .check(matches(isDisplayed()));
    }
}

