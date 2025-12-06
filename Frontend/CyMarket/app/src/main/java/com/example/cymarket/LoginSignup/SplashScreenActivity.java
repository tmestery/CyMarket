package com.example.cymarket.LoginSignup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cymarket.R;

/**
 * Activity that displays the splash screen for a brief duration before
 * navigating to the LoginActivity.
 *
 * @author Tyler Mestery
 */
public class SplashScreenActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Sets the theme, inflates the layout, and starts a delayed transition
     * to the LoginActivity after 1 second.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains
     *                           the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Switch to your normal theme before inflating XML
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            finish();
        }, 1000);
    }
}