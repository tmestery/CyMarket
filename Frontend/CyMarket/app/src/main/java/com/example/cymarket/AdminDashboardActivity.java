package com.example.cymarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private Button salesButton;
    private Button usersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Set buttons:
        salesButton = findViewById(R.id.sales_btn);
        usersButton = findViewById(R.id.users_btn);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Set sales and users info displays that will be hidden/brought out:
        LinearLayout bottomNaveSales = findViewById(R.id.bottom_nav_sales);
        LinearLayout bottomNaveSales2 = findViewById(R.id.bottom_nav_sales_2);
        LinearLayout bottomNavUsers = findViewById(R.id.bottom_nav_users);

        // Setting and getting all users on screen
        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchUsers();

        usersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavUsers.setVisibility(View.VISIBLE);
                bottomNaveSales.setVisibility(View.INVISIBLE);
                bottomNaveSales2.setVisibility(View.INVISIBLE);
            }
        });

        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavUsers.setVisibility(View.INVISIBLE);
                bottomNaveSales.setVisibility(View.VISIBLE);
                bottomNaveSales2.setVisibility(View.VISIBLE);
            }
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, AdminSettingsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, AdminProfilesActivity.class));
                return true;
            }
            return false;
        });
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
                        Intent intent = new Intent(AdminDashboardActivity.this, MessagesActivity.class);
                        intent.putExtra("chat_with", user.getUsername());
                        startActivity(intent);
                    });

                    // ⚡ Set adapter on RecyclerView here
                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(AdminDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}