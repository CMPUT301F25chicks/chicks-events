package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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
 * UI Tests for US 01.02.04:
 * As an entrant, I want to delete my profile if I no longer wish to use the app.
 *
 * These tests verify:
 * - Users can navigate to the delete-account screen
 * - Delete button is visible and clickable
 * - After deletion, all profile fields are cleared
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class US010204_EntrantDeleteProfileUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // ---------- Helper Methods (exact same style) ----------

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

    private void navigateToDeleteScreen() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        waitForView(withId(R.id.btn_profile));
        performReliableClick(onView(withId(R.id.btn_profile)));

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        waitForView(withId(R.id.btn_delete_account));
    }

    // ------------------- TESTS --------------------

    @Test
    public void entrant_canSeeDeleteButton() {
        navigateToDeleteScreen();

        onView(withId(R.id.btn_delete_account)).check(matches(isDisplayed()));
    }

    @Test
    public void entrant_deleteClearsFields() {
        navigateToDeleteScreen();

        // Click Delete Button
        performReliableClick(onView(withId(R.id.btn_delete_account)));

        // After deletion, fields should be cleared
        waitForView(withId(R.id.edit_name));
        waitForView(withId(R.id.edit_email));
        waitForView(withId(R.id.edit_phone));

        onView(withId(R.id.edit_name)).check(matches(withText("")));
        onView(withId(R.id.edit_email)).check(matches(withText("")));
        onView(withId(R.id.edit_phone)).check(matches(withText("")));
    }
}
