package onetomany.Reports;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import onetomany.Sellers.Seller;
import onetomany.Users.User;

@Entity
public class Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String report;


    @ManyToOne
    @JsonIgnore
    @JoinColumn
    private User user;

    @ManyToOne
    @JsonIgnore
    @JoinColumn
    private Seller user2;

    public int getId(){
        return this.id;
    }
    public void setId(int newId){
        this.id=newId;
    }

    public String getReport(){
        return this.report;
    }

    public User getUser(){
        return this.user;
    }

    public Reports(){
    }
    public Reports(String report){
        this.report= report;
    }

    public void setReport(String newReport){
        this.report=newReport;
    }

    public void setUser1(User user){
        this.user= user;
    }

    public void setUser2(Seller user2) {
        this.user2 = user2;
    }

    public void deleteUSer(){
        user= null;
        user2=null;
    }
}
