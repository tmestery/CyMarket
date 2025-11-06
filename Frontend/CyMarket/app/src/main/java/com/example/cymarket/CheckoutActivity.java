package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private Button backButton;
    private EditText nameField;
    private EditText numberField;
    private EditText expiryField;
    private EditText cvcField;
    private EditText addressField;
    private EditText zipField;
    private Button confirmButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // link to xml
        backButton = findViewById(R.id.checkout_back_btn);
        EditText nameField = findViewById(R.id.card_name);
        EditText numberField = findViewById(R.id.card_number);
        EditText expiryField = findViewById(R.id.card_expiry);
        EditText cvcField = findViewById(R.id.card_cvc);
        EditText addressField = findViewById(R.id.card_address);
        EditText zipField = findViewById(R.id.card_zip);
        Button confirmButton = findViewById(R.id.checkout_confirm_btn);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckoutActivity.this, BuyActivity.class);
                startActivity(intent);
            }
        });
    }
}
