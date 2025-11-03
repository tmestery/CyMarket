package onetomany.Checkout;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetomany.Items.Item;
import onetomany.Sellers.Seller;

@Entity
@Table(name = "OrderItems")
@Getter
@Setter
public class CheckoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Checkout checkout;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    private int quantity;
    private double price; // Price at the time of purchase

    private String itemName;
    private String itemDescription;

    // Constructors
    public CheckoutItem() {
    }

    public CheckoutItem(Item item, int quantity, Seller seller) {
        this.item = item;
        this.quantity = quantity;
        this.price = item.getPrice();
        this.seller = seller;
        this.itemName = item.getName();
        this.itemDescription = item.getDescription();
    }

    public double getSubtotal() {
        return price * quantity;
    }
}
