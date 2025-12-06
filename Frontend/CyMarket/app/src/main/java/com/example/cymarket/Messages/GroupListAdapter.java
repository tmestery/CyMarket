package com.example.cymarket.Messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * RecyclerView adapter responsible for displaying a list of
 * chat groups that a user belongs to.
 * <p>
 * Each list item represents one {@link Group} and triggers
 * a callback when selected.
 *
 * @author Tyler Mestery
 */
public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private ArrayList<Group> groups;
    private GroupClickListener listener;

    /**
     * Callback interface for handling group click events.
     */
    public interface GroupClickListener {
        /**
         * Called when a group is selected.
         *
         * @param group the selected group
         */
        void onGroupClick(Group group);
    }

    /**
     * Constructs a new {@code GroupListAdapter}.
     *
     * @param groups   list of groups to display
     * @param listener listener invoked when a group is clicked
     */
    public GroupListAdapter(ArrayList<Group> groups, GroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    /**
     * Inflates the view used for an individual group item.
     *
     * @param parent   parent ViewGroup
     * @param viewType view type
     * @return newly created {@link ViewHolder}
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds group data to a RecyclerView cell.
     *
     * @param holder   view holder
     * @param position position in data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.text.setText(group.getName());
        holder.itemView.setOnClickListener(v -> listener.onGroupClick(group));
    }

    /**
     * Returns the total group count.
     *
     * @return number of groups
     */
    @Override
    public int getItemCount() {
        return groups.size();
    }

    /**
     * ViewHolder used to represent a single group item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        /**
         * Constructs a new {@code ViewHolder}.
         *
         * @param itemView root view for the item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(android.R.id.text1);
        }
    }
}