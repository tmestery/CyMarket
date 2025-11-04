package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private EditText firstNameText;   // define email edit
    private EditText lastNameText;   // define email edit
    private EditText emailEditText;   // define email edit
    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private EditText confirmEditText;   // define confirm edittext variable
    private TextView loginText;         // define login button variable
    private Button signupButton;        // define signup button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /* initialize UI elements */
        firstNameText = findViewById(R.id.signup_first_name_edt);  // link to first name edtext in the Signup activity XML
        lastNameText = findViewById(R.id.signup_last_name_edt);  // link to last name edtext in the Signup activity XML
        usernameEditText = findViewById(R.id.signup_username_edt);  // link to username edtext in the Signup activity XML
        passwordEditText = findViewById(R.id.signup_password_edt);  // link to password edtext in the Signup activity XML
        confirmEditText = findViewById(R.id.signup_confirm_edt);    // link to confirm edtext in the Signup activity XML
        loginText = findViewById(R.id.signup_login_text);  // link to login button in the Signup activity XML
        signupButton = findViewById(R.id.signup_signup_btn);  // link to signup button in the Signup activity XML
        emailEditText = findViewById(R.id.signup_email_edt); // link to email text in the Signup activity XML

        /* click listener on login button pressed */
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* grab strings from user inputs */
                String firstName = firstNameText.getText().toString();
                String lastName = lastNameText.getText().toString();
                String email = emailEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmEditText.getText().toString();

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(getApplicationContext(), "Passwords Don't Match", Toast.LENGTH_LONG).show();
                } else if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Fill Out All Fields", Toast.LENGTH_LONG).show();
                } else {
                    checkUserExists(username, new VolleyCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Signing Up", Toast.LENGTH_LONG).show();
                            signUpUser(firstName, lastName, email, username, password);
                        }

                        @Override
                        public void onFailure() {
                            // Email already exists OR error handled by toast in makeJsonObjReq
                        }
                    });
                }
            }
        });
    }

    // Interface to run the code after the request is finished
    public interface VolleyCallback {
        void onSuccess();

        void onFailure();
    }

    private void checkUserExists(String username, VolleyCallback callback) {
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/users/u/" + username;

        // Had to update to a string request, since currently the backend code sends NULL if user isn't found instead of a 404 error code...
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    // response might be "null" (literally the string "null")
                    if (response == null || response.equals("null") || response.isEmpty()) {
                        // user NOT found → safe to signup
                        callback.onSuccess();
                    } else {
                        // user exists
                        Toast.makeText(getApplicationContext(),
                                "Account Already Created Under This Username!",
                                Toast.LENGTH_SHORT).show();
                        callback.onFailure();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(),
                            "Failed to check username: " + error.toString(),
                            Toast.LENGTH_SHORT).show();
                    callback.onFailure();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }


    private void signUpUser(String firstName, String lastName, String email, String username, String password) {
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/usersLogin/U";  // U = normal user

        JSONObject json = new JSONObject();
        try {
            json.put("userName", username);
            json.put("email", email);
            json.put("password", password);
            // backend doesn't need first/last, it auto-creates User + Seller
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                response -> {
                    Toast.makeText(getApplicationContext(), "Account created! Login now.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                },
                error -> {
                    Toast.makeText(getApplicationContext(),
                            "Signup Failed: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
    }
}