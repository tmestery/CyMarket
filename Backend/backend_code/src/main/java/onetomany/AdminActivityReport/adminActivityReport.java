
package onetomany.AdminActivityReport;
import jakarta.persistence.*;
import onetomany.adminUser.adminUser;
import onetomany.adminUser.adminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
@Table(name="admin_Activities")
public class adminActivityReport {

    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String descriptionOfActivity;
    private String emailId;


    //added userName
    private String username;

    private char status = 'O';


    @Column(unique = true)
    private int reportID;

    @ManyToOne
    adminUser adminUser;







    // =============================== Constructors ================================== //


    public adminActivityReport(String emailId,String username, int ReportID, String descriptionOfActivity) {
        this.emailId = emailId;
        this.username = username;
        this.reportID= ReportID;
        this.descriptionOfActivity= descriptionOfActivity;


    }


    public adminActivityReport() {

    }


    // =============================== Getters and Setters for each field ================================== //




    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
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

    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    public void setDescriptionOfActivity(String descriptionOfActivity) {
        this.descriptionOfActivity = descriptionOfActivity;
    }

    public int getReportID() {
        return reportID;
    }

    public String getDescriptionOfActivity() {
        return descriptionOfActivity;
    }

    public void setAdminUser(adminUser adminUser) {
        this.adminUser = adminUser;
    }

    public adminUser getAdminUser() {
        return adminUser;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public char getStatus() {
        return status;
    }
}
