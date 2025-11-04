package com.example.cymarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportUserActivity extends AppCompatActivity {

    private Button fileReport;
    private EditText reportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_user);

        fileReport = findViewById(R.id.reportButton);
        reportText = findViewById(R.id.reportInput);

        fileReport.setOnClickListener(v -> sendReportToBackend());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_buy) {
                startActivity(new Intent(this, BuyActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(this, SellActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, FriendsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void sendReportToBackend() {
        String text = reportText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Enter report text", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject userObj = new JSONObject();
        JSONObject body = new JSONObject();

        try {
            userObj.put("id", userId);
            body.put("report", text);
            body.put("user", userObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://coms-3090-056.class.las.iastate.edu:8080/reports/";

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, FriendsActivity.class));
                    finish();
                },
                error -> Toast.makeText(this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}