
package onetomany.adminUser;

import onetomany.AdminActivityReport.adminActivityReport;
import onetomany.userLogIn.userLogin;


import java.util.List;

import jakarta.persistence.*;




@Entity
@Table(name="admin_Users")
public class adminUser {

    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String emailId;

    private String adminPassword;
    private int test;

    //added userName
    @Column(unique = true)
    private String username;

    @OneToMany
    List<adminActivityReport> adminActivityReportList ;

    @OneToOne
    userLogin userLogin;


    // =============================== Constructors ================================== //


    public adminUser(String name, String emailId, String adminPassword,String username) {
        this.name = name;
        this.emailId = emailId;
        this.username = username;
        this.adminPassword=adminPassword;


    }


    public adminUser() {

    }


    // =============================== Getters and Setters for each field ================================== //




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

    public List<adminActivityReport> getAdminActivityReportList() {
        return adminActivityReportList;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminActivityReportList(List<adminActivityReport> adminActivityReportList) {
        this.adminActivityReportList = adminActivityReportList;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
    public void addAminActivityReport(adminActivityReport newAdminReport){
        this.adminActivityReportList.add(newAdminReport);
    }

    public void setUserLogin(onetomany.userLogIn.userLogin userLogin) {
        this.userLogin = userLogin;
    }

    public userLogin getUserLogin() {
        return userLogin;
    }
}
