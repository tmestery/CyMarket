package com.example.cymarket;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

// this class will bind listing information to the views set in item_listing_card.xml
public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private List<Listing> listings;    // list of listings, from Listing.java
    private Context context;

    public ListingAdapter(Context context, List<Listing> listings) {
        this.context = context;
        this.listings = listings;
    }

    // References views inside each item card
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, price, quantity;
        private Button buyButton;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            price = itemView.findViewById(R.id.item_price);
            quantity = itemView.findViewById(R.id.item_quantity);
            buyButton = itemView.findViewById(R.id.listing_buy_btn);
        }
    }

    /**
     * returns a new viewholder
     * reads item_listing_card.xml, creates view, then returns to pass as viewholder
     */
    @NonNull
    @Override
    public ListingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing_card, parent, false);
        return new ViewHolder(view);
    }

    // Takes data from a listing and puts it into the views in the created viewholder
    @Override
    public void onBindViewHolder(@NonNull ListingAdapter.ViewHolder holder, int position) {
        Listing item = listings.get(position);
        holder.title.setText(item.title);
        holder.description.setText(item.description);
        holder.price.setText("$" + item.price);
        holder.quantity.setText("Quantity: " + item.quantity);

        holder.buyButton.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity <= 0) { // if no more left, send sold out
                Toast.makeText(context, "Item is sold out", Toast.LENGTH_SHORT).show();
                return;
            }

            int newQuantity = currentQuantity - 1;
            int itemId = item.getId(); // gather the id of the item

            JSONObject updateData = new JSONObject();
            try {
                updateData.put("quantity", newQuantity);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            String url = "http://coms-3090-056.class.las.iastate.edu:8080/items/" + itemId;

            JsonObjectRequest putRequest = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    updateData,
                    response -> {
                        item.setQuantity(newQuantity); // update local model
                        notifyItemChanged(holder.getAdapterPosition()); // refresh UI
                        Toast.makeText(context, "Item Bought!" + newQuantity, Toast.LENGTH_SHORT).show();
                    },
                    error -> Toast.makeText(context, "Purchase failed", Toast.LENGTH_SHORT).show()
            );

            VolleySingleton.getInstance(context).addToRequestQueue(putRequest);
        });
    }

    // returns num of items in list
    @Override
    public int getItemCount() {
        return listings.size();
    }
}
