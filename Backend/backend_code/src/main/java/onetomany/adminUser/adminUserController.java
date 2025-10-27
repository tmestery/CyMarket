package onetomany.adminUser;

import java.util.ArrayList;
import java.util.List;

import onetomany.AdminActivityReport.adminActivityReport;

import onetomany.Reports.Reports;
import onetomany.Reports.ReportsRepository;
import onetomany.Users.User;
import onetomany.Users.UserRepository;
import onetomany.adminUser.adminUser;
import onetomany.adminUser.adminUserRepository;

import onetomany.userLogIn.userLogin;
import onetomany.userLogIn.userLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



/**
 *
 * @author Daniel Pinilla
 *
 */

@RestController
public class adminUserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReportsRepository reportsRepository;

   
    @Autowired
    userLoginRepository userLoginRepository;

    @Autowired
    adminUserRepository adminUserRepository;




    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/adminUser/getAdminUsers/{email}/{password}/")
    List<adminUser> getAdminUser(@PathVariable String email, @PathVariable String password){
        adminUser temp= adminUserRepository.findByEmailId(email);
       if (temp == null)
           return null;
       if (!temp.getAdminPassword().equals(password))
           return null;
       return adminUserRepository.findAll();
    }




    @GetMapping(path = "/adminUser/{email}/{password}/")
    adminUser getAdminById( @PathVariable String email, @PathVariable String password){
        adminUser temp= adminUserRepository.findByEmailId(email);
        if(temp== null || !temp.getEmailId().equals(email) || !temp.getAdminPassword().equals(password))
             return null;
        return temp;
    }

    @GetMapping(path = "/adminUser/getAdmin/{email}/{password}/")
    List<User> getUsers(@PathVariable String email, @PathVariable String password){
        adminUser temp= adminUserRepository.findByEmailId(email);
        if (temp == null)
            return null;
        if (!temp.getAdminPassword().equals(password))
            return null;
        return userRepository.findAll();
    }


    @GetMapping("/adminUser/getUser/{id}/{email}/{password}/")
    User getUserbyId(@PathVariable String email, @PathVariable String password, @PathVariable int id){
        adminUser temp= adminUserRepository.findByEmailId(email);
        if (temp == null)
            return null;
        if (!temp.getAdminPassword().equals(password))
            return null;
        return userRepository.findById(id);
    }
    @GetMapping("/adminUser/getReports/{email}/{password}/")
    List<Reports> getAllReports(@PathVariable String email, @PathVariable String password){
        adminUser temp= adminUserRepository.findByEmailId(email);
        if (temp == null)
            return null;
        if (!temp.getAdminPassword().equals(password))
            return null;
        return reportsRepository.findAll();
    }

    @GetMapping("/adminUser/getReports/{id}/{email}/{password}/")
    Reports getAllReports(@PathVariable String email, @PathVariable String password, @PathVariable int id){
        adminUser temp= adminUserRepository.findByEmailId(email);
        if (temp == null)
            return null;
        if (!temp.getAdminPassword().equals(password))
            return null;
        return reportsRepository.findById(id);
    }

    @PostMapping(path = "/adminUser/{adminPassword}/")
    String createUser(@RequestBody adminUser user){
        if (user == null)
            return failure;

        adminUserRepository.save(user);
        adminUser temptest= adminUserRepository.findByEmailId(user.getEmailId());
        userLogin temp= new userLogin(user.getUsername(),user.getEmailId(),'A', temptest.getAdminPassword());
        temptest.setUserLogin(temp);
        temp.setAdminUser(temptest);
        userLoginRepository.save(temp);
        adminUserRepository.save(user);
        return success;
    }

    @PostMapping(path = "/adminUser/309")
    String createNewAdmin(@RequestBody adminUser user){
        if (user == null)
            return failure;
        adminUserRepository.save(user);
        adminUser temptest= adminUserRepository.findByEmailId(user.getEmailId());
        userLogin temp= new userLogin(user.getUsername(),user.getEmailId(),'A', temptest.getAdminPassword());
        temptest.setUserLogin(temp);
        temp.setAdminUser(temptest);
        userLoginRepository.save(temp);
        adminUserRepository.save(user);
        return success;
    }

    @PostMapping(path = "/adminUser/addActivityReport/{email}/{password}")
    String createActicityReport(@PathVariable String email, @PathVariable String password){
        adminUser temp = adminUserRepository.findByEmailId(email);
        adminUser temp2= adminUserRepository.findById(1);
       return success;

    }
}
