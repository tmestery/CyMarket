package com.example.cymarket.Reporting;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a collection of {@link Report} objects.
 * <p>
 * This class is used to encapsulate a list of reports, typically returned
 * from an API or database query. It supports serialization with Gson.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     Reports reports = new Reports();
 *     List&lt;Report&gt; list = reports.getReportList();
 * </pre>
 *
 * @author Tyler
 */
public class Reports {
    @SerializedName("reportList")
    private List<Report> reportList;

    public Reports() {}

    /**
     * Constructs a Reports object with the given list of reports.
     *
     * @param reportList the list of {@link Report} objects to initialize with
     */
    public Reports(List<Report> reportList) {
        this.reportList = reportList;
    }

    /**
     * Returns the list of reports.
     *
     * @return a {@link List} of {@link Report} objects
     */
    public List<Report> getReportList() {
        return reportList;
    }

    /**
     * Sets the list of reports.
     *
     * @param reportList a {@link List} of {@link Report} objects to set
     */
    public void setReportList(List<Report> reportList) {
        this.reportList = reportList;
    }

    /**
     * Returns a string representation of the Reports object.
     *
     * @return a string describing the reports list
     */
    @Override
    public String toString() {
        return "Reports{" +
                "reportList=" + reportList +
                '}';
    }
}