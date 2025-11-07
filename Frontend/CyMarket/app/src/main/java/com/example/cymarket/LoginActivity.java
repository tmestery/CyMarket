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
import org.json.JSONObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText; // define username edittext variable
    private EditText passwordEditText; // define password edittext variable
    private Button loginButton;  // define login button variable
    private TextView signupButton;  // define signup textview variable
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
        signupButton = findViewById(R.id.login_signup_txt);
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
                Intent intent2 = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent2);
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        secretAdmin.setOnClickListener(new View.OnClickListener() {
            int tapCount = 0;

            @Override
            public void onClick(View v) {
                tapCount++;

                // 10 taps unlock admin (kept your original behavior)
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

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String fetchedEmail = response.optString("email", email);
                        String type = response.optString("type", "U");

                        // save email and password first
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        prefs.edit().putString("email", fetchedEmail).apply();
                        prefs.edit().putString("password", password).apply();

                        // then fetch username from backend
                        fetchAndSaveUsername(fetchedEmail, type);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error logging in", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(),
                        "Invalid Email or Password", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void fetchAndSaveUsername(String email, String type) {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/userslogin/getUsername/" + encodedEmail;


        com.android.volley.toolbox.StringRequest usernameRequest = new com.android.volley.toolbox.StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    String username = response.trim(); // plain text, not JSON
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    prefs.edit().putString("username", username).apply();

                    boolean isAdmin = type.equalsIgnoreCase("A");
                    Intent intent = new Intent(
                            LoginActivity.this,
                            isAdmin ? AdminDashboardActivity.class : MainActivity.class
                    );
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Couldn't fetch username", Toast.LENGTH_SHORT).show();

                    boolean isAdmin = type.equalsIgnoreCase("A");
                    Intent intent = new Intent(
                            LoginActivity.this,
                            isAdmin ? AdminDashboardActivity.class : MainActivity.class
                    );
                    startActivity(intent);
                    finish();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(usernameRequest);
    }
}