package onetomany.AdminActivityReport;

import onetomany.adminUser.adminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface adminActivityReportRepository  extends JpaRepository<adminActivityReport, Long> {


    adminActivityReport findById(int id);
    adminActivityReport findByReportID(int id);

}
