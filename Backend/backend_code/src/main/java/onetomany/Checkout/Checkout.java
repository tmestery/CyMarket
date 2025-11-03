package onetomany.Checkout;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetomany.Users.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Orders")
@Getter
@Setter
public class Checkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "checkout", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CheckoutItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Double totalPrice;

    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date completedDate;

    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    private String shippingCountry;

    private String paymentMethod;
    private String paymentTransactionId;

    private String notes;

    // Constructors
    public Checkout() {
        this.orderDate = new Date();
        this.status = OrderStatus.PENDING;
    }

    public Checkout(User user, String shippingAddress, String shippingCity, 
                    String shippingState, String shippingZipCode, String shippingCountry) {
        this();
        this.user = user;
        this.shippingAddress = shippingAddress;
        this.shippingCity = shippingCity;
        this.shippingState = shippingState;
        this.shippingZipCode = shippingZipCode;
        this.shippingCountry = shippingCountry;
    }

    // Helper methods
    public void addItem(CheckoutItem item) {
        items.add(item);
        item.setCheckout(this);
        calculateTotalPrice();
    }

    public void removeItem(CheckoutItem item) {
        items.remove(item);
        item.setCheckout(null);
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public void completeOrder() {
        this.status = OrderStatus.COMPLETED;
        this.completedDate = new Date();
    }

    public void cancelOrder() {
        this.status = OrderStatus.CANCELLED;
    }

    public int getItemCount() {
        return items.stream()
                .mapToInt(CheckoutItem::getQuantity)
                .sum();
    }
}
