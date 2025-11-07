package com.example.cymarket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messages;  // <-- use Message, not MessageModel

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
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
        holder.senderTextView.setText(message.getSender());
        holder.contentTextView.setText(message.getContent());
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
        TextView senderTextView;
        TextView contentTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }
    }
}


//package com.example.cymarket;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private final List<MessageModel> messages;
//    private static final int TYPE_ME = 0;
//    private static final int TYPE_THEM = 1;
//
//
//    public MessageAdapter(List<MessageModel> messages) {
//        this.messages = messages;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return messages.get(position).isSentByMe() ? TYPE_ME : TYPE_THEM;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == TYPE_ME) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_message_me, parent, false);
//            return new MeViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_message_them, parent, false);
//            return new ThemViewHolder(view);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        MessageModel message = messages.get(position);
//        if (holder instanceof MeViewHolder) {
//            ((MeViewHolder) holder).message.setText(message.getMessage());
//        } else {
//            ((ThemViewHolder) holder).message.setText(message.getMessage());
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return messages.size();
//    }
//
//    // ViewHolder for messages sent by me
//    static class MeViewHolder extends RecyclerView.ViewHolder {
//        TextView message;
//
//        public MeViewHolder(@NonNull View itemView) {
//            super(itemView);
//            message = itemView.findViewById(R.id.textViewMessage);
//        }
//    }
//
//    // ViewHolder for messages sent by others
//    static class ThemViewHolder extends RecyclerView.ViewHolder {
//        TextView message;
//
//        public ThemViewHolder(@NonNull View itemView) {
//            super(itemView);
//            message = itemView.findViewById(R.id.textViewMessage);
//        }
//    }
//}