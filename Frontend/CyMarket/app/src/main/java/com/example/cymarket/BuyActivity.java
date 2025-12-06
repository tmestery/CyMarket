package com.example.cymarket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cymarket.Services.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * ------------------------------------------------------------
 *  CyMarket Frontend - Class Documentation
 * ------------------------------------------------------------
 *
 *  Class: BuyActivity
 *  Author: Xander
 *  Project: CyMarket Android App
 *
 *  Description:
 *  Handles displaying marketplace listings, fetching items from
 *  the backend, clearing listings, and navigating back to main menu.
 *
 *  Key Responsibilities:
 *   - Fetches all listings from /items (GET)
 *   - Deletes listings using /items/{id} (DELETE)
 *   - Displays data using RecyclerView + ListingAdapter
 *   - Manages navigation within the app
 *
 *  Last Updated: 2025-11-21
 * ------------------------------------------------------------
 */
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
    private Button clearButton;      // define clear button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        // link to xml
        recyclerView = findViewById(R.id.buy_listings_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListingAdapter(this, listingList);
        recyclerView.setAdapter(adapter);
        backButton = findViewById(R.id.buy_back_btn);
        clearButton = findViewById(R.id.buy_clear_all_btn);

        // calls clearAllListings w/ button functionality
        clearButton.setOnClickListener(v -> clearAllListings());

        // establish listener for back button functionality
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyActivity.this, MainActivity.class);
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
                            int quantity = obj.optInt("quantity", 1);
                            // get id from backend
                            int id = obj.getInt("id");

                            // add to local list
                            listingList.add(new Listing(title, description, price, quantity, id));

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

    // clears all current listings in the
    private void clearAllListings() {
        String url = "http://coms-3090-056.class.las.iastate.edu:8080/items";

        JsonArrayRequest getRequest = new JsonArrayRequest( // retrieve all current listings
                Request.Method.GET,
                url,
                null,
                response -> {
                    // loop through to get ids
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject item = response.getJSONObject(i);
                            int itemId = item.getInt("id");

                            // delete url for item of "id"
                            String deleteUrl = url + "/" + itemId;

                            // delete request
                            JsonObjectRequest deleteRequest = new JsonObjectRequest(
                                    Request.Method.DELETE,
                                    deleteUrl,
                                    null,
                                    deleteResponse -> {
                                        // empty, could log each deletion
                                    },
                                    error -> {
                                        Toast.makeText(getApplicationContext(), "Delete failed for item " + itemId, Toast.LENGTH_SHORT).show();
                                    }
                            );

                            // delete request to queue
                            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(deleteRequest);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Toast.makeText(getApplicationContext(), "Clear request sent", Toast.LENGTH_SHORT).show();
                    fetchListings(); // refresh after deletion
                },
                error -> Toast.makeText(getApplicationContext(), "Failed to fetch items", Toast.LENGTH_SHORT).show()
        );

        // GET request for UI refresh
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(getRequest);
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
