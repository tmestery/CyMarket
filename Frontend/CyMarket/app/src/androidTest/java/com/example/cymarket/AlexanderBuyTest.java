package com.example.cymarket;

import static android.content.Context.MODE_PRIVATE;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;

@RunWith(AndroidJUnit4.class)
public class AlexanderBuyTest {

    @Before
    public void setupSharedPrefs() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .putString("username", "TestUser")
                .putString("email", "test@email.com")
                .putString("password", "password123")
                .apply();
    }

    @Rule
    public ActivityScenarioRule<BuyActivity> activityRule =
            new ActivityScenarioRule<>(BuyActivity.class);

    // Test: Clear button removes all listings and shows toast
    @Test
    public void testClearListingsShowsToastAndRefreshes() {
        // Click the clear button
        onView(withId(R.id.buy_clear_all_btn)).perform(click());

        // Expect toast confirming clear request
        onView(withText("Clear request sent")).check(matches(isDisplayed()));

        // RecyclerView should still be visible (refreshed)
        onView(withId(R.id.buy_listings_recycler)).check(matches(isDisplayed()));
    }
}
