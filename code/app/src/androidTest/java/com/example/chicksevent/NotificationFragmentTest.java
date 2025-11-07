package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test for {@link NotificationFragment} in the Chicksevent app.
 * <p>
 * This test verifies that the UI elements in the fragment are displayed correctly
 * and that navigation buttons function as expected.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class NotificationFragmentTest {

    /**
     * Launches {@link MainActivity} before each test.
     */
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Verifies that all main buttons and the notification recycler view
     * are displayed on the screen.
     */
    @Test
    public void testButtonsDisplayed() {
        onView(withId(R.id.btn_events)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_addEvent)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_notifications)).check(matches(isDisplayed()));
    }

    /**
     * Tests navigation by simulating button clicks and checking if
     * the resulting UI elements are displayed.
     * <p>
     * Clicks on "Events" and verifies the event row is visible.
     * Clicks on "Add Event" and verifies the event poster image is visible.
     * </p>
     */
    @Test
    public void testNavigationButtons() {
        onView(withId(R.id.btn_events)).perform(click());
        onView(withId(R.id.button_row)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_addEvent)).perform(click());
        onView(withId(R.id.img_event_poster)).check(matches(isDisplayed()));

        // onView(withId(R.id.btn_profile)).perform(click());
        // onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
    }
}
