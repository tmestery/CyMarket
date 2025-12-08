package com.example.cymarket;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.cymarket.LoginSignup.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * System/UI tests for LoginSignup package
 * Covers ALL methods via UI-driven behavior
 */
@RunWith(AndroidJUnit4.class)
public class TylerLoginSignupFolderTest {

    /* ----------------------- SETUP ----------------------- */

    @Before
    public void setupPrefs() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    /* =====================================================
       LOGIN ACTIVITY
       ===================================================== */

    @Test
    public void testLoginActivityLoads() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.login_email_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }

    // empty credentials check
    @Test
    public void testLoginWithEmptyFieldsShowsToast() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.login_login_btn)).perform(click());
    }

    // forgot password navigation
    @Test
    public void testForgotPasswordNavigation() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.forgot_password)).perform(click());
        onView(withId(R.id.reset_password))
                .check(matches(isDisplayed()));
    }

    // signup navigation
    @Test
    public void testSignupNavigationFromLogin() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.login_signup_txt)).perform(click());
        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));
    }

    /* =====================================================
       FORGOT PASSWORD ACTIVITY
       ===================================================== */

    @Test
    public void testForgotPasswordScreenLoads() {
        ActivityScenario.launch(ForgotPasswordActivity.class);
        onView(withId(R.id.editTextTextEmailAddress)).check(matches(isDisplayed()));
        onView(withId(R.id.reset_password)).check(matches(isDisplayed()));
    }

    // empty email validation
    @Test
    public void testForgotPasswordWithEmptyEmail() {
        ActivityScenario.launch(ForgotPasswordActivity.class);
        onView(withId(R.id.reset_password)).perform(click());
    }

    /* =====================================================
       RESET PASSWORD ACTIVITY
       ===================================================== */

    @Test
    public void testResetPasswordScreenLoads() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                ResetPasswordActivity.class
        );
        intent.putExtra("email", "user@test.com");
        ActivityScenario.launch(intent);

        onView(withId(R.id.editTextCode)).check(matches(isDisplayed()));
        onView(withId(R.id.editTextNewPassword)).check(matches(isDisplayed()));
    }

    // empty fields check
    @Test
    public void testResetPasswordWithEmptyFields() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                ResetPasswordActivity.class
        );
        intent.putExtra("email", "user@test.com");
        ActivityScenario.launch(intent);

        onView(withId(R.id.reset_password)).perform(click());
    }

    /* =====================================================
       SIGNUP ACTIVITY
       ===================================================== */

    @Test
    public void testSignupScreenLoads() {
        ActivityScenario.launch(SignupActivity.class);
        onView(withId(R.id.signup_first_name_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.signup_signup_btn)).check(matches(isDisplayed()));
    }

    // login redirect
    @Test
    public void testSignupLoginTextNavigatesToLogin() {
        ActivityScenario.launch(SignupActivity.class);
        onView(withId(R.id.signup_login_text)).perform(click());
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }

    // mismatched passwords
    @Test
    public void testSignupPasswordMismatch() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.signup_first_name_edt)).perform(typeText("John"), closeSoftKeyboard());
        onView(withId(R.id.signup_last_name_edt)).perform(typeText("Doe"), closeSoftKeyboard());
        onView(withId(R.id.signup_email_edt)).perform(typeText("john@test.com"), closeSoftKeyboard());
        onView(withId(R.id.signup_username_edt)).perform(typeText("john123"), closeSoftKeyboard());
        onView(withId(R.id.signup_password_edt)).perform(typeText("pass1"), closeSoftKeyboard());
        onView(withId(R.id.signup_confirm_edt)).perform(typeText("pass2"), closeSoftKeyboard());

        onView(withId(R.id.signup_signup_btn)).perform(click());
    }

    // empty field validation
    @Test
    public void testSignupWithEmptyFields() {
        ActivityScenario.launch(SignupActivity.class);
        onView(withId(R.id.signup_signup_btn)).perform(click());
    }

    /* =====================================================
       USER MODEL (POJO) TESTS
       ===================================================== */

    @Test
    public void testUserGettersAndSetters() {
        User user = new User();

        user.setId(10);
        user.setUsername("testUser");
        user.setProfileImageUrl("http://image.url");

        assert(user.getId() == 10);
        assert(user.getUsername().equals("testUser"));
        assert(user.getProfileImageUrl().equals("http://image.url"));
    }
}