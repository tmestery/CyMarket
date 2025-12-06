package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.cymarket.ProfilesSettings.ProfilesActivity;
import com.google.gson.Gson;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private TextView messageText;    // establish messagetext variable

    private Button backButton;       // establish backbutton variable

    private String username;
    private final Gson gson = new Gson(); // JSON parser for WebSocket messages

    private RecyclerView recyclerView;

    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();


    // BroadcastReceiver to handle WebSocket messages
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            String message = intent.getStringExtra("message");

            if ("notifications".equals(key) && message.startsWith("NOTIFICATION:")) {
                String json = message.substring("NOTIFICATION:".length());
                try {
                    Notification notification = gson.fromJson(json, Notification.class);
                    Toast.makeText(context, "notif " + notification.message, Toast.LENGTH_LONG).show();
                    notificationList.add(0, notification);
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                } catch (Exception e) {
                    Toast.makeText(context, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    // makes sure socket is ready for new notif
    private final BroadcastReceiver socketReadyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            if ("notifications".equals(key)) {
                Toast.makeText(context, "WebSocket connected", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications); // notifications activity xml

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
         username = prefs.getString("username", null);

        // links to xml
        messageText = findViewById(R.id.notifs_msg_txt);
        backButton = findViewById(R.id.notifs_back_btn);
        recyclerView = findViewById(R.id.notifications_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        fetchPastNotifications(); // loads notif history


        // establish listener for back button functionality
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationsActivity.this, ProfilesActivity.class);
                startActivity(intent);
            }
        });

        // start WebSocket connection for notifications
        connectToWebSocket();
    }

    private void connectToWebSocket() {
        Intent connectIntent = new Intent(this, WebSocketService.class);
        connectIntent.setAction("WS_CONNECT");
        connectIntent.putExtra("key", "notifications");
        connectIntent.putExtra("url", "ws://coms-3090-056.class.las.iastate.edu:8080/notifications/" + username);
        Log.d("Connecting to websocket", "Connected");
        startService(connectIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // register receiver to listen for incoming WebSocket messages for live update
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(notificationReceiver, new IntentFilter("WS_MSG"));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(notificationReceiver, new IntentFilter("WS_READY"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // unregister receiver to avoid memory leaks
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(notificationReceiver);

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(socketReadyReceiver);
    }

    private void fetchPastNotifications() {
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/notifications/" + username;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            Notification n = gson.fromJson(obj.toString(), Notification.class);
                            notificationList.add(n);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error parsing notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to load notifications: " + error.toString(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }



}
