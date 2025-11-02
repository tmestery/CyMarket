package onetomany.Checkout;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class CheckoutResponse {
    private Long orderId;
    private int userId;
    private String username;
    private List<CheckoutItemResponse> items;
    private OrderStatus status;
    private Double totalPrice;
    private Date orderDate;
    private Date completedDate;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    private String shippingCountry;
    private String paymentMethod;
    private String paymentTransactionId;
    private String notes;
    private int itemCount;

    public CheckoutResponse(Checkout checkout) {
        this.orderId = checkout.getId();
        this.userId = checkout.getUser().getId();
        this.username = checkout.getUser().getUsername();
        this.items = checkout.getItems().stream()
                .map(CheckoutItemResponse::new)
                .collect(Collectors.toList());
        this.status = checkout.getStatus();
        this.totalPrice = checkout.getTotalPrice();
        this.orderDate = checkout.getOrderDate();
        this.completedDate = checkout.getCompletedDate();
        this.shippingAddress = checkout.getShippingAddress();
        this.shippingCity = checkout.getShippingCity();
        this.shippingState = checkout.getShippingState();
        this.shippingZipCode = checkout.getShippingZipCode();
        this.shippingCountry = checkout.getShippingCountry();
        this.paymentMethod = checkout.getPaymentMethod();
        this.paymentTransactionId = checkout.getPaymentTransactionId();
        this.notes = checkout.getNotes();
        this.itemCount = checkout.getItemCount();
    }
}