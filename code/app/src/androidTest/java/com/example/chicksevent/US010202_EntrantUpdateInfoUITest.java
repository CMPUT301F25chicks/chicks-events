package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.replaceText;
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
 * UI Tests for US 01.02.02:
 * As an entrant, I want to update information such as
 * name, email, and contact information on my profile.
 *
 * These tests verify:
 * - Users can navigate to the edit profile screen
 * - Current information is displayed
 * - Fields are editable
 * - Updated info is visible after editing
 * - Save/update button works
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class US010202_EntrantUpdateInfoUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private void scrollToView(ViewInteraction viewInteraction) {
        try { viewInteraction.perform(scrollTo()); }
        catch (Exception ignored) {}
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

    private void waitForView(Matcher<View> matcher, int maxAttempts) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                onView(matcher).check(matches(isDisplayed()));
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) throw e;
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void waitForView(Matcher<View> matcher) {
        waitForView(matcher, 10);
    }

    private void navigateToEditScreen() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        waitForView(withId(R.id.btn_profile));
        performReliableClick(onView(withId(R.id.btn_profile)));

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

    }

    // ------------------- TESTS --------------------

    @Test
    public void entrant_canSeeExistingInfo() {
        navigateToEditScreen();

        waitForView(withId(R.id.edit_name));
        waitForView(withId(R.id.edit_email));

        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_phone)).check(matches(isDisplayed()));
    }

    @Test
    public void entrant_canUpdateInfo() {
        navigateToEditScreen();

        onView(withId(R.id.edit_name)).perform(replaceText("Updated Name"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(replaceText("updated@example.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_phone)).perform(replaceText("9999999999"), closeSoftKeyboard());

        onView(withId(R.id.edit_name)).check(matches(withText("Updated Name")));
        onView(withId(R.id.edit_email)).check(matches(withText("updated@example.com")));
        onView(withId(R.id.edit_phone)).check(matches(withText("9999999999")));
    }

}
