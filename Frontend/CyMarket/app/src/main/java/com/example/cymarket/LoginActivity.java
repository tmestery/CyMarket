package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signupButton;
    private TextView forgotPassword;
    private TextView secretAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.login_email_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);
        signupButton = findViewById(R.id.login_signup_txt);
        forgotPassword = findViewById(R.id.forgot_password);
        secretAdmin = findViewById(R.id.admin_hidden_entry);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                checkUserCredentials(email, password);
            }
        });

        forgotPassword.setOnClickListener(v -> {
            Intent intent2 = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent2);
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        secretAdmin.setOnClickListener(new View.OnClickListener() {
            int tapCount = 0;

            @Override
            public void onClick(View v) {
                tapCount++;
                if (tapCount == 10) {
                    tapCount = 0;
                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void checkUserCredentials(String email, String password) {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8);

        String url = "http://coms-3090-056.class.las.iastate.edu:8080/login/e/"
                + encodedEmail + "/" + encodedPassword;

        JsonObjectRequest loginRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String fetchedEmail = response.optString("emailId", email);

                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        prefs.edit().putString("email", fetchedEmail).apply();
                        prefs.edit().putString("password", password).apply();

                        fetchUserTypeAndRedirect(fetchedEmail);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error logging in", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "Invalid Email or Password", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(loginRequest);
    }

    private void fetchUserTypeAndRedirect(String email) {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String typeUrl = "http://coms-3090-056.class.las.iastate.edu:8080/userslogin/getUserType/" + encodedEmail;

        com.android.volley.toolbox.StringRequest typeRequest = new com.android.volley.toolbox.StringRequest(
                Request.Method.GET,
                typeUrl,
                response -> {
                    if (response == null || response.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Empty response from server", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String cleanedResponse = response.replaceAll("\"", "").trim().toLowerCase();
                    boolean isAdmin = cleanedResponse.equals("n"); // n means admin from backend
                    Intent intent = new Intent(
                            LoginActivity.this,
                            isAdmin ? AdminDashboardActivity.class : MainActivity.class
                    );
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Couldn't fetch user type", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(typeRequest);
    }
}