package com.example.cymarket;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import android.content.Intent;

import com.example.cymarket.ProfilesSettings.SettingsActivity;

@RunWith(AndroidJUnit4.class)
public class TylerMesterySystemTest2 {
    @Test
    public void testSettingsLoadsAndButtonsWork() {

        // Launch SettingsActivity manually with mock extras
        Intent intent = new Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                SettingsActivity.class
        );
        intent.putExtra("username", "testUser");
        intent.putExtra("password", "abcd1234");
        intent.putExtra("email", "test@example.com");

        ActivityScenario.launch(intent);

        // Verify the text fields render correctly
        onView(withId(R.id.username_text))
                .check(matches(withText("Username: testUser")));

        onView(withId(R.id.email_text))
                .check(matches(withText("Email: test@example.com")));

        onView(withId(R.id.password_text))
                .check(matches(withText("Password: abcd1234")));

        // Check that the buttons are visible
        onView(withId(R.id.delete_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.logout_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.remove_pfp_btn)).check(matches(isDisplayed()));

        // Ensure the buttons are clickable
        onView(withId(R.id.logout_btn)).perform(click());
    }
}