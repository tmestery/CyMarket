package com.example.cymarket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final List<User> users;
    private final OnUserClickListener listener;

    public FriendsAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.imageViewFriend);
            username = itemView.findViewById(R.id.textViewUsername);
        }
    }
}