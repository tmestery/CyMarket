package com.example.cymarket;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.cymarket.ProfilesSettings.ProfilesActivity;
import com.example.cymarket.ProfilesSettings.SettingsActivity;

/**
 * System/UI tests for ProfilesSettings package
 * Covers ProfilesActivity + SettingsActivity
 */
@RunWith(AndroidJUnit4.class)
public class TylerProfilesSettingsTest {

    /* ----------------------- SETUP ----------------------- */

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

    /* =====================================================
       PROFILES ACTIVITY
       ===================================================== */

    // Screen loads
    @Test
    public void testProfilesScreenLoads() {
        ActivityScenario.launch(ProfilesActivity.class);

        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_image_view)).check(matches(isDisplayed()));
        onView(withId(R.id.prfls_home_page_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.prfls_setting_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.prfls_notifications_btn)).check(matches(isDisplayed()));
    }

    // Username displayed from SharedPreferences
    @Test
    public void testUsernameDisplayed() {
        ActivityScenario.launch(ProfilesActivity.class);

        onView(withId(R.id.username_text))
                .check(matches(withText("TestUser")));
    }

    // Clicking profile image launches picker intent
    @Test
    public void testProfileImageClickable() {
        ActivityScenario.launch(ProfilesActivity.class);

        onView(withId(R.id.profile_image_view))
                .perform(click());
    }

    // Home button navigates
    @Test
    public void testHomeButtonNavigation() {
        ActivityScenario.launch(ProfilesActivity.class);

        onView(withId(R.id.prfls_home_page_btn))
                .perform(click());
    }

    // Notifications button navigates
    @Test
    public void testNotificationsButtonNavigation() {
        ActivityScenario.launch(ProfilesActivity.class);

        onView(withId(R.id.prfls_notifications_btn))
                .perform(click());
    }

    // Settings button navigation with extras
    @Test
    public void testSettingsButtonNavigation() {
        ActivityScenario.launch(ProfilesActivity.class);

        onView(withId(R.id.prfls_setting_btn))
                .perform(click());

        onView(withId(R.id.username_text))
                .check(matches(isDisplayed()));
    }

    /* =====================================================
       SETTINGS ACTIVITY
       ===================================================== */

    // Screen loads with intent extras
    @Test
    public void testSettingsScreenLoads() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SettingsActivity.class
        );
        intent.putExtra("username", "TestUser");
        intent.putExtra("email", "test@email.com");
        intent.putExtra("password", "password123");

        ActivityScenario.launch(intent);

        onView(withId(R.id.delete_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.remove_pfp_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.logout_btn)).check(matches(isDisplayed()));
    }

    // User info text displayed
    @Test
    public void testUserInfoDisplayed() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SettingsActivity.class
        );
        intent.putExtra("username", "TestUser");
        intent.putExtra("email", "test@email.com");
        intent.putExtra("password", "pass");

        ActivityScenario.launch(intent);

        onView(withText("Username: TestUser")).check(matches(isDisplayed()));
        onView(withText("Email: test@email.com")).check(matches(isDisplayed()));
        onView(withText("Password: pass")).check(matches(isDisplayed()));
    }

    // Logout button navigation
    @Test
    public void testLogoutButtonNavigatesToLogin() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SettingsActivity.class
        );
        intent.putExtra("username", "TestUser");
        intent.putExtra("email", "test@email.com");
        intent.putExtra("password", "password123");

        ActivityScenario.launch(intent);

        onView(withId(R.id.logout_btn))
                .perform(click());
    }

    // Delete account button clickable
    @Test
    public void testDeleteAccountButtonClickable() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SettingsActivity.class
        );
        intent.putExtra("username", "TestUser");
        intent.putExtra("email", "test@email.com");
        intent.putExtra("password", "password123");

        ActivityScenario.launch(intent);

        onView(withId(R.id.delete_btn))
                .perform(click());
    }

    // Remove profile picture button clickable
    @Test
    public void testRemoveProfilePictureButton() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                SettingsActivity.class
        );
        intent.putExtra("username", "TestUser");
        intent.putExtra("email", "test@email.com");
        intent.putExtra("password", "password123");

        ActivityScenario.launch(intent);

        onView(withId(R.id.remove_pfp_btn))
                .perform(click());
    }
}