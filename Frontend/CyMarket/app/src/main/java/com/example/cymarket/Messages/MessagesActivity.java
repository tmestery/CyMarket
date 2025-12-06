package com.example.cymarket.Messages;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cymarket.BuyActivity;
import com.example.cymarket.R;
import com.example.cymarket.Reporting.ReportUserActivity;
import com.example.cymarket.SellActivity;
import com.example.cymarket.WebSocketService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Activity for displaying and sending messages within a group chat.
 * <p>
 * Connects to a WebSocket service for real-time messaging and allows
 * reporting other users in the group. Displays messages in a RecyclerView.
 *
 * @author Tyler Mestery
 */
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

    /**
     * Initializes the activity, sets up views, adapter, WebSocket connection,
     * and bottom navigation.
     *
     * @param savedInstanceState The saved instance state bundle
     */
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

    /**
     * Displays a dialog allowing the user to report other group members.
     * Excludes the current user from the list.
     */
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

    /**
     * Broadcast receiver for messages received from the WebSocket service.
     * Adds incoming messages to the RecyclerView.
     */
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

    /**
     * Registers the WebSocket message broadcast receiver when the activity starts.
     */
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter("WS_MSG"));
    }

    /**
     * Unregisters the WebSocket message broadcast receiver when the activity stops.
     */
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(receiver);
    }

    /**
     * Fetches all members of the current group from the backend.
     * Updates the UI to show all members except the current user.
     */
    private void fetchGroupMembers() {
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/getMembers/" + groupId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        Log.d("GROUP_MEMBERS", "RAW RESPONSE:\n" + response);

                        JSONArray users = new JSONArray(response);
                        groupMembers.clear();

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                            String uname = user.getString("username");

                            if (!uname.equals(username)) {
                                groupMembers.add(uname);
                            }
                        }

                        if (groupMembers.isEmpty()) {
                            groupchatPerson.setText("No other members");
                        } else {
                            groupchatPerson.setText(String.join(", ", groupMembers));
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