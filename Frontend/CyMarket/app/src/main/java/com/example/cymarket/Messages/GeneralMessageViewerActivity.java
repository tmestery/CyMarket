package com.example.cymarket.Messages;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cymarket.BuyActivity;
import com.example.cymarket.R;
import com.example.cymarket.SellActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity responsible for displaying the general message viewer
 * and managing navigation between major sections of the app
 * using a bottom navigation bar.
 *
 * @author Tyler Mestery
 */
public class GeneralMessageViewerActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the layout and sets up the bottom navigation
     * to allow switching between Buy, Sell, and Chat sections.
     *
     * @param savedInstanceState contains the most recent saved state,
     *                           or {@code null} if none exists
     */
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