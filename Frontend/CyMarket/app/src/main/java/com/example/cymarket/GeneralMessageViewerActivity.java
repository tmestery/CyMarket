package com.example.cymarket;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GeneralMessageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_buy) {
                startActivity(new Intent(GeneralMessageViewerActivity.this, BuyActivity.class));
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(GeneralMessageViewerActivity.this, SellActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(GeneralMessageViewerActivity.this, MessagesActivity.class));
                return true;
            }
            return false;
        });
    }
}