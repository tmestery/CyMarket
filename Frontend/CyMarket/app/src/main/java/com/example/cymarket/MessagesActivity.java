package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private String chatWith;
    private int groupId;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private Button sendButton;
    private EditText messageInput;

    private List<MessageModel> messages = new ArrayList<>();

    // Receiver to enable send button when WebSocket is ready
    private final BroadcastReceiver webSocketReadyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            if (key.equals(chatWith)) {
                sendButton.setEnabled(true);
            }
        }
    };

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            String message = intent.getStringExtra("message");
            if (key.equals(chatWith)) {
                messages.add(new MessageModel(message, false));
                adapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        chatWith = getIntent().getStringExtra("chat_with");

        recyclerView = findViewById(R.id.messagesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sendButton = findViewById(R.id.sendButton); // use class-level reference
        messageInput = findViewById(R.id.messageInput);

        adapter = new MessageAdapter(messages);
        recyclerView.setAdapter(adapter);

        chatWith = getIntent().getStringExtra("chat_with");
        groupId = getIntent().getIntExtra("groupId", -1);

        if (groupId == -1) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Disable send until WebSocket is ready
        sendButton.setEnabled(false);

        sendButton.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
                messageInput.setText("");
            }
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_buy) {
                startActivity(new Intent(MessagesActivity.this, BuyActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(MessagesActivity.this, SellActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(MessagesActivity.this, FriendsActivity.class));
                return true;
            }
            return false;
        });

        startWebSocket();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("WebSocketMessageReceived"));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(webSocketReadyReceiver, new IntentFilter("WebSocketReady"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(webSocketReadyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, WebSocketService.class);
        intent.setAction("DISCONNECT");
        intent.putExtra("key", chatWith);
        startService(intent);
    }

    private void startWebSocket() {
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        serviceIntent.setAction("CONNECT");
        serviceIntent.putExtra("url", "ws://coms-3090-056.class.las.iastate.edu:8080/chat/" + groupId + "/" + chatWith);
        serviceIntent.putExtra("key", chatWith);
        startService(serviceIntent);
    }

    private void sendMessage(String message) {
        messages.add(new MessageModel(message, true));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);

        Intent intent = new Intent("SendWebSocketMessage");
        intent.putExtra("key", chatWith);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}