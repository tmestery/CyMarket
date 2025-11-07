package com.example.cymarket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<Report> reports;

    public ReportsAdapter(List<Report> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    public void setReports(List<Report> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.title.setText(report.getTitle());
        holder.description.setText(report.getDescription());
        holder.date.setText(report.getDate());
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.report_title);
            description = itemView.findViewById(R.id.report_desc);
            date = itemView.findViewById(R.id.report_date);
        }
    }
}