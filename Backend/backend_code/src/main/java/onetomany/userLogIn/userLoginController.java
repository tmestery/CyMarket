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
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    ReportsRepository reportsRepository;


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

    @GetMapping(path = "/userslogin/getUserType/{email}")
    char getUserTypeByEmail(@PathVariable String email){
        userLogin temp= userLoginRepository.findByEmail(email);
        if (temp == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return temp.getType();
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

    @PostMapping(path = "/usersLogin/newReport/{username1}/{username2}")
    @Transactional
    public String createReport(@PathVariable String username1,
                           @PathVariable String username2,
                           @RequestBody String payload) {

    userLogin ul1 = userLoginRepository.findByUserName(username1);
    userLogin ul2 = userLoginRepository.findByUserName(username2);

    if (ul1 == null || ul2 == null)
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found");

    User user = ul1.getUser();
    Seller seller = ul2.getSeller();
    if (user == null || seller == null)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login not bound to User/Seller");

    
    Reports existing = reportsRepository.findByReport(payload);
    if (existing != null)
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Report already exists");

    Reports newReport = new Reports(user, seller, payload);

   
    user.addReport(newReport);
    seller.addReport(newReport);

  
    reportsRepository.save(newReport);

    return success;
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

    @DeleteMapping(path = "/userslogin/{id}")
@Transactional
public String deleteLogin(@PathVariable int id) {
    userLogin login = userLoginRepository.findById(id);
    if (login == null) 
        return failure;
    
    Seller s = login.getSeller();
    if (s != null) {
        
        login.setSeller(null);
        s.setUserLogin(null);

        sellerRepository.delete(s);   
    }

    User u = login.getUser();
    if (u != null) {
        login.setUser(null);
        u.setUserLogin(null);
        userRepository.delete(u);
    }

    userLoginRepository.delete(login);
    return success;
}



}
