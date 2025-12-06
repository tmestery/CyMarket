package com.example.cymarket.Reporting;

import com.example.cymarket.LoginSignup.User;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a single report submitted by a user.
 * <p>
 * This class encapsulates all necessary information about a report,
 * including its ID, title, description, date, and the username of the reporter.
 * It is compatible with Gson for JSON serialization/deserialization.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     Report report = new Report(1, "Spam", "User is sending spam messages", "2025-12-06", "johnDoe");
 *     String title = report.getTitle();
 * </pre>
 *
 * @author Tyler Mestery
 */
public class Report {
    @SerializedName("id")
    private int id;

    @SerializedName("report")
    private String report;

    @SerializedName("user")
    private User user;

    @SerializedName("seller")
    private User seller;

    @SerializedName("reviewed")
    private boolean reviewed;

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReport() { return report; }
    public void setReport(String report) { this.report = report; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", report='" + report + '\'' +
                ", user=" + user +
                ", seller=" + seller +
                ", reviewed=" + reviewed +
                '}';
    }
}