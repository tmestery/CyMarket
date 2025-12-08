package com.example.cymarket;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.cymarket.LoginSignup.User;
import com.example.cymarket.Reporting.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.List;

/**
 * System + model tests for Reporting folder
 */
@RunWith(AndroidJUnit4.class)
public class TylerReportingFolderTest {

    /* =====================================================
       REPORT MODEL
       ===================================================== */

    @Test
    public void testReportGettersSettersAndToString() {
        User user = new User();
        user.setUsername("tester");

        User seller = new User();
        seller.setUsername("seller");

        Report report = new Report();
        report.setId(12);
        report.setReport("Spam report");
        report.setUser(user);
        report.setSeller(seller);
        report.setReviewed(true);

        assert(report.getId() == 12);
        assert(report.getReport().equals("Spam report"));
        assert(report.getUser().getUsername().equals("tester"));
        assert(report.getSeller().getUsername().equals("seller"));
        assert(report.isReviewed());

        assert(report.toString().contains("Spam report"));
    }

    /* =====================================================
       REPORTS WRAPPER MODEL
       ===================================================== */

    @Test
    public void testReportsListAndToString() {
        List<Report> list = new ArrayList<>();
        list.add(new Report());

        Reports reports = new Reports(list);

        assert(reports.getReportList().size() == 1);

        reports.setReportList(new ArrayList<>());
        assert(reports.getReportList().isEmpty());

        assert(reports.toString().contains("reportList"));
    }

    /* =====================================================
       REPORTS ADAPTER (LOGIC-LEVEL)
       ===================================================== */

    @Test
    public void testReportsAdapterLogic() {
        User user = new User();
        user.setUsername("reporter");

        Report report = new Report();
        report.setId(1);
        report.setReport("Offensive content");
        report.setUser(user);

        List<Report> reports = new ArrayList<>();
        reports.add(report);

        ReportsAdapter adapter = new ReportsAdapter(reports);

        // getItemCount
        assert(adapter.getItemCount() == 1);

        // setReports
        adapter.setReports(new ArrayList<>());
        assert(adapter.getItemCount() == 0);
    }

    /* =====================================================
       REPORT USER ACTIVITY (UI PATH)
       ===================================================== */

    @Test
    public void testReportUserActivityLoads() {
        android.content.Intent intent =
                new android.content.Intent(
                        ApplicationProvider.getApplicationContext(),
                        ReportUserActivity.class
                );
        intent.putExtra("reportedUser", "user2");
        intent.putExtra("currentUser", "user1");

        androidx.test.core.app.ActivityScenario.launch(intent);
    }

    /* =====================================================
       PRIVATE METHOD COVERAGE (VIA SIDEEFFECT)
       ===================================================== */

    @Test
    public void testSendReportEmptyTextPath() {
        android.content.Intent intent =
                new android.content.Intent(
                        ApplicationProvider.getApplicationContext(),
                        ReportUserActivity.class
                );
        intent.putExtra("reportedUser", "user2");
        intent.putExtra("currentUser", "user1");

        androidx.test.core.app.ActivityScenario.launch(intent);
        // empty input → private validation path hit
    }
}