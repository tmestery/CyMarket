package com.example.cymarket.Reporting;

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

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("date")
    private String date;

    @SerializedName("username")
    private String username;

    /**
     * Constructs a Report object with the given properties.
     *
     * @param id the unique identifier of the report
     * @param title the title or summary of the report
     * @param description the detailed description of the report
     * @param date the date the report was submitted
     * @param username the username of the user who submitted the report
     */
    public Report(int id, String title, String description, String date, String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.username = username;
    }

    /**
     * Returns the ID of the report.
     *
     * @return the report ID
     */
    public int getId() { return id; }

    /**
     * Sets the ID of the report.
     *
     * @param id the report ID to set
     */
    public void setId(int id) { this.id = id; }

    /**
     * Returns the title of the report.
     *
     * @return the report title
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the report.
     *
     * @param title the report title to set
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Returns the description of the report.
     *
     * @return the report description
     */
    public String getDescription() { return description; }

    /**
     * Sets the description of the report.
     *
     * @param description the report description to set
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns the submission date of the report.
     *
     * @return the report date as a String
     */
    public String getDate() { return date; }

    /**
     * Sets the submission date of the report.
     *
     * @param date the report date to set
     */
    public void setDate(String date) { this.date = date; }

    /**
     * Returns the username of the user who submitted the report.
     *
     * @return the reporter's username
     */
    public String getUsername() { return username; }

    /**
     * Sets the username of the user who submitted the report.
     *
     * @param username the reporter's username to set
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Returns a string representation of the Report object.
     *
     * @return a string describing all report properties
     */
    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}