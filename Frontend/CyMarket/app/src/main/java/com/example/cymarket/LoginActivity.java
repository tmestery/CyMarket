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
import org.json.JSONException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText; // define username edittext variable
    private EditText passwordEditText; // define password edittext variable
    private Button loginButton;  // define login button variable
    private Button signupButton;  // define signup button variable
    private TextView forgotPassword;
    private TextView secretAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* initialize UI elements */
        emailEditText = findViewById(R.id.login_email_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);
        signupButton = findViewById(R.id.login_signup_btn);
        forgotPassword = findViewById(R.id.forgot_password);
        secretAdmin = findViewById(R.id.admin_hidden_entry);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter both email and password", Toast.LENGTH_SHORT).show();
                } else {
                    checkUserCredentials(email, password);
                }
            }
        });

        /* click listener on forgot password button */
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent2 = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent2);
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        secretAdmin.setOnClickListener(new View.OnClickListener() {
            int tapCount = 0;

            @Override
            public void onClick(View v) {
                tapCount++;

                // 5 taps unlock admin
                if (tapCount == 5) {
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

        String url = "http://coms-3090-056.class.las.iastate.edu:8080/loginEmail/"
                + encodedEmail + "/" + encodedPassword + "/";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String fetchedUsername = response.getString("username");
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        prefs.edit().putString("username", fetchedUsername).apply();
                        prefs.edit().putString("password", password).apply();
                        prefs.edit().putString("email", email).apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("username", fetchedUsername);
                        intent.putExtra("password", password);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(),
                                "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(),
                        "Login failed: " + error.toString(), Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}