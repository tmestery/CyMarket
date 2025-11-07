package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessagesActivity extends AppCompatActivity {

    private Button sendButton;
    private EditText messageInput;
    private TextView groupChatName;
    private RecyclerView messagesRecyclerView;
    private Button reportButton;
    private String CHAT_KEY;
    private String username;
    private int groupId;
    private String groupName;
    private ArrayList<Message> messages = new ArrayList<>();
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // --- Views ---
        sendButton = findViewById(R.id.sendButton);
        messageInput = findViewById(R.id.messageInput);
        groupChatName = findViewById(R.id.groupChatName);
        reportButton = findViewById(R.id.reportButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageAdapter = new MessageAdapter(messages, username);
        messagesRecyclerView.setAdapter(messageAdapter);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- Get username ---
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "");

        // --- Get group info ---
        groupId = getIntent().getIntExtra("groupID", -1);
        groupName = getIntent().getStringExtra("groupName");

        // --- Set chat key for group ---
        CHAT_KEY = "groupChat_" + groupId;

        // --- Display group info ---
        groupChatName.setText(groupName != null ? groupName : "Group #" + groupId);

        // --- Send message handler ---
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) return;

            Intent intent = new Intent("WS_SEND");
            intent.putExtra("key", CHAT_KEY);
            intent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            messageInput.setText("");
        });

        // --- Report button ---
        reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(MessagesActivity.this, ReportUserActivity.class);
            intent.putExtra("reportedUser", "Group Chat"); // or specific username if DM
            startActivity(intent);
        });

        // --- Connect to WebSocket ---
        String wsUrl = "ws://coms-3090-056.class.las.iastate.edu:8080/chat/"
                + groupId + "/" + username;

        Intent serviceIntent = new Intent(this, WebSocketService.class);
        serviceIntent.setAction("WS_CONNECT");
        serviceIntent.putExtra("key", CHAT_KEY);
        serviceIntent.putExtra("url", wsUrl);
        startService(serviceIntent);

        setupBottomNav();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CHAT_KEY.equals(intent.getStringExtra("key"))) {
                String msg = intent.getStringExtra("message");
                Log.d("MessagesActivity", "Received msg: " + msg);
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(msg);
                        String sender = obj.getString("sender");
                        String content = obj.getString("content");
                        messages.add(new Message(sender, content));
                        messageAdapter.notifyItemInserted(messages.size() - 1);
                        messagesRecyclerView.scrollToPosition(messages.size() - 1);
                    } catch (Exception e) {
                        Log.e("MessagesActivity", "JSON parse error: " + e.getMessage());
                    }
                });
            }
        }

//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (CHAT_KEY.equals(intent.getStringExtra("key"))) {
//                String msg = intent.getStringExtra("message");
//                runOnUiThread(() -> {
//                    try {
//                        JSONObject obj = new JSONObject(msg);
//                        String sender = obj.getString("sender");
//                        String content = obj.getString("content");
//                        messageAdapter.addMessage(new Message(sender, content));
//                        messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
//                    } catch (Exception e) {
//                        messageAdapter.addMessage(new Message("System", msg));
//                        messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
//                    }
//                });
//            }
//        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter("WS_MSG"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(receiver);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_buy)
                startActivity(new Intent(this, BuyActivity.class));
            else if (item.getItemId() == R.id.nav_sell)
                startActivity(new Intent(this, SellActivity.class));
            else if (item.getItemId() == R.id.nav_chat)
                startActivity(new Intent(this, GroupListActivity.class));
            return true;
        });
    }
}