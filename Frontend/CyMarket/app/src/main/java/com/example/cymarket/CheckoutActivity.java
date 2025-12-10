package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.cymarket.Services.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author - Alexander LeFeber
 */
public class CheckoutActivity extends AppCompatActivity {

    private Button backButton;
    private EditText nameField;
    private EditText numberField;
    private EditText expiryField;
    private EditText cvvField;
    private EditText addressField;
    private EditText zipField;
    private Button confirmButton;

    private Listing selectedItem;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // link to xml
        backButton = findViewById(R.id.checkout_back_btn);
        nameField = findViewById(R.id.card_name);
        numberField = findViewById(R.id.card_number);
        expiryField = findViewById(R.id.card_expiry);   // have to seperate into MM/YY
        cvvField = findViewById(R.id.card_cvv);
        addressField = findViewById(R.id.card_address);
        zipField = findViewById(R.id.card_zip);
        confirmButton = findViewById(R.id.checkout_confirm_btn);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckoutActivity.this, BuyActivity.class);
                startActivity(intent);
            }
        });

        // get selected item from buy button intent
            selectedItem = new Listing(
                getIntent().getStringExtra("title"),
                getIntent().getStringExtra("description"),
                getIntent().getDoubleExtra("price", 0.0),
                getIntent().getIntExtra("quantity", 1),
                getIntent().getIntExtra("id", -1)
        );

       // confirm button
        confirmButton.setOnClickListener(v -> {
            String cardHolderName = nameField.getText().toString().trim();
            String rawCardNumber = numberField.getText().toString().trim();
            String cardNumber = rawCardNumber.replaceAll("[^\\d]", ""); // removes spaces, dashes, etc.
            String expiry = expiryField.getText().toString().trim();      // MM/YY
            String cvv = cvvField.getText().toString().trim();
            String address = addressField.getText().toString().trim();
            String zip = zipField.getText().toString().trim();

            if (cardHolderName.isEmpty() || cardNumber.isEmpty() || expiry.isEmpty() ||
                    cvv.isEmpty() || address.isEmpty() || zip.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            } else if (cardNumber.length() < 13 || cardNumber.length() > 19) {
                Toast.makeText(getApplicationContext(), "Card number must be between 13 and 19 digits", Toast.LENGTH_SHORT).show();
            } else if (cvv.length() != 3) {
                Toast.makeText(getApplicationContext(), "CVV must be exactly 3 digits", Toast.LENGTH_SHORT).show();
            } else {
                submitCheckoutRequest(cardHolderName, cardNumber, expiry, cvv, address, zip);
            }
        });

    }

    // request
    private void submitCheckoutRequest(String cardHolderName, String cardNumber, String expiry,
                                       String cvv, String address, String zip) {

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // make sure expiry is written correctly
        String[] expiryParts = expiry.split("/");
        if (expiryParts.length != 2) {
            Toast.makeText(this, "Use MM/YY format for expiry", Toast.LENGTH_SHORT).show();
            return;
        }

        String expirationMonth = expiryParts[0];
        String expirationYear = "20" + expiryParts[1];

        // Build item list
        List<CheckoutItemRequest> items = new ArrayList<>();
        items.add(new CheckoutItemRequest(selectedItem.getID(), selectedItem.getQuantity()));

        // Build request object
        CheckoutRequest requestObj = new CheckoutRequest(
                userId,
                items,
                address, "Ames", "IA", zip, "USA",
                cardNumber, cardHolderName, expirationMonth, expirationYear, cvv,
                address, "Ames", "IA", zip, "USA",
                "N/A"
        );

        // Convert to JSON
        Gson gson = new Gson();
        String jsonString = gson.toJson(requestObj);

        JSONObject checkoutData;
        try {
            checkoutData = new JSONObject(jsonString);
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://coms-3090-056.class.las.iastate.edu:8080/api/checkout";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                checkoutData,
                response -> {
                    Toast.makeText(getApplicationContext(), "Order placed!", Toast.LENGTH_SHORT).show();
                    finish();

                    // Build notification JSON
                    String username = prefs.getString("username", "unknown");
                    String createdAt = java.time.LocalDateTime.now().toString();

                    JSONObject notifJson = new JSONObject();
                    try {
                        notifJson.put("type", "ITEM_SOLD");
                        notifJson.put("message", "Your checkout was successful for item '"
                                + selectedItem.getTitle() + "'.");
                        notifJson.put("relatedEntityId", selectedItem.getID());
                        notifJson.put("relatedEntityType", "Item");
                        notifJson.put("actionUrl", JSONObject.NULL);
                    } catch (Exception e) {
                        Log.e("CHECKOUT", "Error building notification JSON: " + e.getMessage());
                    }

                    String notifUrl = "http://coms-3090-056.class.las.iastate.edu:8080/notifications/test/" + username;

                    JsonObjectRequest notifRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            notifUrl,
                            notifJson,
                            notifResponse -> Log.d("CHECKOUT", "Checkout notification sent successfully"),
                            notifError -> Log.e("CHECKOUT", "Notification error: " + notifError.toString())
                    );

                    VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(notifRequest);
                },
                error -> {
                    String body = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        body = new String(error.networkResponse.data);
                        Log.e("CHECKOUT", "Error response: " + body);
                    }
                    Toast.makeText(getApplicationContext(),
                            "Checkout failed: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        );

            // finally add to queue
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);


    }
}
