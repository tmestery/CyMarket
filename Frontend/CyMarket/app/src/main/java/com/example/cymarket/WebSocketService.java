package com.example.cymarket;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WebSocketService extends Service {

    private final Map<String, WebSocketClient> sockets = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(sendReceiver, new IntentFilter("WS_SEND"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WS", "onStartCommand: action=" + (intent != null ? intent.getAction() : "null") +
                ", key=" + (intent != null ? intent.getStringExtra("key") : "null"));
        if (intent == null) return START_STICKY;

        String action = intent.getAction();
        String key = intent.getStringExtra("key");

        if ("WS_CONNECT".equals(action)) {
            connect(key, intent.getStringExtra("url"));
        } else if ("WS_DISCONNECT".equals(action)) {
            disconnect(key);
        }

        return START_STICKY;
    }

    private void connect(String key, String url) {
        Log.d("WS", "Attempting to connect: key=" + key + ", url=" + url);
        if (sockets.containsKey(key)) {
            Log.d("WS", "Already connected for key: " + key);
            return;
        }

        WebSocketClient client = new WebSocketClient(URI.create(url)) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                Log.d("WS", "Connected: " + key);
                Intent ready = new Intent("WS_READY");
                ready.putExtra("key", key);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(ready);
            }

            @Override
            public void onMessage(String msg) {
                Log.d("WS", "Received: " + msg);
                Intent intent = new Intent("WS_MSG");
                intent.putExtra("key", key);
                intent.putExtra("message", msg);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d("WS", "Closed: " + key + " Reason: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.e("WS", "Error: " + ex.getMessage());
            }
        };

        sockets.put(key, client);
        client.connect();
    }

    private final BroadcastReceiver sendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            String msg = intent.getStringExtra("message");

            WebSocketClient ws = sockets.get(key);
            if (ws != null) {
                if (ws.isOpen()) {
                    ws.send(msg);
                    Log.d("WS", "Sent: " + msg);
                } else {
                    Log.d("WS", "WS not open yet, message not sent");
                }
            }
        }
    };

    private void disconnect(String key) {
        WebSocketClient ws = sockets.get(key);
        if (ws != null) {
            ws.close();
            sockets.remove(key);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}