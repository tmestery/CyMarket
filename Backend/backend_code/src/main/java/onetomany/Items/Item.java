package onetomany.Items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import onetomany.Sellers.Seller;
import onetomany.Users.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "Items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    private double price;
    private Date creationDate;
    private boolean ifAvailable;
    private String category;
    private int viewCount = 0;


    @Column(unique = true)
    private String username;

    @ManyToOne
    @JsonIgnore
    @JoinColumn
    private Seller seller;

    @ManyToMany
    @JoinTable(
            name = "user_liked_items",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedByUsers = new HashSet<>();


    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemImage> images = new ArrayList<>();


    // =============================== Constructors ================================== //

    public Item(String name, String description, double price, String category, String userName) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.creationDate = new Date();
        this.ifAvailable = true;

        this.category = category;

        this.username = userName;


    }

    public Item() {

    }

    public void addLikedByUser(User user) {
        this.likedByUsers.add(user);
    }
    public void removeLikedByUser(User user) {
        this.likedByUsers.remove(user);
    }

    public Set<User> getLikedByUsers() {
        return this.likedByUsers;
    }

    public int getLikedByUsersCount() {
        return this.likedByUsers.size();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getDescription(){
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


    public boolean isIfAvailable() {
        return ifAvailable;
    }

    public void setIfAvailable(boolean ifAvailable) {
        this.ifAvailable = ifAvailable;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void addCount() {
        this.viewCount++;
    }

    public List<ItemImage> getImages() {
        return images;
    }

    public void setImages(List<ItemImage> images) {
        this.images = images;
    }

    public void addImage(ItemImage image) {
        if (image != null) {
            images.add(image);
            image.setItem(this);
        }
    }

    public void removeImage(ItemImage image) {
        if (image != null && images.remove(image)) {
            image.setItem(null);
        }
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public Long getSellerId() {
        return seller != null ? seller.getId() : null;
    }
}
