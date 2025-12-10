package com.example.cymarket;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class AlexanderSellTest {

    @Rule
    public ActivityScenarioRule<SellActivity> activityRule =
            new ActivityScenarioRule<>(SellActivity.class);

    // Test: Successful listing shows toast
    @Test
    public void testSuccessfulListingShowsToast() {
        onView(withId(R.id.sell_item_name_edt)).perform(typeText("Laptop"), closeSoftKeyboard());
        onView(withId(R.id.sell_price_edt)).perform(typeText("500"), closeSoftKeyboard());
        onView(withId(R.id.sell_description_edt)).perform(typeText("Gaming laptop"), closeSoftKeyboard());
        onView(withId(R.id.sell_quantity_edt)).perform(typeText("1"), closeSoftKeyboard());

        onView(withId(R.id.sell_submit_btn)).perform(click());

        // Expect success toast
        onView(withText("Item listed!")).check(matches(isDisplayed()));
    }
}
