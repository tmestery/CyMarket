package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import retrofit2.Response;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsActivity extends AppCompatActivity {
    private Button homeButton;  // define profile button variable
    private Button profileButton;  // define settings button variable
    private Button deleteAccount; // define account deletion button here
    private Button logoutButton; // defines the logout button
    private TextView usernameText;
    private TextView emailText;
    private TextView firstLastNameText;
    private TextView passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        homeButton = findViewById(R.id.stngs_home_btn);
        profileButton = findViewById(R.id.stngs_prfile_btn);
        deleteAccount = findViewById(R.id.delete_btn);
        logoutButton = findViewById(R.id.logout_btn);

        // Intializing the TextViews that display account info:
        usernameText = findViewById(R.id.username_text);
        emailText = findViewById(R.id.email_text);
        firstLastNameText = findViewById(R.id.first_last_name_label);
        passwordText = findViewById(R.id.password_text);

        // Setting the TextViews to contain correct info:
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String email = getIntent().getStringExtra("email");

        usernameText.setText("Username: " + username);
        emailText.setText("Email: " + email);
        firstLastNameText.setText("Name: ");
        passwordText.setText("Password: " + password);

        deleteAccount.setOnClickListener(v -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://coms-3090-056.class.las.iastate.edu:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);

            Call<Void> call = apiService.deleteUser(username);

            call.enqueue(new Callback<Void>() {
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

        // Click listener on home button pressed:
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Click listener to logout the user, sending them to the login screen:
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Click listener on profile button pressed:
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fixing the encoding error for join date with:
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String email = prefs.getString("email", null);
                String password = prefs.getString("password", null);
                Intent intent = new Intent(SettingsActivity.this, ProfilesActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        });
    }
}