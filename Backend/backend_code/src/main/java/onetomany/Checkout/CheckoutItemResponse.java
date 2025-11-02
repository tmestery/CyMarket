package onetomany.Checkout;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutItemResponse {
    private Long itemId;
    private String itemName;
    private String itemDescription;
    private int quantity;
    private double price;
    private double subtotal;
    private Long sellerId;
    private String sellerName;

    public CheckoutItemResponse(CheckoutItem checkoutItem) {
        this.itemId = Long.valueOf(checkoutItem.getItem().getId());
        this.itemName = checkoutItem.getItemName();
        this.itemDescription = checkoutItem.getItemDescription();
        this.quantity = checkoutItem.getQuantity();
        this.price = checkoutItem.getPrice();
        this.subtotal = checkoutItem.getSubtotal();
        this.sellerId = checkoutItem.getSeller().getId();
        this.sellerName = checkoutItem.getSeller().getUsername();
    }
}