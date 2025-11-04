// Reports.java
package com.example.cymarket;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Reports {
    @SerializedName("reportList")
    private List<Report> reportList;

    public Reports() {}

    public Reports(List<Report> reportList) {
        this.reportList = reportList;
    }

    public List<Report> getReportList() {
        return reportList;
    }

    public void setReportList(List<Report> reportList) {
        this.reportList = reportList;
    }

    @Override
    public String toString() {
        return "Reports{" +
                "reportList=" + reportList +
                '}';
    }
}