package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import retrofit2.Response;
import android.widget.EditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsActivity extends AppCompatActivity {
    private Button homeButton;  // define profile button variable
    private Button profileButton;  // define settings button variable
    private Button deleteAccount; // define account deletion button here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        homeButton = findViewById(R.id.stngs_home_btn);
        profileButton = findViewById(R.id.stngs_prfile_btn);
        deleteAccount = findViewById(R.id.delete_btn);

        String email = getIntent().getStringExtra("email");

        // Click listener for when delete button is pressed:
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make the request to delete the account
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://coms-3090-056.class.las.iastate.edu:8080/") // ✅ base URL should end with /
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiService apiService = retrofit.create(ApiService.class);

                Call<Void> call = apiService.deleteUser(email); // or userId depending on what your backend expects

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("DeleteUser", "User deleted successfully");
                        } else {
                            Log.e("DeleteUser", "Failed to delete user: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("DeleteUser", "Error: " + t.getMessage());
                    }
                });

                // Change screen when the request has been fulfilled - account deleted
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Click listener on home button pressed:
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Click listener on profile button pressed:
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ProfilesActivity.class);
                startActivity(intent);
            }
        });
    }
}