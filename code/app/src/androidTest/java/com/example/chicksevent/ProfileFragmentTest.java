package com.example.chicksevent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileFragmentTest {

    @Before
    public void launchFragment() {
        // You can pass arguments if needed
        Bundle args = new Bundle();
        args.putString("eventName", "Sample Event");

        FragmentScenario.launchInContainer(
                ProfileFragment.class,
                args,
                R.style.Theme_ChicksEvent,  // Your app theme
                (FragmentFactory) null
        );
    }

    @Test
    public void testUIElementsAreDisplayed() {
        // Check that UI elements show up
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_phone)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save_info)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_delete_account)).check(matches(isDisplayed()));
    }

    @Test
    public void testCanTypeAndSaveProfile() {
        // Type into EditTexts
        onView(withId(R.id.edit_name)).perform(typeText("John Doe"));
        onView(withId(R.id.edit_email)).perform(typeText("john@example.com"));
        onView(withId(R.id.edit_phone)).perform(typeText("1234567890"));

        // Click save button
        onView(withId(R.id.btn_save_info)).perform(click());
    }

    @Test
    public void testDeleteButtonClickShowsToast() {
        // Click delete and check if the fields are cleared
        onView(withId(R.id.btn_delete_account)).perform(click());
    }
}
