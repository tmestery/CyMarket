package com.example.cymarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button resetPassword;
    private EditText emailInput;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        resetPassword = findViewById(R.id.reset_password);
        emailInput = findViewById(R.id.editTextTextEmailAddress);
        requestQueue = Volley.newRequestQueue(this);

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRecoveryEmail();
            }
        });
    }

    private void sendRecoveryEmail() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://coms-3090-056.class.las.iastate.edu:8080/users/recovery-code"; // Replace with actual endpoint

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Recovery email sent!", Toast.LENGTH_SHORT).show();

                    // go to recovery code
                    Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                },
                error -> Toast.makeText(this, "Error sending email: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        requestQueue.add(request);
    }
}