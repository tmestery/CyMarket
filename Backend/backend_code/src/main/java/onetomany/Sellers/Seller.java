package onetomany.Sellers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import onetomany.Items.Item;

import java.util.Date;
import java.util.HashSet;
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

   

  @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Item> items = new HashSet<>();

    public Seller() {
    }

    public Seller(String username, String bio) {
        this.username = username;
        this.bio = bio;
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
