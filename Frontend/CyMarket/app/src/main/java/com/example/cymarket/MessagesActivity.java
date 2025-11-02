package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MessagesActivity extends AppCompatActivity {
    private Button sendButton;
    private EditText messageInput;
    private TextView messagesRecyclerView; // For now keeping simple – swap later with recycler
    private static final String CHAT_KEY = "groupChat";
    private static final String TAG = "MessagesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        sendButton = findViewById(R.id.sendButton);
        messageInput = findViewById(R.id.messageInput);
        messagesRecyclerView = findViewById(R.id.messagesTitle);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty()) return;

            Intent intent = new Intent("SendWebSocketMessage");
            intent.putExtra("key", CHAT_KEY);
            intent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            messageInput.setText("");
        });

        // Connect when we arrive here
        String wsUrl = "ws://YOUR_SERVER/chat/" + getIntent().getStringExtra("groupName");

        Intent serviceIntent = new Intent(this, WebSocketService.class);
        serviceIntent.setAction("CONNECT");
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

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("WebSocketMessageReceived")
        );
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