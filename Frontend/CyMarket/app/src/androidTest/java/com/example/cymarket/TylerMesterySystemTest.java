package com.example.cymarket;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class TylerMesterySystemTest {

    @Rule
    public ActivityScenarioRule<MessagesActivity> activityRule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), MessagesActivity.class)
                            .putExtra("groupID", 1)
                            .putExtra("groupName", "Test Group")
            );

    // Test 1: Screen loads
    @Test
    public void testMessagesScreenLoads() {
        onView(withId(R.id.messageInput)).check(matches(isDisplayed()));
        onView(withId(R.id.sendButton)).check(matches(isDisplayed()));
        onView(withId(R.id.messagesRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.groupChatName)).check(matches(isDisplayed()));
    }

    // Test 2: Group name shows
    @Test
    public void testGroupNameDisplayed() {
        onView(withId(R.id.groupChatName))
                .check(matches(withText("Test Group")));
    }

    // Test 3: Send clears input
    @Test
    public void testMessageInputClearedAfterSend() {
        onView(withId(R.id.messageInput))
                .perform(typeText("Hello"), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        onView(withId(R.id.messageInput))
                .check(matches(withText("")));
    }

    // Test 4: Broadcast message appears
    @Test
    public void testIncomingMessageDisplayed() {
        Intent intent = new Intent("WS_MSG");
        intent.putExtra("key", "group_1");
        intent.putExtra("message", "user1: Hello!");

        LocalBroadcastManager.getInstance(
                ApplicationProvider.getApplicationContext()
        ).sendBroadcast(intent);

        onView(withText("Hello!")).check(matches(isDisplayed()));
        onView(withText("user1")).check(matches(isDisplayed()));
    }
}