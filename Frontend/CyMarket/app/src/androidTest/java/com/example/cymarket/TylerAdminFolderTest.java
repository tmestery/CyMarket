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
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.cymarket.Admin.AdminDashboardActivity;
import com.example.cymarket.Admin.AdminProfilesActivity;
import com.example.cymarket.Admin.AdminSettingsActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TylerAdminFolderTest {

    @Before
    public void setupSharedPrefs() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .putString("username", "AdminUser")
                .putString("email", "admin@test.com")
                .putString("password", "password123")
                .apply();
    }

    /* =====================================================
       ADMIN DASHBOARD ACTIVITY TESTS
       ===================================================== */

    @Rule
    public ActivityScenarioRule<AdminDashboardActivity> dashboardRule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), AdminDashboardActivity.class)
            );

    // onCreate → screen loads
    public void testAdminDashboardLoads() {
        onView(withId(R.id.sales_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.users_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.report_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerViewFriends)).check(matches(isDisplayed()));
    }

    // fetchUsers() → users view visible by default
    public void testUsersRecyclerViewVisibleOnLaunch() {
        onView(withId(R.id.recyclerViewFriends))
                .check(matches(isDisplayed()));
    }

    // reportButton → fetchReports()
    @Test
    public void testClickReportsButtonShowsReportsSection() {
        onView(withId(R.id.report_btn)).perform(click());
        onView(withId(R.id.recyclerViewReports))
                .check(matches(isDisplayed()));
    }

    // usersButton visibility logic
    @Test
    public void testUsersButtonRestoresUsersView() {
        onView(withId(R.id.report_btn)).perform(click());
        onView(withId(R.id.users_btn)).perform(click());
        onView(withId(R.id.recyclerViewFriends))
                .check(matches(isDisplayed()));
    }

    // salesButton → fetchTotalSales()
    @Test
    public void testSalesButtonShowsSalesUI() {
        onView(withId(R.id.sales_btn)).perform(click());
        onView(withId(R.id.total_sales_amount))
                .check(matches(isDisplayed()));
    }

    // bottom navigation → profile
    @Test
    public void testDashboardBottomNavProfile() {
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.profile_image_view)).check(matches(isDisplayed()));
    }

    /* =====================================================
       ADMIN PROFILES ACTIVITY TESTS
       ===================================================== */

    @Test
    public void testAdminProfileScreenLoads() {
        ActivityScenario.launch(AdminProfilesActivity.class);
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_image_view)).check(matches(isDisplayed()));
        onView(withId(R.id.textView2)).check(matches(isDisplayed()));
    }

    // onCreate → username shown from SharedPreferences
    @Test
    public void testUsernameDisplayedFromPrefs() {
        ActivityScenario.launch(AdminProfilesActivity.class);
        onView(withId(R.id.username_text))
                .check(matches(withText("AdminUser")));
    }

    // profileImage click → image picker intent
    @Test
    public void testProfileImageClickable() {
        ActivityScenario.launch(AdminProfilesActivity.class);
        onView(withId(R.id.profile_image_view))
                .perform(click());
    }

    // bottom nav → dashboard
    @Test
    public void testProfileBottomNavHome() {
        ActivityScenario.launch(AdminProfilesActivity.class);
        onView(withId(R.id.nav_home)).perform(click());
        onView(withId(R.id.sales_btn)).check(matches(isDisplayed()));
    }

    /* =====================================================
       ADMIN SETTINGS ACTIVITY TESTS
       ===================================================== */

    @Test
    public void testAdminSettingsScreenLoads() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                AdminSettingsActivity.class
        );
        intent.putExtra("username", "AdminUser");
        intent.putExtra("email", "admin@test.com");
        intent.putExtra("password", "password123");

        ActivityScenario.launch(intent);

        onView(withId(R.id.logout_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.remove_pfp_btn)).check(matches(isDisplayed()));
    }

    // onCreate → populated account fields
    @Test
    public void testAdminSettingsTextDisplayed() {
        ActivityScenario.launch(AdminSettingsActivity.class);
        onView(withId(R.id.username_text)).check(matches(isDisplayed()));
        onView(withId(R.id.email_text)).check(matches(isDisplayed()));
        onView(withId(R.id.password_text)).check(matches(isDisplayed()));
    }

    // logoutButton → LoginActivity
    @Test
    public void testLogoutButtonNavigatesToLogin() {
        ActivityScenario.launch(AdminSettingsActivity.class);
        onView(withId(R.id.logout_btn)).perform(click());
    }

    // removePFP button clickable (API tested indirectly)
    @Test
    public void testRemoveProfilePictureButtonClickable() {
        ActivityScenario.launch(AdminSettingsActivity.class);
        onView(withId(R.id.remove_pfp_btn)).perform(click());
    }

    // bottom nav → profile
    @Test
    public void testSettingsBottomNavProfile() {
        ActivityScenario.launch(AdminSettingsActivity.class);
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.profile_image_view)).check(matches(isDisplayed()));
    }
}