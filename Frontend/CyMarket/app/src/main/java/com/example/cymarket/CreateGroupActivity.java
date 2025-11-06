package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameInput;
    private Button createGroupBtn;
    private String username;
    private static final String BASE_URL = "http://coms-3090-056.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "");
        String friendUsername = getIntent().getStringExtra("friendUsername");

        groupNameInput = findViewById(R.id.groupNameInput);
        createGroupBtn = findViewById(R.id.createGroupBtn);

        createGroupBtn.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();

            if (groupName.isEmpty()) {
                Toast.makeText(this, "Enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            createGroupBtn.setEnabled(false);
            createGroup(groupName, friendUsername);
        });
    }

    private void createGroup(String groupName, String friendUsername) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String encGroupName = URLEncoder.encode(groupName, StandardCharsets.UTF_8.toString());

                // Create group and directly get group ID (backend returns int)
                URL url = new URL(BASE_URL + "/groups/create/" + encGroupName);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK
                        && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                    final int code = conn.getResponseCode();
                    runOnUiThread(() -> {
                        createGroupBtn.setEnabled(true);
                        Toast.makeText(this, "Failed to create group (code " + code + ")", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Read integer response (group ID)
                String response;
                try (Scanner sc = new Scanner(conn.getInputStream())) {
                    response = sc.useDelimiter("\\A").hasNext() ? sc.next() : "";
                }

                if (response.isEmpty()) {
                    runOnUiThread(() -> {
                        createGroupBtn.setEnabled(true);
                        Toast.makeText(this, "Empty response from server", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                int groupId = Integer.parseInt(response.trim());

                // Add users (creator + friend)
                List<String> usersToAdd = new ArrayList<>();
                if (username != null && !username.isEmpty()) usersToAdd.add(username);
                if (friendUsername != null && !friendUsername.isEmpty()) usersToAdd.add(friendUsername);

                for (String user : usersToAdd) {
                    String encUser = URLEncoder.encode(user, StandardCharsets.UTF_8.toString());
                    URL addUserUrl = new URL(BASE_URL + "/groups/group/add-user/" + groupId + "/" + encUser);
                    HttpURLConnection addUserConn = (HttpURLConnection) addUserUrl.openConnection();
                    addUserConn.setRequestMethod("POST");
                    addUserConn.connect();
                    addUserConn.getResponseCode();
                    addUserConn.disconnect();
                }

                // Save groupID and move to MessagesActivity
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit().putInt("currentGroupID", groupId).apply();

                runOnUiThread(() -> {
                    Intent intent = new Intent(CreateGroupActivity.this, MessagesActivity.class);
                    intent.putExtra("groupID", groupId);
                    intent.putExtra("groupName", groupName);
                    startActivity(intent);
                    Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    createGroupBtn.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}