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

@RunWith(AndroidJUnit4.class)
public class CreateEventFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testButtonsDisplayed() {
        // Verify the main buttons in CreateEventFragment are visible
        onView(withId(R.id.btn_addEvent)).perform(click());
        onView(withId(R.id.btn_notification)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_events)).check(matches(isDisplayed()));
        onView(withId(R.id.cb_limit_waiting_list)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create_event)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_cancel)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationButtons() {
        // Navigate using buttons

        onView(withId(R.id.btn_notification)).perform(click());
        onView(withId(R.id.recycler_notifications)).check(matches(isDisplayed())); // NotificationFragment UI

        // Go back to CreateEventFragment before next test
        onView(withId(R.id.btn_events)).perform(click());
        onView(withId(R.id.button_row)).check(matches(isDisplayed())); // EventFragment UI
    }

    @Test
    public void testMaxEntrantsCheckbox() {
        // The max entrants field should initially be gone
        onView(withId(R.id.btn_addEvent)).perform(click());
        onView(withId(R.id.et_max_entrants)).check(matches(not(isDisplayed())));

        // Click the checkbox to show it
        onView(withId(R.id.cb_limit_waiting_list)).perform(click());
        onView(withId(R.id.et_max_entrants)).check(matches(isDisplayed()));

        // Click again to hide
        onView(withId(R.id.cb_limit_waiting_list)).perform(click());
        onView(withId(R.id.et_max_entrants)).check(matches(not(isDisplayed())));
    }
}
