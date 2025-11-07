package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
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
        EditText nameField = findViewById(R.id.card_name);
        EditText numberField = findViewById(R.id.card_number);
        EditText expiryField = findViewById(R.id.card_expiry);   // have to seperate into MM/YY
        EditText cvvField = findViewById(R.id.card_cvv);
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
            String cardNumber = numberField.getText().toString().trim();
            String expiry = expiryField.getText().toString().trim();      // MM/YY
            String cvv = cvvField.getText().toString().trim();
            String address = addressField.getText().toString().trim();
            String zip = zipField.getText().toString().trim();

            if (cardHolderName.isEmpty() || cardNumber.isEmpty() || expiry.isEmpty() ||
                    cvv.isEmpty() || address.isEmpty() || zip.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            } else if (cardNumber.length() != 12) {
                Toast.makeText(getApplicationContext(), "Card number must be exactly 12 digits", Toast.LENGTH_SHORT).show();
            } else if (cvv.length() != 3) {
                Toast.makeText(getApplicationContext(), "CVV must be exactly 3 digits", Toast.LENGTH_SHORT).show();
            } else {
                submitCheckoutRequest(cardHolderName, cardNumber, expiry, cvv, address, zip);
            }
        });

    }

    // request
    private void submitCheckoutRequest(String cardHolderName, String cardNumber, String expiry,
                                       String cvv, String address, String zip){

        // get user ID
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userID = prefs.getInt("userId", -1);
        if (userID == -1) {
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
        String expirationYear = expiryParts[1];

        // build JSON payload
        JSONObject checkoutData = new JSONObject();
        try {
            checkoutData.put("userID", userID);

            // Wrap the single item in array for correct compatibility with backend
            JSONArray itemsArray = new JSONArray();
            JSONObject itemObj = new JSONObject();
            itemObj.put("itemID", selectedItem.getID());
            itemObj.put("quantity", 1);
            itemsArray.put(itemObj);
            checkoutData.put("items", itemsArray);

            checkoutData.put("shippingAddress", address);
            checkoutData.put("shippingCity", "");
            checkoutData.put("shippingState", "");
            checkoutData.put("shippingZipCode", zip);
            checkoutData.put("shippingCountry", "");

            checkoutData.put("cardNumber", cardNumber);
            checkoutData.put("cardHolderName", cardHolderName);
            checkoutData.put("expirationMonth", expirationMonth);
            checkoutData.put("expirationYear", expirationYear);
            checkoutData.put("cvv", cvv);

            checkoutData.put("billingAddress", address);
            checkoutData.put("billingCity", "");
            checkoutData.put("billingState", "");
            checkoutData.put("billingZipCode", zip);
            checkoutData.put("billingCountry", "");

            checkoutData.put("notes", "");

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Error creating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Send POST request
        String url = "http://coms-309-056.class.las.iastate.edu:8080/api/checkout";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                checkoutData,
                response -> {
                    Toast.makeText(getApplicationContext(), "Order placed!", Toast.LENGTH_SHORT).show();
                    finish(); // or redirect to confirmation
                },
                error -> Toast.makeText(getApplicationContext(),
                        "Checkout failed: " + error.toString(), Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}