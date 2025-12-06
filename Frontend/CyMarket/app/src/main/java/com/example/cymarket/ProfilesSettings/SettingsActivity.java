package com.example.cymarket.ProfilesSettings;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cymarket.ApiService;
import com.example.cymarket.BuyActivity;
import com.example.cymarket.LoginSignup.LoginActivity;
import com.example.cymarket.Messages.GroupListActivity;
import com.example.cymarket.R;
import com.example.cymarket.SellActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Activity for user settings, including account deletion, profile picture removal,
 * and logout functionality. Displays user information such as username, email,
 * and password.
 *
 * @author Tyler Mestery
 */
public class SettingsActivity extends AppCompatActivity {
    private Button deleteAccount;
    private Button removePFP;
    private TextView usernameText;
    private TextView emailText;
    private Button logoutButton;
    private TextView passwordText;

    private static final String BASE_URL = "http://coms-3090-056.class.las.iastate.edu:8080/";

    /**
     * Initializes the settings activity, sets up UI elements, Retrofit calls,
     * and bottom navigation.
     *
     * @param savedInstanceState Bundle containing saved state if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize the top AppBar
        MaterialToolbar topAppBar = findViewById(R.id.settings_top_appbar);
        topAppBar.setNavigationOnClickListener(v -> {
            // Optional: handle navigation icon click (e.g., go back)
            finish();
        });

        deleteAccount = findViewById(R.id.delete_btn);
        removePFP = findViewById(R.id.remove_pfp_btn);
        logoutButton = findViewById(R.id.logout_btn);

        usernameText = findViewById(R.id.username_text);
        emailText = findViewById(R.id.email_text);
        passwordText = findViewById(R.id.password_text);

        // Get user info passed from previous activity
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String email = getIntent().getStringExtra("email");

        usernameText.setText("Username: " + username);
        emailText.setText("Email: " + email);
        passwordText.setText("Password: " + password);

        // Retrofit instance
        Retrofit retrofitScalars = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
                .build();
        ApiService apiService = retrofitScalars.create(ApiService.class);

        // GET user's full name
        Call<String> getNameCall = apiService.getName(email);
        getNameCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });

        // DELETE account
        deleteAccount.setOnClickListener(v -> {
            Retrofit retrofitGson = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService apiServiceDelete = retrofitGson.create(ApiService.class);

            Call<Void> deleteCall = apiServiceDelete.deleteUser(username);
            deleteCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        Log.d("DeleteUser", "User deleted successfully");

                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Failed to delete account. Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e("DeleteUser", "Failed to delete user: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(SettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DeleteUser", "Error: " + t.getMessage());
                }
            });
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Remove profile picture
        removePFP.setOnClickListener(v -> {
            Call<String> removePfpCall = apiService.deleteProfileImage(username);
            removePfpCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(SettingsActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SettingsActivity.this, ProfilesActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Failed to remove profile picture", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(SettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        /**
         * Sets up the bottom navigation bar to switch between Buy, Sell, and Chat pages.
         */
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_buy) {
                startActivity(new Intent(SettingsActivity.this, BuyActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(SettingsActivity.this, SellActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(SettingsActivity.this, GroupListActivity.class));
                return true;
            }
            return false;
        });
    }
}