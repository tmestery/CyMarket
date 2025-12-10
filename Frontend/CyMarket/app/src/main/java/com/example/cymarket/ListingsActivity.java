package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cymarket.Messages.MessagesActivity;

/**
 * @author - Alexander LeFeber
 */
public class ListingsActivity extends AppCompatActivity {

    private TextView messageText;    // establish messagetext variable

    private Button backButton;      // establish backbutton variable

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);   // listings activity xml

        // links to xml
        messageText = findViewById(R.id.listings_msg_txt);
        backButton = findViewById(R.id.listings_back_btn);

        // establish linstener for back button functionality
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingsActivity.this, MessagesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
