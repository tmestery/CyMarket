package com.example.cymarket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ME = 1;
    private static final int TYPE_THEM = 2;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isMine() ? TYPE_ME : TYPE_THEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ME) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me, parent, false);
            return new MeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_them, parent, false);
            return new ThemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = messages.get(position);
        if (holder instanceof MeViewHolder) {
            ((MeViewHolder) holder).message.setText(m.getText());
        } else {
            ((ThemViewHolder) holder).message.setText(m.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MeViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        MeViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message_me);
        }
    }

    static class ThemViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ThemViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message_them);
        }
    }
}