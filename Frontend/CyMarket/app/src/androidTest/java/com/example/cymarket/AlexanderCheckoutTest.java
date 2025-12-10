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
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;

@RunWith(AndroidJUnit4.class)
public class AlexanderCheckoutTest {

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
    public ActivityScenarioRule<CheckoutActivity> activityRule =
            new ActivityScenarioRule<>(CheckoutActivity.class);

    // Test 1: Successful Checkout
    @Test
    public void testSuccessfulCheckout() {
        onView(withId(R.id.card_name)).perform(typeText("Example Example"), closeSoftKeyboard());
        onView(withId(R.id.card_number)).perform(typeText("1111111111111111"), closeSoftKeyboard());
        onView(withId(R.id.card_expiry)).perform(typeText("12/25"), closeSoftKeyboard());
        onView(withId(R.id.card_cvv)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.card_address)).perform(typeText("123 Main St"), closeSoftKeyboard());
        onView(withId(R.id.card_zip)).perform(typeText("60000"), closeSoftKeyboard());

        onView(withId(R.id.checkout_confirm_btn)).perform(click());

        // Expect successful toast
        onView(withText("Order placed!")).check(matches(isDisplayed()));
    }

    // Test 2: Card number is invalid
    @Test
    public void testInvalidCardNumberShowsError() {
        onView(withId(R.id.card_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.card_number)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.card_expiry)).perform(typeText("12/25"), closeSoftKeyboard());
        onView(withId(R.id.card_cvv)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.card_address)).perform(typeText("123 Main St"), closeSoftKeyboard());
        onView(withId(R.id.card_zip)).perform(typeText("50010"), closeSoftKeyboard());

        onView(withId(R.id.checkout_confirm_btn)).perform(click());

        onView(withText("Card number must be between 13 and 19 digits"))
                .check(matches(isDisplayed()));
    }

    // Test 3: Fields are empty
    @Test
    public void testEmptyFieldsShowError() {
        onView(withId(R.id.checkout_confirm_btn)).perform(click());
        onView(withText("Please fill out all fields")).check(matches(isDisplayed()));
    }

    // Test 4: Card number is in an expired year
    @Test
    public void testExpiredCardShowsError() {
        onView(withId(R.id.card_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.card_number)).perform(typeText("4111111111111111"), closeSoftKeyboard());
        onView(withId(R.id.card_expiry)).perform(typeText("12/20"), closeSoftKeyboard()); // expired year
        onView(withId(R.id.card_cvv)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.card_address)).perform(typeText("123 Main St"), closeSoftKeyboard());
        onView(withId(R.id.card_zip)).perform(typeText("50010"), closeSoftKeyboard());

        onView(withId(R.id.checkout_confirm_btn)).perform(click());

        onView(withText("Card has expired")).check(matches(isDisplayed()));
    }
}
