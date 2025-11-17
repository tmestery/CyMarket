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

        groupNameInput = findViewById(R.id.groupNameInput);
        createGroupBtn = findViewById(R.id.createGroupBtn);

        createGroupBtn.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();

            if (groupName.isEmpty()) {
                Toast.makeText(CreateGroupActivity.this, "Enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            createGroupBtn.setEnabled(false);
            createGroup(groupName);
        });
    }

    private void createGroup(String groupName) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String encGroupName = URLEncoder.encode(groupName, StandardCharsets.UTF_8.toString());

                // Create group and get group ID
                URL url = new URL(BASE_URL + "/groups/create/" + encGroupName);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK
                        && responseCode != HttpURLConnection.HTTP_CREATED) {
                    runOnUiThread(() -> {
                        createGroupBtn.setEnabled(true);
                        Toast.makeText(CreateGroupActivity.this,
                                "Failed to create group (code " + responseCode + ")", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Read response
                String response;
                try (Scanner sc = new Scanner(conn.getInputStream())) {
                    response = sc.useDelimiter("\\A").hasNext() ? sc.next() : "";
                }

                if (response.isEmpty()) {
                    runOnUiThread(() -> {
                        createGroupBtn.setEnabled(true);
                        Toast.makeText(CreateGroupActivity.this,
                                "Empty response from server", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Strip quotes if backend returned a JSON string like "42"
                response = response.trim();
                if (response.startsWith("\"") && response.endsWith("\"")) {
                    response = response.substring(1, response.length() - 1);
                }

                int groupId = Integer.parseInt(response);

                // Add only the current user
                try {
                    String encUser = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
                    URL addUserUrl = new URL(BASE_URL + "/groups/group/add-user/" + groupId + "/" + encUser);
                    HttpURLConnection addUserConn = (HttpURLConnection) addUserUrl.openConnection();
                    addUserConn.setRequestMethod("POST");
                    addUserConn.setConnectTimeout(5000);
                    addUserConn.setReadTimeout(5000);
                    addUserConn.connect();

                    int code = addUserConn.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        System.out.println("Failed to add current user (code " + code + ")");
                    }

                    addUserConn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Save groupID and navigate to FriendsActivity
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit().putInt("currentGroupID", groupId).apply();

                runOnUiThread(() -> {
                    Intent intent = new Intent(CreateGroupActivity.this, FriendsActivity.class);
                    intent.putExtra("groupID", groupId);
                    intent.putExtra("groupName", groupName);
                    startActivity(intent);
                    finish(); // Prevent going back
                    Toast.makeText(CreateGroupActivity.this,
                            "Group created successfully!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    createGroupBtn.setEnabled(true);
                    Toast.makeText(CreateGroupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}