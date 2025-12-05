package onetomany.Reports;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import onetomany.Sellers.Seller;
import onetomany.Users.User;

@Entity
public class  Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String report;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Seller seller;

    public Reports(User user, Seller seller, String report){
        this.user= user;
        this.seller= seller;
        this.report = report;
    }
    // test

    private boolean reviewed= false;
    public boolean isReviewed() {
        return reviewed;
    }
    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }
    public Seller getSeller(){
        return this.seller;
    }

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

    public void setSeller(Seller seller) {
        this.seller = seller;
    }


    public void deleteUser(){
        user= null;
        seller=null;
    }
}
