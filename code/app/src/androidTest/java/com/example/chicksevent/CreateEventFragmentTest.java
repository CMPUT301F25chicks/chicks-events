package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test for {@link CreateEventFragment} in the Chicksevent app.
 * <p>
 * This test verifies that UI elements in the Create Event screen are displayed correctly,
 * navigation buttons work as expected, and the "Limit Waiting List" checkbox behaves correctly.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventFragmentTest {

    /**
     * Launches {@link MainActivity} before each test.
     */
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Verifies that all main buttons and checkboxes in the CreateEventFragment
     * are displayed after clicking the "Add Event" button.
     */
    @Test
    public void testButtonsDisplayed() {
        onView(withId(R.id.btn_addEvent)).perform(click());
        onView(withId(R.id.btn_notification)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_events)).check(matches(isDisplayed()));
        onView(withId(R.id.cb_limit_waiting_list)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create_event)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_cancel)).check(matches(isDisplayed()));
    }

    /**
     * Tests navigation using the fragment's buttons.
     * <p>
     * Clicks the "Notification" button and verifies the NotificationFragment UI is shown.
     * Then clicks the "Events" button and verifies the EventFragment UI is shown.
     * </p>
     */
    @Test
    public void testNavigationButtons() {
        onView(withId(R.id.btn_notification)).perform(click());
        onView(withId(R.id.recycler_notifications)).check(matches(isDisplayed())); // NotificationFragment UI

        onView(withId(R.id.btn_events)).perform(click());
        onView(withId(R.id.button_row)).check(matches(isDisplayed())); // EventFragment UI
    }

    /**
     * Tests the behavior of the "Limit Waiting List" checkbox and
     * the associated max entrants input field.
     * <p>
     * Initially, the max entrants field should be hidden. Clicking the checkbox
     * shows the field, and clicking again hides it.
     * </p>
     */
    @Test
    public void testMaxEntrantsCheckbox() {
        onView(withId(R.id.btn_addEvent)).perform(click());
        onView(withId(R.id.et_max_entrants)).check(matches(not(isDisplayed())));

        onView(withId(R.id.cb_limit_waiting_list)).perform(click());
        onView(withId(R.id.et_max_entrants)).check(matches(isDisplayed()));

        onView(withId(R.id.cb_limit_waiting_list)).perform(click());
        onView(withId(R.id.et_max_entrants)).check(matches(not(isDisplayed())));
    }
}
