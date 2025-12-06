package com.example.cymarket.Messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.cymarket.R;
import com.example.cymarket.LoginSignup.User;

import java.util.List;

/**
 * RecyclerView adapter used to display a list of users that can be added
 * to a messaging group.
 * <p>
 * Each item shows the user's profile image and username and allows selection
 * via a click listener.
 *
 * @author Tyler Mestery
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    /**
     * Listener interface for handling user click events.
     */
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final List<User> users;
    private final OnUserClickListener listener;

    /**
     * Constructs a new FriendsAdapter.
     *
     * @param users    list of users to display
     * @param listener callback invoked when a user is clicked
     */
    public FriendsAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    /**
     * Inflates the RecyclerView item layout and creates a ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new {@link FriendViewHolder}
     */
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    /**
     * Binds user data to the ViewHolder at the specified position.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User user = users.get(position);
        holder.username.setText(user.getUsername());

        Glide.with(holder.itemView.getContext())
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.pfp)
                .into(holder.profileImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    /**
     * Returns the total number of users in the adapter.
     *
     * @return number of items
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class that holds references to the views
     * used to display a user's information.
     */
    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username;

        /**
         * Constructs a new FriendViewHolder.
         *
         * @param itemView the root view for the list item
         */
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.imageViewFriend);
            username = itemView.findViewById(R.id.textViewUsername);
        }
    }
}