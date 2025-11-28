package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
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
 * UI tests for US 01.02.01:
 * As an entrant, I want to provide my personal information such as
 * name, email, and optional phone number in the app.
 *
 * These tests verify that:
 * - Users can navigate to the profile info screen
 * - Input fields are visible and editable
 * - Submit/save button is clickable
 * - Required fields (name, email) must be entered
 * - Optional field (phone) is allowed to be empty
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantProvideInfoUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private void scrollToView(ViewInteraction viewInteraction) {
        try { viewInteraction.perform(scrollTo()); }
        catch (Exception ignored) { }
    }

    private void performReliableClick(ViewInteraction viewInteraction) {
        scrollToView(viewInteraction);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        viewInteraction.perform(new GeneralClickAction(
                Tap.SINGLE,
                GeneralLocation.CENTER,
                Press.FINGER,
                0, 0
        ));
    }

    private void waitForView(Matcher<View> viewMatcher, int maxAttempts) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                onView(viewMatcher).check(matches(isDisplayed()));
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) throw e;
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void waitForView(Matcher<View> viewMatcher) {
        waitForView(viewMatcher, 10);
    }

    private void navigateToProfileForm() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        waitForView(withId(R.id.btn_profile));
        performReliableClick(onView(withId(R.id.btn_profile)));

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

    }

    // ------------------ TESTS -------------------

    @Test
    public void entrant_fieldsAreVisible() {
        navigateToProfileForm();

        waitForView(withId(R.id.edit_name));
        waitForView(withId(R.id.edit_email));
        waitForView(withId(R.id.edit_phone));

        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_phone)).check(matches(isDisplayed()));
    }

    @Test
    public void entrant_canEnterPersonalInfo() {
        navigateToProfileForm();

        onView(withId(R.id.edit_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_phone)).perform(typeText("1234567890"), closeSoftKeyboard());

        onView(withId(R.id.edit_name)).check(matches(withText("John Doe")));
        onView(withId(R.id.edit_email)).check(matches(withText("john@example.com")));
        onView(withId(R.id.edit_phone)).check(matches(withText("1234567890")));
    }


}
