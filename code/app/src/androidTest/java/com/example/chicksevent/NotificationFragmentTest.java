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

@RunWith(AndroidJUnit4.class)
public class NotificationFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testButtonsDisplayed() {
        // Verify all buttons and list are visible
        onView(withId(R.id.btn_events)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_addEvent)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_notifications)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationButtons() {
        // Simulate navigation clicks
        onView(withId(R.id.btn_events)).perform(click());
        onView(withId(R.id.button_row)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_addEvent)).perform(click());
        onView(withId(R.id.img_event_poster)).check(matches(isDisplayed()));

        //onView(withId(R.id.btn_profile)).perform(click());
        //onView(withId(R.id.edit_name)).check(matches(isDisplayed()));

    }
}
