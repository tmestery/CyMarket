package com.example.cymarket;

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

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messages;
    private final String currentUser;

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
        Log.d("ADAPTER_TEST", "Binding: " + messages.get(position).getContent());
        holder.messageText.setWidth(200);
        holder.messageText.setHeight(80);
        Message message = messages.get(position);

        // layout params for bubble alignment
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();

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
        holder.messageText.setText(message.getContent());

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

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessage);
        }
    }
}