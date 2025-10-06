package onetomany.Users;

import java.util.Date;
import java.util.List;


import org.springframework.http.MediaType;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;


import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import onetomany.Items.Item;
import onetomany.Items.ItemsRepository;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;

/**
 *
 * @author Vivek Bengre
 *
 */

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemsRepository itemRepository;
    @Autowired
    PasswordRecoveryService passwordRecoveryService;


    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    private String recoverySent = "{\"message\":\"Recovery code sent\"}";
    private String passwordReset = "{\"message\":\"Password updated\"}";

    @GetMapping(path = "/users")
    List<User> getAllUsersss(){
        return userRepository.findAll();
    }
    @GetMapping(path = "/users/{id}")
    User getAllUser(@PathVariable int id){


        return  userRepository.findById(id);
    }

    @GetMapping(path = "/users/u/{username}")
    User getUser (@PathVariable String username){
      return userRepository.findByUsername(username);
    }

    @GetMapping(path = "/users/joinDate/{email}/{password}")
    Date getUserJoinDate (@PathVariable String email, @PathVariable String password){
        User temp = userRepository.findByEmailId(email);
        if (temp == null) 
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        if (!temp.getUserPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
        return temp.getJoiningDate();
    }


   @GetMapping(path = "/loginEmail/{email}/{password}/")
   User getUserByEmail(@PathVariable String email, @PathVariable String password){
       User temp = userRepository.findByEmailId(email);
    if (temp == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    if (!temp.getUserPassword().equals(password)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
    }
    return temp;
   }
//    @GetMapping(path = "/us/{username}/{password}/")
//    User getUserByUsername( @PathVariable String username, @PathVariable String password){
//        User temp= userRepository.findByUsername(username);
//        if (temp.getUserPassword().equals(password))
//            return temp;
//        return null;
//    }


//    @GetMapping("/users/getReports/{id}/")
//    List<Reports> add(@PathVariable int id){
//        User tempUser= userRepository.findById(id);
//        if(tempUser == null)
//            return null;
//        return tempUser.getReports();
//    }


    
    @PostMapping(path = "/users")
    String createUser(@RequestBody User user){
        if (user == null)
            return failure;
        user.setJoiningDate(new Date());
        user.setLastLoggin();
        user.setIfActive(true);
        userRepository.save(user);

        return success;
    }

    @PostMapping(path = "/users/recovery-code")
    public ResponseEntity<String> requestPasswordRecovery(@RequestBody PasswordRecoveryRequest request) {
        if (request == null || !StringUtils.hasText(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        User user = userRepository.findByEmailId(request.getEmail());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        passwordRecoveryService.sendRecoveryEmail(user);
        return ResponseEntity.ok(recoverySent);
    }

    @PostMapping(path = "/users/recover-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        if (request == null || !StringUtils.hasText(request.getEmail())
                || !StringUtils.hasText(request.getRecoveryCode())
                || !StringUtils.hasText(request.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email, recovery code and new password are required");
        }

        User user = userRepository.findByEmailId(request.getEmail());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (!passwordRecoveryService.isRecoveryCodeValid(user, request.getRecoveryCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired recovery code");
        }

        user.setUserPassword(request.getNewPassword());
        passwordRecoveryService.clearRecoveryDetails(user);
        userRepository.save(user);

        return ResponseEntity.ok(passwordReset);
    }
    @PostMapping(path = "/users/addItem/{username}/{itemID}")
    String createUser( @PathVariable String username, @PathVariable int itemID){
        User tempUser = userRepository.findByUsername(username);
        Item tempItem = itemRepository.findById(itemID);
        if (tempUser == null || tempItem == null)
            return failure;

        System.out.println("User: " + tempUser.getUsername() + " Item: " + tempItem.getName());
        tempItem.addLikedByUser(tempUser);
        itemRepository.save(tempItem);
        tempUser.addItem(itemRepository.findById(itemID));
        userRepository.save(tempUser);
        itemRepository.save(tempItem);
        return success;

    }

 

    @PutMapping("/users/{id}/{password}/{newPassword}")
    User updateUser(@PathVariable int id, @PathVariable String password, @PathVariable String newPassword){
        User user = userRepository.findById(id);

        if(user == null || !user.getUserPassword().equals(password))
            return null;

        user.setUserPassword(newPassword);
        userRepository.save(user);
        return userRepository.findById(id);
    }





    @DeleteMapping(path = "/users/{id}")
    String deleteUser(@PathVariable int id){
        User temp= userRepository.findById(id);
      
       
        userRepository.delete(temp);

        return success;
    }
    @PostMapping("/users/{username}/profile-image")
    public String uploadProfileImage(@PathVariable String username, @RequestParam("image") MultipartFile imageFile) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }

        try {
            user.setProfileImage(imageFile.getBytes());
            userRepository.save(user);
            return "Profile image uploaded successfully";
        } catch (IOException e) {
            return "Failed to upload profile image";
        }
    }

 

    @GetMapping("/users/{username}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(user.getProfileImage());
    }

    @DeleteMapping("/users/{username}/profile-image")
    public String deleteProfileImage(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getProfileImage() == null) {
            return "User not found or profile image not set";
        }

        user.setProfileImage(null);
        userRepository.save(user);
        return "Profile image deleted successfully";
    }
    //test


}

