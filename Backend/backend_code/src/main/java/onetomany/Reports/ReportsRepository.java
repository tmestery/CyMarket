package onetomany.Reports;

import onetomany.Users.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportsRepository extends JpaRepository<Reports, Long>{
    Reports findById(int id);
    Reports findById(long id);
    Reports findByReport(String report);

    List<Reports> findByUser2(User user2);

}
