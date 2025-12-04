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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessagesActivity extends AppCompatActivity {

    // A list of all the group members in the group-chat:
    private ArrayList<String> groupMembers = new ArrayList<>();
    private TextView groupchatPerson;
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
        groupchatPerson = findViewById(R.id.groupchatPerson);
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

        reportButton.setOnClickListener(v -> showReportUserDialog());

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

        // Collect a list of users in the group:
        fetchGroupMembers();
    }

    // A list of users, the user is able to report:
    private void showReportUserDialog() {
        // Filter out yourself
        ArrayList<String> reportableUsers = new ArrayList<>();
        for (String user : groupMembers) {
            if (!user.equals(username)) {
                reportableUsers.add(user);
            }
        }

        if (reportableUsers.isEmpty()) {
            Log.e("REPORT", "No users available to report");
            return;
        }

        String[] userArray = reportableUsers.toArray(new String[0]);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Report User")
                .setItems(userArray, (dialog, which) -> {
                    String selectedUser = userArray[which];

                    Intent intent = new Intent(MessagesActivity.this, ReportUserActivity.class);
                    intent.putExtra("reportedUser", selectedUser);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    // Parse for group members, since no exact API was given to collect the list...
    private ArrayList<String> parseGroupMembers(
            String response,
            int targetGroupId,
            String currentUsername
    ) {
        ArrayList<String> members = new ArrayList<>();

        try {
            JSONArray groups = new JSONArray(response);

            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);

                int groupId = group.getInt("id");
                if (groupId != targetGroupId) continue;

                JSONArray users = group.getJSONArray("users");

                for (int j = 0; j < users.length(); j++) {
                    JSONObject user = users.getJSONObject(j);

                    String username = user.optString("username", null);
                    if (username == null) continue;

                    // Don’t allow reporting yourself
                    if (username.equals(currentUsername)) continue;

                    members.add(username);
                }

                break;
            }
        } catch (Exception e) {
            Log.e("GROUP_MEMBERS", "Parsing failed", e);
        }

        return members;
    }

    private void fetchGroupMembers() {
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/groups";

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        Log.d("GROUP_MEMBERS", "RAW RESPONSE:\n" + response);

                        JSONObject group = new JSONObject(response);
                        JSONArray users = group.getJSONArray("users");

                        groupMembers.clear();

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                            String uname = user.getString("username");

                            Log.d("GROUP_MEMBERS", "Found user: " + uname);

                            // skip yourself
                            if (!uname.equals(username)) {
                                groupMembers.add(uname);
                            }
                        }

                        Log.d("GROUP_MEMBERS", "FINAL MEMBER LIST: " + groupMembers);

                        // build a comma-separated string
                        String membersText = String.join(", ", groupMembers);

                        // if empty, show fallback
                        if (membersText.isEmpty()) {
                            groupchatPerson.setText("No other members");
                        } else {
                            groupchatPerson.setText(membersText);
                        }
                    } catch (Exception e) {
                        Log.e("GROUP_MEMBERS", "Parsing error", e);
                    }
                },
                error -> Log.e("GROUP_MEMBERS", "Request failed", error)
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