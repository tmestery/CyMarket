package com.example.cymarket.Messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cymarket.R;

import java.util.List;

/**
 * Adapter for displaying chat messages in a RecyclerView.
 * <p>
 * Aligns messages differently depending on whether they are sent by the current user
 * or received from others.
 * </p>
 *
 * @author Tyler Mestery
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<Message> messages;
    private final String username; // current user

    /**
     * Constructor for ChatAdapter.
     *
     * @param messages List of messages to display
     * @param username Current user's username for differentiating sent/received messages
     */
    public ChatAdapter(List<Message> messages, String username) {
        this.messages = messages;
        this.username = username;
    }

    /**
     * Returns a view type for the message based on whether it was sent by the current user.
     *
     * @param position Position of the message in the list
     * @return 1 if sent by the current user, 2 otherwise
     */
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSender().equals(username) ? 1 : 2;
    }

    /**
     * Inflates the message layout for the RecyclerView item.
     *
     * @param parent   The parent ViewGroup
     * @param viewType The view type determined by getItemViewType
     * @return MessageViewHolder instance
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    /**
     * Binds the message content and sets background depending on sender.
     *
     * @param holder   The ViewHolder for the message item
     * @param position Position of the message in the list
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message m = messages.get(position);
        holder.messageText.setText(m.getContent());

        holder.messageText.setBackgroundResource(
                m.getSender().equals(username) ? R.drawable.bg_message_sent : R.drawable.bg_message_recieved
        );
    }

    /**
     * Returns the total number of messages.
     *
     * @return size of messages list
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        /**
         * Constructor for MessageViewHolder.
         *
         * @param itemView The message item view
         */
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessage);
        }
    }
}