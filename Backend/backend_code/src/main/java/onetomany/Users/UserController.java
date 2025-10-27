package onetomany.Users;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.springframework.http.MediaType;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;


import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import onetomany.Items.Item;
import onetomany.Items.ItemsRepository;
import onetomany.userLogIn.userLogin;

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
import onetomany.userLogIn.*;
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
    @Autowired
    private UserImageRepository userImageRepository;

    @Autowired
    private userLoginRepository userLoginRepository;


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
    @GetMapping(path = "/users/getName/{email}")
    String getName(@PathVariable String email){
        User temp =  userRepository.findByEmailId(email);
        if (temp == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return temp.getName();
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

        User temptest= userRepository.findUserByUsername(user.getUsername());
        userLogin temp= new userLogin(user.getUsername(),user.getEmailId(),'n',user.getUserPassword());
        temp.setUser(temptest);
        temptest.setUserLogin(temp);
        userLoginRepository.save(temp);
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

        if(temp == null)
            return failure;

        userRepository.delete(temp);


        return success;
    }
    @DeleteMapping(path = "/users/u/{username}")
    String deleteUserByEmail(@PathVariable String username){
        User temp= userRepository.findByUsername(username);
        if(temp == null)
            return failure;
         List<Item> items = new ArrayList<>(temp.getLikedItems());
        for (Item item : items) {
            item.removeLikedByUser(temp);
            itemRepository.save(item);
        }
       
        userRepository.delete(temp);

        return success;
    }
    @PostMapping("/users/{username}/profile-image")
    public String uploadProfileImage(@PathVariable String username, @RequestParam("image") MultipartFile imageFile) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }

        if (imageFile == null || imageFile.isEmpty()) {
            return "No image provided";
        }

        try {
            for (UserImage existing : new ArrayList<>(user.getImages())) {
                user.removeImage(existing);
            }
            UserImage image = new UserImage(imageFile.getBytes(), imageFile.getContentType());
            user.addImage(image);
            userRepository.save(user);
            return "Profile image uploaded successfully";
        } catch (IOException e) {
            return "Failed to upload profile image";
        }
    }



    @GetMapping("/users/{username}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getImages().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserImage image = user.getImages().get(0);
        MediaType mediaType = image.getContentType() != null
                ? MediaType.parseMediaType(image.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image.getData());
    }

    @DeleteMapping("/users/{username}/profile-image")
    public String deleteProfileImage(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getImages().isEmpty()) {
            return "User not found or profile image not set";
        }

        for (UserImage image : new ArrayList<>(user.getImages())) {
            user.removeImage(image);
        }
        userRepository.save(user);
        return "Profile image deleted successfully";
    }

    @PostMapping("/users/{username}/images")
    public ResponseEntity<String> uploadUserImages(@PathVariable String username,
                                                   @RequestParam("images") List<MultipartFile> imageFiles) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (imageFiles == null || imageFiles.isEmpty()) {
            return ResponseEntity.badRequest().body("No images provided");
        }

        try {
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile == null || imageFile.isEmpty()) {
                    continue;
                }

                UserImage image = new UserImage(imageFile.getBytes(), imageFile.getContentType());
                user.addImage(image);
            }

            userRepository.save(user);
            return ResponseEntity.ok("Images uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload images");
        }
    }

    @GetMapping("/users/{username}/images")
    public ResponseEntity<List<Map<String, String>>> getUserImages(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, String>> images = user.getImages().stream()
                .map(image -> Map.of(
                        "id", String.valueOf(image.getId()),
                        "contentType", image.getContentType() != null ? image.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        "data", Base64.getEncoder().encodeToString(image.getData())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(images);
    }

    @GetMapping("/users/{username}/images/{imageId}")
    public ResponseEntity<byte[]> getUserImage(@PathVariable String username, @PathVariable Long imageId) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return userImageRepository.findById(imageId)
                .filter(image -> image.getUser() != null && image.getUser().getId() == user.getId())
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(
                                image.getContentType() != null ? image.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                        .body(image.getData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{username}/images/{imageId}")
    public ResponseEntity<String> deleteUserImage(@PathVariable String username, @PathVariable Long imageId) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return userImageRepository.findById(imageId)
                .filter(image -> image.getUser() != null && image.getUser().getId() == user.getId())
                .map(image -> {
                    user.removeImage(image);
                    userRepository.save(user);
                    return ResponseEntity.ok("Image deleted successfully");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found"));
    }
    //test


}

