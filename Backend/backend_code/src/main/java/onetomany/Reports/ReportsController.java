package onetomany.Reports;
import java.util.List;


import onetomany.Users.User;
import onetomany.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class ReportsController {
    @Autowired
    ReportsRepository reportsRepository;

    @Autowired
    UserRepository userRepository;
    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

//    @GetMapping(path = "/reports")
//    List<Reports> getAllReports(){
//        return reportsRepository.findAll();
//    }
    @GetMapping(path = "/reports/{id}")
    Reports getReportsById(@PathVariable int id){
        return reportsRepository.findById(id);
    }

    @PostMapping(path = "/reports/")
    String createReport(@RequestBody Reports Report){
        if (Report == null)
            return failure;
        reportsRepository.save(Report);
        return success;
    }

    @PutMapping(path = "/reports/{id}")
    Reports updateReport(@PathVariable int id, @RequestBody Reports request){
        Reports report = reportsRepository.findById(id);
        if(request == null)
            return null;
        report.setReport(request.getReport());
        reportsRepository.save(report);
        return reportsRepository.findById(id);
    }

    @DeleteMapping(path = "/reports/{id}")
    String deleteReport(@PathVariable long id){
        User temp= reportsRepository.findById(id).getUser();
        temp.removeReport(reportsRepository.findById(id));
        userRepository.save(temp);
        reportsRepository.deleteById(id);

        return success;
    }


}
