
package onetomany.Users;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import onetomany.Items.Item;




@Entity
@Table(name="Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    
    private Date joiningDate;
    private boolean ifActive;
    private String UserPassword;
    private Date lastLoggin;

    @Column(unique = true)
    private String username;
    
    @Column(unique = true)
    private String emailId;

     @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_items",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private Set<Item> likedItems = new HashSet<>();

    private String passwordRecoveryCode;

    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordRecoveryExpiry;

    @Lob
    private byte[] profileImage;
   




 




    // =============================== Constructors ================================== //


    public User(String name, String emailId, String userPassword,String username ) {
        this.name = name;
        this.emailId = emailId;
        this.joiningDate = new Date();
        this.ifActive = true;
    
        this.UserPassword= userPassword;

        this.username = username;
        
        this.lastLoggin=new Date();


    }

    public User() {
       
    }
     
    public void addItem(Item item){
        this.likedItems.add(item);
    }
    @JsonIgnore
    public Set<Item> getLikedItems(){
        return this.likedItems;
    }
     public int getLikedItemsCount(){
        return this.likedItems.size();
    }
    public void removeItem(Item item){
        this.likedItems.remove(item);
    }

    
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

   public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailId(){
        return emailId;
    }

    public void setEmailId(String emailId){
        this.emailId = emailId;
    }

    public Date getJoiningDate(){
        return joiningDate;
    }

    public void setJoiningDate(Date joiningDate){
        this.joiningDate = joiningDate;
    }




    public boolean isIfActive() {
        return ifActive;
    }

    public void setIfActive(boolean ifActive){
        this.ifActive = ifActive;
    }

    public String getUserPassword(){
        return this.UserPassword;
    }

    public void setUserPassword(String userPassword) {
        this.UserPassword = userPassword;
    }

    public void setLastLoggin(){
        lastLoggin= new Date();
    }


    public Date getLastLoggin(){
        return this.lastLoggin;
    }

   
    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

    public String getPasswordRecoveryCode() {
        return passwordRecoveryCode;
    }

    public void setPasswordRecoveryCode(String passwordRecoveryCode) {
        this.passwordRecoveryCode = passwordRecoveryCode;
    }

    public Date getPasswordRecoveryExpiry() {
        return passwordRecoveryExpiry;
    }

    public void setPasswordRecoveryExpiry(Date passwordRecoveryExpiry) {
        this.passwordRecoveryExpiry = passwordRecoveryExpiry;
    }
   

    public User getUser(User user){
        User temp = new User();
        return temp;
    }

    
}
