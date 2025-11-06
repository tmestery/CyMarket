package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

public class MessagesActivity extends AppCompatActivity {

    private Button sendButton;
    private EditText messageInput;
    private TextView groupchatPersonName;
    private TextView groupChatName;
    private TextView messagesTextView; // renamed for clarity
    private Button reportButton;
    private String CHAT_KEY;
    private static final String TAG = "MessagesActivity";
    private String username;
    private int groupId;
    private String groupName;
    private String friendUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        sendButton = findViewById(R.id.sendButton);
        messageInput = findViewById(R.id.messageInput);
        messagesTextView = findViewById(R.id.messagesRecyclerView);
        groupchatPersonName = findViewById(R.id.groupchatPerson);
        groupChatName = findViewById(R.id.groupChatName);
        reportButton = findViewById(R.id.reportButton);

        // ✅ Get username from shared prefs
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "");

        // ✅ Receive group info and friend name
        groupId = getIntent().getIntExtra("groupID", -1);
        groupName = getIntent().getStringExtra("groupName");
        friendUsername = getIntent().getStringExtra("friendUsername");

        if (groupId == -1) {
            messagesTextView.setText("Error: Missing group ID.");
            return;
        }

        // Set chat key here:
        CHAT_KEY = "groupChat_" + groupId;

        // ✅ Display info
        groupChatName.setText(groupName != null ? groupName : "Group #" + groupId);
        groupchatPersonName.setText(friendUsername != null ? friendUsername : "Group Chat");

        // ✅ Handle sending messages
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) return;

            Intent intent = new Intent("WS_SEND");
            intent.putExtra("key", CHAT_KEY);
            intent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            messageInput.setText("");
        });

        // ✅ Report button
        reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(MessagesActivity.this, ReportUserActivity.class);
            intent.putExtra("reportedUser", groupchatPersonName.getText().toString());
            startActivity(intent);
        });

        // ✅ Connect to WebSocket
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
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(msg);
                        String sender = obj.getString("sender");
                        String content = obj.getString("content");
                        messagesTextView.append("\n" + sender + ": " + content);
                    } catch (Exception e) {
                        messagesTextView.append("\n" + msg); // fallback
                    }
                });
            }
        }
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