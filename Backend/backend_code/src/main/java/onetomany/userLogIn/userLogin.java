package onetomany.userLogIn;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import onetomany.Sellers.Seller;
import onetomany.Users.User;
import onetomany.adminUser.adminUser;


@Entity
@Table(name = "userLogin")
public class userLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(unique = true)
    private String userName;

    String name;

    String email;

    char type;

    String password;






    public userLogin(){

    }

    @OneToOne
    @JoinColumn(name="user_id")
    @JsonIgnore
    private User user;

    @OneToOne
    @JsonIgnore
    @JoinColumn
    adminUser adminUser;

    @OneToOne
    @JoinColumn(name="seller_id")
    @JsonIgnore
    private Seller seller;


    public userLogin(String userName, String email, String password,String name){
        this.email= email;
        this.userName= userName;
        this.password= password;
        this.name = name;
        
    }

    public userLogin(String userName, String email, char type, String password){
        this.email= email;
        this.userName= userName;
        this.password= password;
        this.type= type;
    }


    public String getName() {
        return name;
    }
    public void setName(String newName) {
        this.name = newName;
    }

    public Seller getSeller() {
        return seller;
    }
    public void setSeller(Seller newSeller){
        this.seller= newSeller;
    }
    public adminUser getAdminUser() {
        return adminUser;
    }



    public void setId(int id){
        this.id= id;
    }
    public void setUser(User newUser){
        this.user= newUser;
    }
    public void setPassword(String newPassword){
        this.password= newPassword;
    }

    
    public User getUser() {
        return user;
    }

    public char getType() {
        return this.type;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setType(char newtype) {
        this.type = newtype;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAdminUser(adminUser newAdminUser){
        this.adminUser = newAdminUser;
    }
    public void deleteLoginUser(User user){
        this.user= null;
        this.userName= null;
    }
}
