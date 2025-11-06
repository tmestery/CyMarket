package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MessagesActivity extends AppCompatActivity {
    private Button sendButton;
    private EditText messageInput;
    private TextView groupchatPersonName;
    private TextView groupChatName;
    private TextView messagesRecyclerView;
    private Button reportButton;
    private static final String CHAT_KEY = "groupChat";
    private static final String TAG = "MessagesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // These are the intents that are being passed:
        //  intent.putExtra("chat_with", username);
        //  intent.putExtra("groupId", groupId); // pass the correct group ID
        //        String groupchatName = getIntent().getStringExtra("groupID");
        //        String friendUsername = getIntent().getStringExtra("friendUsername");

        reportButton = findViewById(R.id.reportButton);
        sendButton = findViewById(R.id.sendButton);
        messageInput = findViewById(R.id.messageInput);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        groupchatPersonName = findViewById(R.id.groupchatPerson);
        groupChatName = findViewById(R.id.groupChatName);

        String groupId = getIntent().getStringExtra("groupId");
        String friendUsername = getIntent().getStringExtra("friendUsername");

        // Add a fetch function for groupID using one of these on backend:
//    @GetMapping(path = "/getGroups")
//    public List<Group> getGroup(){
//        List<Group> temp= groupRepository.findAll();
//        return temp;
//    }
//    @GetMapping(path = "/{name}")
//    public Group getGroups(@PathVariable String name){
//        return groupRepository.findByName(name);
//    }
        // i think that one of these backend endpoints will return group ID then i will be able to proeprly make websocket work!

        // Set name of group-chat in UI
        groupChatName.setText(groupId); // or fetch group name if needed

        // Set name of person in group-chat UI
        groupchatPersonName.setText(friendUsername);

        sendButton.setOnClickListener(v -> {
            if (!socketReady) {
                Log.w(TAG, "Socket not ready yet");
                return;
            }
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

            // pass the user being reported
            intent.putExtra("reportedUser", groupchatPersonName.getText().toString());
            startActivity(intent);
        });

        // Connect when we arrive here
        //  String wsUrl = "ws://coms-3090-056.class.las.iastate.edu:8080/chat/"
        //          + getIntent().getStringExtra("groupId") + "/"
        //          + getIntent().getStringExtra("friendUsername");
        String wsUrl = "ws://coms-3090-056.class.las.iastate.edu:8080/chat/"
                + groupId + "/"
                + friendUsername;

        Intent serviceIntent = new Intent(this, WebSocketService.class);
        serviceIntent.setAction("WS_CONNECT");
        serviceIntent.putExtra("key", CHAT_KEY);
        serviceIntent.putExtra("url", wsUrl);
        startService(serviceIntent);
        setupBottomNav();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CHAT_KEY.equals(intent.getStringExtra("key"))) {
                String msg = intent.getStringExtra("message");
                runOnUiThread(() -> {
                    messagesRecyclerView.append("\n" + msg);
                });
            }
        }
    };

    private boolean socketReady = false;

    private final BroadcastReceiver socketReadyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CHAT_KEY.equals(intent.getStringExtra("key"))) {
                socketReady = true;
                runOnUiThread(() -> sendButton.setEnabled(true));
                Log.d(TAG, "WebSocket ready!");
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter("WS_MSG"));
        lbm.registerReceiver(socketReadyReceiver, new IntentFilter("WS_READY"));
        sendButton.setEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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