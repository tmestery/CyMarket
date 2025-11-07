package com.example.cymarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminSettingsActivity extends AppCompatActivity {
    private Button logoutButton;
    private Button removePFP;
    private TextView usernameText;
    private TextView emailText;
    private TextView firstLastNameText;
    private TextView passwordText;
    private static final String BASE_URL = "http://coms-3090-056.class.las.iastate.edu:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        logoutButton = findViewById(R.id.logout_btn);
        removePFP = findViewById(R.id.remove_pfp_btn);

        usernameText = findViewById(R.id.username_text);
        emailText = findViewById(R.id.email_text);
        firstLastNameText = findViewById(R.id.first_last_name_label);
        passwordText = findViewById(R.id.password_text);

        // Get user info passed from previous activity
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String email = getIntent().getStringExtra("email");

        usernameText.setText("Username: DanielAdminn");
        emailText.setText("Email: admin@gmai.com");
        firstLastNameText.setText("Name: Daniel");
        passwordText.setText("Password: Codxe2027");

//        usernameText.setText("Username: " + username);
//        emailText.setText("Email: " + email);
//        firstLastNameText.setText("Name: ");
//        passwordText.setText("Password: " + password);

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
                if (response.isSuccessful() && response.body() != null) {
                    firstLastNameText.setText("Name: " + response.body());
                } else {
                    firstLastNameText.setText("Name: Daniel");
                    Log.e("GET_NAME", "Failed to get name. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                firstLastNameText.setText("Name: Daniel");
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

        // Logout button
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminSettingsActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Remove profile picture
        removePFP.setOnClickListener(v -> {
            Call<String> removePfpCall = apiService.deleteProfileImage(username);
            removePfpCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AdminSettingsActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AdminSettingsActivity.this, AdminProfilesActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(AdminSettingsActivity.this, "Failed to remove profile picture", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(AdminSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}