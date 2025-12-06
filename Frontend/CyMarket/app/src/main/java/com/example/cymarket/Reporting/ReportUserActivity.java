package com.example.cymarket.Reporting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.example.cymarket.BuyActivity;
import com.example.cymarket.Messages.FriendsActivity;
import com.example.cymarket.R;
import com.example.cymarket.SellActivity;
import com.example.cymarket.Services.VolleySingleton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        String reportedUser = getIntent().getStringExtra("reportedUser");
        String currentUser = getIntent().getStringExtra("currentUser");

        fileReport.setOnClickListener(v ->
                sendReportToBackend(currentUser, reportedUser)
        );

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
    private void sendReportToBackend(String reporter, String reportedUser) {
        String text = reportText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Enter report text", Toast.LENGTH_SHORT).show();
            return;
        }

        String url =
                "http://coms-3090-056.class.las.iastate.edu:8080/usersLogin/newReport/"
                        + reporter + "/" + reportedUser;

        com.android.volley.toolbox.StringRequest request =
                new com.android.volley.toolbox.StringRequest(
                        Request.Method.POST,
                        url,
                        response -> {
                            Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show();
                            finish();
                        },
                        error -> Toast.makeText(
                                this,
                                "Failed: " +
                                        (error.networkResponse != null
                                                ? error.networkResponse.statusCode
                                                : "Network error"),
                                Toast.LENGTH_SHORT
                        ).show()
                ) {
                    @Override
                    public byte[] getBody() {
                        return text.getBytes(); // ✅ payload is raw string
                    }

                    @Override
                    public String getBodyContentType() {
                        return "text/plain; charset=utf-8";
                    }
                };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}