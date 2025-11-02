package com.example.cymarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReportUserActivity extends AppCompatActivity {

    private Button fileReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_user);

        fileReport = findViewById(R.id.reportButton);

        fileReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportUserActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_buy) {
                startActivity(new Intent(ReportUserActivity.this, BuyActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(ReportUserActivity.this, SellActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(ReportUserActivity.this, FriendsActivity.class));
                return true;
            }
            return false;
        });
    }
}
