package com.example.cymarket;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class AlexanderBuyTest {

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
