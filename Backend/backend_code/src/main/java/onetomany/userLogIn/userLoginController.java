package onetomany.userLogIn;

import java.util.ArrayList;
import java.util.List;


import onetomany.Reports.Reports;
import onetomany.Reports.ReportsRepository;
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



/**
 *
 * @author Daniel Pinilla
 *
 */

@RestController
public class userLoginController {

    @Autowired
    userLoginRepository userLoginRepository;

    @Autowired
    UserRepository userRepository;


    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/usersLogin/")
    List<userLogin> getAllUsers(){

        return userLoginRepository.findAll();
    }
    @GetMapping(path = "/login/e/{email}/{password}/")
    userLogin getUserByEmail(@PathVariable String email, @PathVariable String password){
        userLogin temp= userLoginRepository.findByEmail(email);
        if (temp.getPassword().equals(password))
            return temp;
        return null;
    }

    @GetMapping(path = "/login/u/{username}/{password}/")
    userLogin getUserByUsername(@PathVariable String username, @PathVariable String password){
        userLogin temp= userLoginRepository.findByUserName(username);
        if (temp.getPassword().equals(password))
            return temp;
        return null;
    }



    @GetMapping(path = "/usersLogin/{emailId}/{password}/")
    userLogin getUserById( @PathVariable String emailId, @PathVariable String password){

        userLogin temp= userLoginRepository.findByEmail(emailId);
        if(temp == null || !temp.getPassword().equals(password))
            return null;
        return temp;
    }


    @PostMapping(path = "/usersLogin/{type}")
    String createUser(@PathVariable char type,@RequestBody userLogin user){
        if (user == null)
            return failure;
        userLoginRepository.save(user);
        return success;
    }

    @DeleteMapping(path = "/usersLogin/{id}")
    String deleteLoginUser( @PathVariable String password, @PathVariable long id){

        userLoginRepository.deleteById(id);
        return success;

    }








}
