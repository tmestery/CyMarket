package onetomany.Sellers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import onetomany.Items.Item;
import onetomany.Reports.Reports;
import onetomany.userLogIn.userLogin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3–20 characters")
    @Column(nullable = false, unique = true)
    private String username;
    @Column(length = 250)
    @Size(max = 250, message = "Bio cannot exceed 250 characters")
    private String bio;
    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private Double rating;
    @Min(value = 0, message = "Ratings count cannot be negative")
    private Integer ratingsCount;
    @Min(value = 0, message = "Total sales cannot be negative")
    private Integer totalSales;
    private Boolean active;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

   @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reports> userReports = new ArrayList<>();

   

  @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Item> items = new HashSet<>();


    @OneToOne(mappedBy = "seller", cascade = CascadeType.ALL, optional = true)
    private userLogin userLogin;

    public Seller() {
    }

    public Seller(String username, String bio) {
        this.username = username;
        this.bio = bio;
        userReports = new ArrayList<>();
    }

     public Seller(String username) {
        this.username = username;
        this.bio = "";
        this.rating = 0.0;
        this.ratingsCount = 0;
        this.totalSales = 0;
        this.active = true;
        this.createdAt = new Date();
        userReports = new ArrayList<>();
     }


     public Seller(userLogin userLogin) {
        this.username = userLogin.getUserName();
        this.bio = "";
        this.rating = 0.0;
        this.ratingsCount = 0;
        this.totalSales = 0;
        this.active = true;
        this.createdAt = new Date();
        this.userLogin = userLogin;
     }

     public List<Reports> getUserReports() {
        return userReports;
    }
    public void setUserReports(List<Reports> userReports) {
        this.userReports = userReports;
    }
    public void addReport(Reports report) {
        this.userReports.add(report);
    }
    public void removeReport(Reports report) {
        this.userReports.remove(report);
    }

     public userLogin getUserLogin() {
        return userLogin;
    }
    public void setUserLogin(userLogin userLogin) {
        this.userLogin = userLogin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(Integer ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public Integer getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Integer totalSales) {
        this.totalSales = totalSales;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    public int getItemsCount() {
        return items.size();
    }

    public void addItem(Item item) {
        items.add(item);
        item.setSeller(this);
    }

    public void removeItem(Item item) {
        items.remove(item);
        item.setSeller(null);
    }
}
