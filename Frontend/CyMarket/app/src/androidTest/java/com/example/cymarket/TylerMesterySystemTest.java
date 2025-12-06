package com.example.cymarket;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.example.cymarket.Messages.MessagesActivity;

@RunWith(AndroidJUnit4.class)
public class TylerMesterySystemTest {

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .putString("username", "TestUser")
                .apply();
    }

    @Rule
    public ActivityScenarioRule<MessagesActivity> activityRule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), MessagesActivity.class)
                            .putExtra("groupID", 123)
                            .putExtra("groupName", "TestGroup")
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
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MessagesActivity.class);
        intent.putExtra("groupID", 123);
        intent.putExtra("groupName", "TestGroup");
        ActivityScenario.launch(intent);

        onView(withId(R.id.groupChatName))
                .check(matches(withText("TestGroup")));
    }

    @Test
    public void testMessageInputClearedAfterSend() throws InterruptedException {
        onView(withId(R.id.messageInput)).perform(typeText("Hello"), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());
        onView(withId(R.id.messageInput)).check(matches(withText("")));
    }

    // Test 4: Broadcast message appears
    @Test
    public void testIncomingMessageDisplayed() {
        Intent intent = new Intent("WS_MSG");
        intent.putExtra("groupID", 12345);
        intent.putExtra("groupName", "TestGroupTwo");
        ActivityScenario.launch(intent);

        LocalBroadcastManager.getInstance(
                ApplicationProvider.getApplicationContext()
        ).sendBroadcast(intent);

        onView(withText("Hello!")).check(matches(isDisplayed()));
        onView(withText("user1")).check(matches(isDisplayed()));
    }
}