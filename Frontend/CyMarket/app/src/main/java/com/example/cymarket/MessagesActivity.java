package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MessagesActivity extends AppCompatActivity {

    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        String chatWith = getIntent().getStringExtra("chat_with");
        if (chatWith != null) {
            setTitle("Chat with " + chatWith);
        }

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
                return true;
            }
            return false;
        });

        if (chatWith != null) {
            String wsUrl = "ws://coms-3090-056.class.las.iastate.edu:8080/chat/" + chatWith;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(wsUrl).build();

            webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    runOnUiThread(() -> {
                        // TODO: display message in chat
                    });
                }
            });
        }
    }
}