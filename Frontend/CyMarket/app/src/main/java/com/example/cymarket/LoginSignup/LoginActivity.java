package com.example.cymarket.LoginSignup;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.cymarket.Admin.AdminDashboardActivity;
import com.example.cymarket.MainActivity;
import com.example.cymarket.R;
import com.example.cymarket.VolleySingleton;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * LoginActivity handles authentication for both users and administrators.
 * <p>
 * This activity allows users to:
 * <ul>
 *     <li>Log in using email and password</li>
 *     <li>Select a role (User or Admin)</li>
 *     <li>Navigate to signup or password recovery</li>
 * </ul>
 * Successful authentication routes the user to the appropriate dashboard.
 * Networking is handled using the Volley library.
 *
 * @author Tyler Mestery
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText; // define username edittext variable
    private EditText passwordEditText; // define password edittext variable
    private Button loginButton;  // define login button variable
    private TextView signupButton;  // define signup textview variable
    private TextView forgotPassword;

    /**
     * Initializes the login screen UI, role selection,
     * and click listeners for authentication and navigation.
     *
     * @param savedInstanceState previously saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set up user/admin selection here:
        RadioGroup roleGroup = findViewById(R.id.login_role_group);
        RadioButton userRoleBtn = findViewById(R.id.role_user);
        RadioButton adminRoleBtn = findViewById(R.id.role_admin);

        /* initialize UI elements */
        emailEditText = findViewById(R.id.login_email_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);
        signupButton = findViewById(R.id.login_signup_txt);
        forgotPassword = findViewById(R.id.forgot_password);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter both email and password", Toast.LENGTH_SHORT).show();
                } else {
                    String selectedRole = userRoleBtn.isChecked() ? "U" : "n";
                    checkUserCredentials(email, password, selectedRole);
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
    }

    /**
     * Sends login credentials to the backend API for authentication.
     * On success, user data is stored locally and username fetching
     * is initiated.
     *
     * @param email        user email
     * @param password     user password
     * @param selectedRole selected role identifier
     */
    private void checkUserCredentials(String email, String password, String selectedRole) {
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
                        String username = response.optString("userName", "");
                        String type = response.optString("type", "U");

                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putInt("userId", response.optInt("id", -1))
                                .putString("email", fetchedEmail)
                                .putString("password", password)
                                .putString("username", username)
                                .apply();
                        finish();

                        // then fetch username from backend
                        fetchAndSaveUsername(fetchedEmail, selectedRole);

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

    /**
     * Fetches the username associated with the given email
     * and routes the user to the appropriate activity.
     *
     * @param email        authenticated email
     * @param selectedRole selected role identifier
     */
    private void fetchAndSaveUsername(String email, String selectedRole) {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/userslogin/getUsername/" + encodedEmail;


        com.android.volley.toolbox.StringRequest usernameRequest = new com.android.volley.toolbox.StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    String username = response.trim(); // plain text, not JSON
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    prefs.edit().putString("username", username).apply();

                    boolean isAdmin = selectedRole.equalsIgnoreCase("n");
                    Intent intent = new Intent(
                            LoginActivity.this,
                            isAdmin ? AdminDashboardActivity.class : MainActivity.class
                    );
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Couldn't fetch username", Toast.LENGTH_SHORT).show();

                    boolean isAdmin = selectedRole.equalsIgnoreCase("n");
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