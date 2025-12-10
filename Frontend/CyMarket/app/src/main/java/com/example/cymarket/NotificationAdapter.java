package com.example.cymarket;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

// bind notifications to the cards in notifications xml
/**
 * @author - Alexander LeFeber
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<Notification> notifications;


    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Notification n = notifications.get(position);
        holder.message.setText(n.message);
        holder.timestamp.setText(n.createdAt);

        // x to delete
        holder.deleteButton.setOnClickListener( v -> {
            notifications.remove(position);
            notifyItemRemoved(position);

            // send backend request to delete if functional
            String url = "http://coms-3090-056.class.las.iastate.edu:8080/notifications/" + n.id;
            StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, url,
                    response -> Toast.makeText(v.getContext(), "Deleted", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(v.getContext(), "Delete failed", Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(v.getContext()).add(deleteRequest);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, timestamp;
        Button deleteButton;
        public ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.notification_message);
            timestamp = itemView.findViewById(R.id.notification_timestamp);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}