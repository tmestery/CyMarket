package com.example.cymarket;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class AlexanderNotificationTest {

    @Rule
    public ActivityScenarioRule<NotificationsActivity> activityRule =
            new ActivityScenarioRule<>(NotificationsActivity.class);

    // Test: Notifications list is visible and contains at least one item
    @Test
    public void testNotificationsRecyclerViewDisplaysItems() {
        // RecyclerView should be visible
        onView(withId(R.id.notifications_recycler)).check(matches(isDisplayed()));

        // Expect a notification message to be displayed
        onView(withText("Your item 'Test Item' has been listed."))
                .check(matches(isDisplayed()));
    }
}
