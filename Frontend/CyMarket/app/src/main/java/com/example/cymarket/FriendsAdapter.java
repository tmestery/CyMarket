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

    private final List<User> users;

    public FriendsAdapter(List<User> users) {
        this.users = users;
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
                .placeholder(R.drawable.pfp) // your placeholder image
                .into(holder.profileImage);
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