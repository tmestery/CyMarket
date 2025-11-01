package com.example.cymarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private Button messagesButton; // set messages button
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        messagesButton = findViewById(R.id.messages_btn);
        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchUsers();
    }

    private void fetchUsers() {
        ApiService apiService = RetroClient.getApiService();
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();

                    // Set profile image URL
                    for (User user : users) {
                        user.setProfileImageUrl("http://coms-3090-056.class.las.iastate.edu:8080/users/" + user.getUsername() + "/profile-image");
                    }

                    adapter = new FriendsAdapter(users, user -> {
                        Intent intent = new Intent(FriendsActivity.this, MessagesActivity.class);
                        intent.putExtra("chat_with", user.getUsername());
                        startActivity(intent);
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

        // Click listener on messages button pressed:
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, MessagesActivity.class);
                startActivity(intent);
            }
        });
    }
}