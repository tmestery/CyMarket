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
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO - Let users actually buy items
 * TODO - Clicking on an item takes you to a screen dedicated to that item?
 * TODO - User cannot buy their own items
 * TODO - Show images once sell screen has image support
 */
public class BuyActivity extends AppCompatActivity {

    // Components for the recyclerview item display
    private RecyclerView recyclerView;
    private ListingAdapter adapter;
    private List<Listing> listingList = new ArrayList<>();

    private Button backButton;       // define backbutton variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        // link to xml
        recyclerView = findViewById(R.id.buy_listings_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListingAdapter(listingList);
        recyclerView.setAdapter(adapter);
        backButton = findViewById(R.id.buy_back_btn);

        // establish listener for back button functionality
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyActivity.this, MessagesActivity.class);
                startActivity(intent);
            }
        });

        // the GET request to return all the listings
        fetchListings();
    }

    /**
     * Fetches listings posted by Sell using GET
     * No user filtering because it is public marketplace view
     */
    private void fetchListings() {
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/items"; // retrieve all items from backend

        // volley get request, retrieves JSON array of listings
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    listingList.clear(); // clear old listing before refresh

                    // go through each listing object
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            // get fields expected from sell screen
                            String title = obj.optString("name", "Untitled");
                            String description = obj.optString("description", "No description");
                            double price = obj.optDouble("price", 0.0);
                            int quantity = 1; // TODO - backend does not return quantity, needs implementation

                            // add to local list
                            listingList.add(new Listing(title, description, price, quantity));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // adapter gets notified that data has changed, so that UI will refresh
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Toast.makeText(this, "Failed to load listings", Toast.LENGTH_SHORT).show();
                }
        );

        // volley request
        Volley.newRequestQueue(this).add(request);
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
