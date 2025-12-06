package com.example.cymarket.Reporting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cymarket.R;

import java.util.List;

/**
 * Adapter for displaying a list of {@link Report} objects in a RecyclerView.
 * <p>
 * Handles binding each report's title, description, and date to the corresponding views.
 * </p>
 *
 * @author Tyler Mestery
 */
public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<Report> reports;

    /**
     * Constructs a ReportsAdapter with the given list of reports.
     *
     * @param reports the list of reports to display
     */
    public ReportsAdapter(List<Report> reports) {
        this.reports = reports;
    }


    /**
     * Inflates the layout for a single report item and returns a ViewHolder.
     *
     * @param parent the parent ViewGroup
     * @param viewType the type of the view
     * @return a new ReportViewHolder instance
     */
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

    /**
     * Binds a report to the given ViewHolder, setting the title, description, and date.
     *
     * @param holder the ReportViewHolder to bind data to
     * @param position the position of the report in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);

        // Display report ID as title
        holder.title.setText("Report ID: " + report.getId());

        // Display the report content
        holder.description.setText(report.getReport());

        // Display reporting user's username or email
        if (report.getUser() != null) {
            holder.date.setText("By: " + report.getUser().getUsername());
        } else {
            holder.date.setText("By: Unknown");
        }
    }

    /**
     * Returns the total number of reports in the adapter.
     *
     * @return the number of reports
     */
    @Override
    public int getItemCount() {
        return reports.size();
    }

    /**
     * ViewHolder for a single report item.
     * <p>
     * Holds references to the title, description, and date TextViews.
     * </p>
     */
    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;

        /**
         * Constructs a ReportViewHolder and initializes its views.
         *
         * @param itemView the item view for the report
         */
        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.report_title);
            description = itemView.findViewById(R.id.report_desc);
            date = itemView.findViewById(R.id.report_date);
        }
    }
}