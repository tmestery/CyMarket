package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SellActivity extends AppCompatActivity {

    private TextView messageText;   // define messagetext variable

    private Button backButton;     // define backbutton variable

    private EditText itemNameEditText;    // define itemnameedittext variable

    private EditText priceEditText;     // define priceedittext variable
    private EditText descriptionEditText;    // define descriptionedittext variable
    private EditText quantityEditText;      // define quantityedittext variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);     // sell activity xml

        // link to xml
        messageText = findViewById(R.id.sell_msg_txt);
        backButton = findViewById(R.id.sell_back_btn);
        itemNameEditText = findViewById(R.id.sell_item_name_edt);
        priceEditText = findViewById(R.id.sell_price_edt);
        descriptionEditText = findViewById(R.id.sell_description_edt);
        quantityEditText = findViewById(R.id.sell_quantity_edt);

        // establish listener for back button functionality
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellActivity.this, MessagesActivity.class);
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

