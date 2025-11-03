package onetomany.userLogIn;

import java.util.ArrayList;
import java.util.List;


import onetomany.Reports.Reports;
import onetomany.Reports.ReportsRepository;
import onetomany.Sellers.Seller;
import onetomany.Sellers.SellerRepository;
import onetomany.Users.User;
import onetomany.Users.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;



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
    @Autowired
    SellerRepository sellerRepository;


    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/usersLogin/")
    List<userLogin> getAllUsers(){

        return userLoginRepository.findAll();
    }
    @GetMapping(path = "/login/e/{email}/{password}")
    userLogin getUserByEmail(@PathVariable String email, @PathVariable String password){
        userLogin temp= userLoginRepository.findByEmail(email);
        if (temp.getPassword().equals(password))
            return temp;
        return null;
    }

    @GetMapping(path = "/userslogin/{username}/{password}")
    userLogin getLoginByUsername(@PathVariable String username, @PathVariable String password){
        userLogin temp= userLoginRepository.findByUserName(username);
        if (temp == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!temp.getPassword().equals(password)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
    return temp;
    }
    @GetMapping(path = "/userslogin/getUser/{username}/{password}")
    User getUserandLoginByUsername(@PathVariable String username, @PathVariable String password){
        userLogin temp= userLoginRepository.findByUserName(username);
        if (temp == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!temp.getPassword().equals(password)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
        return temp.getUser();
    }
     @GetMapping(path = "/userslogin/getSeller/{username}/{password}")
    Seller getSellerUserLogin(@PathVariable String username, @PathVariable String password){
        userLogin temp= userLoginRepository.findByUserName(username);
        if (temp == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!temp.getPassword().equals(password)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
        return temp.getSeller();
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

        user.setType(type);
        userLoginRepository.save(user);
        userLogin temp= userLoginRepository.findByUserName(user.getUserName());
        if (temp == null)
            return failure;

        User newUser = new User(temp);
        userRepository.save(newUser);
        temp.setUser(userRepository.findByEmailId(temp.email));
        userLoginRepository.save(temp);

        Seller newSeller = new Seller(userLoginRepository.findByEmail(temp.email));
        sellerRepository.save(newSeller);
        temp = userLoginRepository.findByEmail(temp.email);
        temp.setSeller(sellerRepository.findByUsername(newSeller.getUsername()));
        userLoginRepository.save(temp);
        return success;
    }

    

    @DeleteMapping(path = "/usersLogin/{id}")
    String deleteLoginUser( @PathVariable String password, @PathVariable long id){

        userLoginRepository.deleteById(id);
        return success;

    }








}
