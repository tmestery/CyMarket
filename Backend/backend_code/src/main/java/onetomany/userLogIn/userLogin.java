package onetomany.userLogIn;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


    String email;

    char type;

    String password;






    public userLogin(){

    }

    @OneToOne
    @JsonIgnore
    @JoinColumn
    User user;
    @OneToOne
    @JsonIgnore
    @JoinColumn
    adminUser adminUser;


    public userLogin(String userName, String email, char type, String password){
        this.email= email;
        this.userName= userName;
        this.password= password;
        this.type= type;
    }


    public void setId(int id){
        this.id= id;
    }
    public void setUser(User newUser){
        this.user= newUser;
    }
    public void setPassword(String newPassword){
        this.password= password;
    }

    public User getUser() {
        return user;
    }

    public char getType() {
        return type;
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

    public void setType(char type) {
        this.type = type;
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
