package com.example.cymarket;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

// this class will bind listing information to the views set in item_listing_card.xml
public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private List<Listing> listings;    // list of listings, from Listing.java

    public ListingAdapter(List<Listing> listings) {
        this.listings = listings;
    }

    // References views inside each item card
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, price, quantity;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            price = itemView.findViewById(R.id.item_price);
            quantity = itemView.findViewById(R.id.item_quantity);
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
    }

    // returns num of items in list
    @Override
    public int getItemCount() {
        return listings.size();
    }
}
