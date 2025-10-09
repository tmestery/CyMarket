package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MessagesActivity extends AppCompatActivity {

    private Button homeButton;  // define profile button variable
    private Button profileButton;  // define messages button variable
    private Button friendsButton; // find friends button
    private Button buyButton;
    private Button sellButton;
    private Button myListingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Link all the buttons:
        homeButton = findViewById(R.id.msgs_home_page_btn);
        profileButton = findViewById(R.id.msgs_profile_btn);
        friendsButton = findViewById(R.id.friends_btn);

        // Click listener on home button pressed:
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Click listener on friends button pressed:
        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagesActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });

        // Click listener on profile button pressed:
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagesActivity.this, ProfilesActivity.class);
                startActivity(intent);
            }
        });
    }
}