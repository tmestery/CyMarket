package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cymarket.Messages.GroupListActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private RequestQueue queue;

    private static final String BASE_URL = "http://coms-3090-056.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        queue = Volley.newRequestQueue(this);

        // Try to load username and email
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String tempUsername = getIntent().getStringExtra("username");
        if (tempUsername == null) {
            tempUsername = prefs.getString("username", null);
        }
        String email = prefs.getString("email", null);

        // Fetch from backend if username missing but email present
        if ((tempUsername == null || tempUsername.isEmpty()) && email != null) {
            fetchUsername(email);
        }

        final String username = tempUsername;

        // Set up the top app bar
        MaterialToolbar topAppBar = findViewById(R.id.top_appbar);
        topAppBar.setNavigationOnClickListener(v -> {
            // Navigation icon (profile) click
            String username1 = prefs.getString("username", null);
            String email1 = prefs.getString("email", null);
            String password1 = prefs.getString("password", null);

            Intent intent = new Intent(MainActivity.this, ProfilesActivity.class);
            intent.putExtra("username", username1);
            intent.putExtra("email", email1);
            intent.putExtra("password", password1);
            startActivity(intent);
        });

        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String username1 = prefs.getString("username", null);
                String email1 = prefs.getString("email", null);
                String password = prefs.getString("password", null);

                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
                return true;
            }
            return false;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_buy) {
                startActivity(new Intent(MainActivity.this, BuyActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(MainActivity.this, SellActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(MainActivity.this, GroupListActivity.class));
                return true;
            }
            return false;
        });
    }

    private void fetchUsername(String email) {
        String url = BASE_URL + "/users/getName/" + email;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    String username = response.replace("\"", "").trim();

                    // Save username in prefs
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", username);
                    editor.apply();

                    Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Failed to fetch username", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}