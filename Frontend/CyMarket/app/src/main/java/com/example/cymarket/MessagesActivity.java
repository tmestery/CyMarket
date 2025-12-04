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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessagesActivity extends AppCompatActivity {

    // A list of all the group members in the group-chat:
    private ArrayList<String> groupMembers = new ArrayList<>();
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

        // --- Get username ---
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = prefs.getString("username", null);
        if (username == null) {
            Log.e("USERNAME_CHECK", "Username is null! Cannot connect to WebSocket.");
            return;
        }

        Log.d("USERNAME_CHECK", "Username = " + username);


        // --- Adapter ---
        messageAdapter = new MessageAdapter(messages, username);           // <-- NOW VALID
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);                   // <-- NOW VALID

        // --- Group info ---
        groupId = getIntent().getIntExtra("groupID", -1);
        groupName = getIntent().getStringExtra("groupName");
        CHAT_KEY = "group_" + groupId;

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

        reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(MessagesActivity.this, ReportUserActivity.class);
            intent.putExtra("reportedUser", "Group Chat: " + groupName);
            startActivity(intent);
        });

        // --- Connect to WebSocket ---
        String wsUrl = "ws://coms-3090-056.class.las.iastate.edu:8080/chat/"
                + groupId + "/" + username;

        Log.d("WS_URL", "Connecting to WebSocket URL: " + wsUrl);

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
            Log.d("WS_RECEIVER", "Intent received with key=" + intent.getStringExtra("key"));
            if (!CHAT_KEY.equals(intent.getStringExtra("key"))) return;

            String raw = intent.getStringExtra("message");
            Log.d("WS_RECEIVER", "Message content = " + raw);

            String sender = "Unknown";
            String content = raw;

            if (raw.contains(":")) {
                String[] parts = raw.split(":", 2);
                sender = parts[0].trim();
                content = parts[1].trim();
            } else {
                sender = "System";
                content = raw;
            }

            messages.add(new Message(sender, content));
            messageAdapter.notifyItemInserted(messages.size() - 1);
            messagesRecyclerView.scrollToPosition(messages.size() - 1);
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

    private void fetchGroupMembers() {
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/groups/" + groupName;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray users = response.getJSONArray("users");
                        groupMembers.clear();

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                            groupMembers.add(user.getString("username"));
                        }

                        Log.d("GROUP_MEMBERS", groupMembers.toString());

                    } catch (Exception e) {
                        Log.e("GROUP_MEMBERS", "Parsing error", e);
                    }
                },
                error -> Log.e("GROUP_MEMBERS", "Request error", error)
        );

        Volley.newRequestQueue(this).add(request);
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