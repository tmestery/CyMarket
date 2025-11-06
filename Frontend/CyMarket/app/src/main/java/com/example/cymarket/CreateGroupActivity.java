package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
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

        // assign to the class field (don't shadow)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "");
        String friendUsername = getIntent().getStringExtra("friendUsername");

        groupNameInput = findViewById(R.id.groupNameInput);
        createGroupBtn = findViewById(R.id.createGroupBtn);

        createGroupBtn.setOnClickListener(view -> {
            String groupName = groupNameInput.getText().toString().trim();
            if (!groupName.isEmpty()) {
                createGroup(groupName, friendUsername);
            } else {
                Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGroup(String groupName, String friendUsername) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            HttpURLConnection getConn = null;
            try {
                // URL-encode path segments
                String encGroupName = URLEncoder.encode(groupName, StandardCharsets.UTF_8.toString());

                // Create group
                URL url = new URL(BASE_URL + "/groups/create/" + encGroupName);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "*/*"); // accept any response type
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();

                // consume the response safely
                int createCode;
                try {
                    createCode = conn.getResponseCode();
                    // read and discard the response to avoid EOF
                    try (Scanner sc = new Scanner(conn.getInputStream())) {
                        while (sc.hasNext()) sc.next();
                    }
                } catch (Exception e) {
                    // fallback if server sends no body
                    createCode = conn.getResponseCode();
                }

                if (createCode == HttpURLConnection.HTTP_OK || createCode == HttpURLConnection.HTTP_CREATED) {

                    // Fetch group to get its ID (by name)
                    URL getUrl = new URL(BASE_URL + "/groups/" + encGroupName);
                    getConn = (HttpURLConnection) getUrl.openConnection();
                    getConn.setRequestMethod("GET");
                    getConn.connect();

                    if (getConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        String response;
                        try (Scanner sc = new Scanner(getConn.getInputStream())) {
                            response = sc.useDelimiter("\\A").hasNext() ? sc.next() : "";
                        }

                        if (response.isEmpty()) {
                            runOnUiThread(() -> Toast.makeText(this, "Empty response when fetching group", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        JSONObject groupObj = new JSONObject(response);
                        int groupId = 11;

                        // Build list of users to add
                        List<String> usersToAdd = new ArrayList<>();
                        if (username != null && !username.isEmpty()) usersToAdd.add(username);
                        if (friendUsername != null && !friendUsername.isEmpty()) usersToAdd.add(friendUsername);

                        for (String user : usersToAdd) {
                            String encUser = URLEncoder.encode(user, StandardCharsets.UTF_8.toString());
                            URL addUserUrl = new URL(BASE_URL + "/groups/group/add-user/" + groupId + "/" + encUser);
                            HttpURLConnection addUserConn = (HttpURLConnection) addUserUrl.openConnection();
                            addUserConn.setRequestMethod("POST");
                            addUserConn.connect();

                            int code = addUserConn.getResponseCode();
                            addUserConn.disconnect();

                            // Optionally: handle non-200 codes
                            if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_CREATED) {
                                final String errMsg = "Failed to add user " + user + " (code " + code + ")";
                                runOnUiThread(() -> Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show());
                            }
                        }

                        // Start MessagesActivity on UI thread and pass group info
                        runOnUiThread(() -> {
                            Intent intent = new Intent(CreateGroupActivity.this, MessagesActivity.class);
                            // pass both forms used in your app to be safe
                            intent.putExtra("groupID", String.valueOf(groupId));
                            intent.putExtra("friendUsername", friendUsername != null ? friendUsername : "");
                            startActivity(intent);

                            Toast.makeText(this, "Group created and joined successfully!", Toast.LENGTH_SHORT).show();
                        });

                    } else {
                        final int gc = getConn.getResponseCode();
                        runOnUiThread(() -> Toast.makeText(this, "Failed to fetch created group (code " + gc + ")", Toast.LENGTH_SHORT).show());
                    }

                } else {
                    final int cc = createCode;
                    runOnUiThread(() -> Toast.makeText(this, "Failed to create group (code " + cc + ")", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                if (conn != null) conn.disconnect();
                if (getConn != null) getConn.disconnect();
            }
        }).start();
    }
}