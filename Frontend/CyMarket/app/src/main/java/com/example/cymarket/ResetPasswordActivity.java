package com.example.cymarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cymarket.LoginSignup.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText codeInput, newPasswordInput;
    private Button confirmButton;
    private RequestQueue requestQueue;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        email = getIntent().getStringExtra("email");

        codeInput = findViewById(R.id.editTextCode);
        newPasswordInput = findViewById(R.id.editTextNewPassword);
        confirmButton = findViewById(R.id.reset_password);

        requestQueue = Volley.newRequestQueue(this);

        confirmButton.setOnClickListener(v -> sendResetPasswordRequest());
    }

    private void sendResetPasswordRequest() {
        String code = codeInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();

        if (code.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://coms-3090-056.class.las.iastate.edu:8080/users/recover-password"; // replace with actual endpoint

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("recoveryCode", code);
            jsonBody.put("newPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                    // Go back to login
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    String message = "Error resetting password";
                    if (error.networkResponse != null) {
                        message += ": " + new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(request);
    }
}