package com.example.cymarket.Reporting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.cymarket.BuyActivity;
import com.example.cymarket.Messages.FriendsActivity;
import com.example.cymarket.R;
import com.example.cymarket.SellActivity;
import com.example.cymarket.Services.VolleySingleton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity for reporting a user.
 * <p>
 * Allows the current user to submit a report against another user, sending the report to the backend.
 * </p>
 *
 * @author Tyler Mestery
 */
public class ReportUserActivity extends AppCompatActivity {

    private Button fileReport;
    private EditText reportText;

    /**
     * Initializes the activity, sets up views, handles report submission, and bottom navigation.
     *
     * @param savedInstanceState the saved state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_user);

        fileReport = findViewById(R.id.reportButton);
        reportText = findViewById(R.id.reportInput);

        // Get the reported user/group
        String reportedUser = getIntent().getStringExtra("reportedUser");

        fileReport.setOnClickListener(v -> sendReportToBackend(reportedUser));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_buy) {
                startActivity(new Intent(this, BuyActivity.class));
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(this, SellActivity.class));
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, FriendsActivity.class));
            }
            return true;
        });
    }

    /**
     * Sends the report data to the backend server via a POST request.
     *
     * @param reportedUser the username of the user being reported
     */
    private void sendReportToBackend(String reportedUser) {
        String text = reportText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Enter report text", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            JSONObject userObj = new JSONObject();
            userObj.put("id", userId);

            body.put("report", text);
            body.put("user", userObj);

            // Add reported user info
            body.put("reportedUser", reportedUser);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to build report", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://coms-3090-056.class.las.iastate.edu:8080/reports/";

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show();
                    finish(); // close activity
                },
                error -> Toast.makeText(this, "Failed: " + (error.getMessage() != null ? error.getMessage() : "Network error"), Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}