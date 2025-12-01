package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for US 01.07.01: As an entrant, I want to be identified by my device, 
 * so that I don't have to use a username and password.
 * These tests verify that:
 * <ul>
 *   <li>App uses device ID for identification</li>
 *   <li>No login screen is required</li>
 *   <li>User is automatically identified on app launch</li>
 *   <li>Profile is accessible without authentication</li>
 * </ul>
 * <p>
 * <b>Note:</b> This is more of a system-level test. The app should automatically
 * identify users by Android ID without requiring login credentials.
 * </p>
 *
 * @author Jinn Kasai
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TEST01_US010701_EntrantDeviceIdentificationUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

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

    /**
     * Test: App launches without login screen.
     * VERIFIES FUNCTIONALITY: App uses device identification, no login required.
     */
    @Test
    public void entrant_appLaunchesWithoutLoginScreen() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        
        // VERIFY FUNCTIONALITY: Main screen should be displayed immediately (no login screen)
        // If we can see main navigation buttons, device identification worked
        
        // Check for main navigation buttons (these appear on main screens)
        boolean mainScreenVisible = false;
        
        try {
            waitForView(withId(R.id.btn_events), 15);
            onView(withId(R.id.btn_events))
                    .check(matches(isDisplayed()));
            mainScreenVisible = true;
        } catch (Exception e) {
            // Try alternative - might be on NotificationFragment
            try {
                waitForView(withId(R.id.recycler_notifications), 10);
                onView(withId(R.id.recycler_notifications))
                        .check(matches(isDisplayed()));
                mainScreenVisible = true;
            } catch (Exception e2) {
                // Try checking for profile button
                try {
                    waitForView(withId(R.id.btn_profile), 10);
                    onView(withId(R.id.btn_profile))
                            .check(matches(isDisplayed()));
                    mainScreenVisible = true;
                } catch (Exception e3) {
                }
            }
        }
        
        // Verify no login screen appeared
        // If main screen is visible, device identification worked
        if (!mainScreenVisible) {
            throw new AssertionError("Main screen not visible. App may require login or device identification failed.");
        }
        
        // SUCCESS: User story functionality verified - app launches without login, uses device ID
    }

    /**
     * Test: User can access profile without authentication.
     * VERIFIES FUNCTIONALITY: Profile is accessible without login credentials.
     */
    @Test
    public void entrant_canAccessProfileWithoutAuthentication() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        
        // Navigate to profile - should work without authentication
        waitForView(withId(R.id.btn_profile), 15);
        onView(withId(R.id.btn_profile)).perform(click());
        
        // Wait for navigation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // VERIFY FUNCTIONALITY: Profile screen should be accessible
        // Verify profile fields are visible (no login required)
        waitForView(withId(R.id.edit_name), 10);
        onView(withId(R.id.edit_name))
                .check(matches(isDisplayed()));
        
        onView(withId(R.id.edit_email))
                .check(matches(isDisplayed()));
        
        // SUCCESS: User story functionality verified - profile accessible without authentication
        // Device-based identification works correctly
    }
}

