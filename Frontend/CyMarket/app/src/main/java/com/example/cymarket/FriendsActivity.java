package com.example.cymarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsActivity extends AppCompatActivity {

    private Button messagesButton;
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private int groupID;
    private String groupName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        messagesButton = findViewById(R.id.messages_btn);
        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupID = getIntent().getIntExtra("groupID", -1);
        groupName = getIntent().getStringExtra("groupName");

        if (groupID == -1 || groupName == null) {
            Toast.makeText(this, "Error: Missing group info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchUsers();

        messagesButton.setOnClickListener(v -> {
            // Navigate to MessagesActivity with current group
            Intent intent = new Intent(FriendsActivity.this, MessagesActivity.class);
            intent.putExtra("groupID", groupID);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUsers() {
        ApiService apiService = RetroClient.getApiService();
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();

                    // Assign each user's profile image URL
                    for (User user : users) {
                        user.setProfileImageUrl(
                                "http://coms-3090-056.class.las.iastate.edu:8080/users/"
                                        + user.getUsername()
                                        + "/profile-image"
                        );
                    }

                    // Set up RecyclerView adapter
                    adapter = new FriendsAdapter(users, user -> {
                        // Add user to the current group
                        addUserToGroup(groupID, user.getUsername());
                    });

                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(FriendsActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(FriendsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUserToGroup(int groupID, String username) {
        ApiService apiService = RetroClient.getApiService();
        apiService.addUserToGroup(groupID, username).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(FriendsActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FriendsActivity.this, username + " added to group!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(FriendsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}