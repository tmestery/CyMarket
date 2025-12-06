package com.example.cymarket.Messages;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cymarket.R;
import com.example.cymarket.ReportUserActivity;

import java.util.List;

/**
 * RecyclerView Adapter for displaying chat messages.
 * <p>
 * Messages sent by the current user are aligned to the right,
 * while messages from others are aligned to the left.
 * Users can long-press any message to report the sender.
 *
 *  @author Tyler Mestery
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messages;
    private final String currentUser;

    /**
     * Constructs a new MessageAdapter.
     *
     * @param messages    List of Message objects to display.
     * @param currentUser Username of the current user, used to determine alignment.
     */
    public MessageAdapter(List<Message> messages, String currentUser) {
        this.messages = messages;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        Log.d("ADAPTER_TEST", "Binding: " + message.getContent());

        holder.messageText.setText(message.getContent());

        // Use LinearLayout.LayoutParams since root is LinearLayout
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();

        // Gravity for left/right alignment
        if (message.isSentByMe(currentUser)) {
            params.gravity = Gravity.END;
            holder.messageText.setBackgroundResource(R.drawable.bg_message_sent);
            holder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            params.gravity = Gravity.START;
            holder.messageText.setBackgroundResource(R.drawable.bg_message_recieved);
            holder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }

        holder.messageText.setLayoutParams(params);

        // Long press to report a user
        holder.itemView.setOnLongClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ReportUserActivity.class);
            intent.putExtra("reportedUser", message.getSender());
            holder.itemView.getContext().startActivity(intent);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Adds a new message to the adapter and notifies RecyclerView.
     *
     * @param message Message to add.
     */
    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /**
     * ViewHolder for chat message items.
     */
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        /**
         * Constructs a new MessageViewHolder.
         *
         * @param itemView The view representing a message item.
         */
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessage);
        }
    }
}