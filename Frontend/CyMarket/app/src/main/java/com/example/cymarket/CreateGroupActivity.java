package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameInput;
    private Button createGroupBtn;
    private String username = "yourUsername"; // replace with logged-in user
    private static final String BASE_URL = "http://coms-3090-056.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

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
            try {
                // Create group
                URL url = new URL(BASE_URL + "/groups/create/" + groupName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    // Fetch group to get its ID
                    URL getUrl = new URL(BASE_URL + "/groups/" + groupName);
                    HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
                    getConn.setRequestMethod("GET");
                    getConn.connect();

                    if (getConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        java.util.Scanner sc = new java.util.Scanner(getConn.getInputStream());
                        String response = sc.useDelimiter("\\A").next();
                        JSONObject groupObj = new JSONObject(response);
                        int groupId = groupObj.getInt("id");

                        // Add logged-in user + friend to group
                        String[] usersToAdd = {username, friendUsername};
                        for (String user : usersToAdd) {
                            URL addUserUrl = new URL(BASE_URL + "/groups/group/add-user/" + groupId + "/" + user);
                            HttpURLConnection addUserConn = (HttpURLConnection) addUserUrl.openConnection();
                            addUserConn.setRequestMethod("POST");
                            addUserConn.connect();
                        }

                        Intent intent = new Intent(CreateGroupActivity.this, MessagesActivity.class);
                        intent.putExtra("chat_with", username);
                        intent.putExtra("groupId", groupId); // pass the correct group ID
                        startActivity(intent);

                        runOnUiThread(() ->
                                Toast.makeText(this, "Group created and joined successfully!", Toast.LENGTH_SHORT).show()
                        );
                    }

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}