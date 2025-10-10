package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsActivity extends AppCompatActivity {
    private Button homeButton;
    private Button profileButton;
    private Button deleteAccount;
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
        setContentView(R.layout.activity_settings);

        homeButton = findViewById(R.id.stngs_home_btn);
        profileButton = findViewById(R.id.stngs_prfile_btn);
        deleteAccount = findViewById(R.id.delete_btn);
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

        usernameText.setText("Username: " + username);
        emailText.setText("Email: " + email);
        firstLastNameText.setText("Name: ");
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
                if (response.isSuccessful() && response.body() != null) {
                    firstLastNameText.setText("Name: " + response.body());
                } else {
                    firstLastNameText.setText("Name: Not found");
                    Log.e("GET_NAME", "Failed to get name. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                firstLastNameText.setText("Name: Error");
                Log.e("GET_NAME", "Error: " + t.getMessage());
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

        // Home button
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Profile button
        profileButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String savedEmail = prefs.getString("email", email);
            String savedPassword = prefs.getString("password", password);
            Intent intent = new Intent(SettingsActivity.this, ProfilesActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("email", savedEmail);
            intent.putExtra("password", savedPassword);
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
    }
}