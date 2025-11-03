package onetomany.Checkout;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequest {
    private int userId;
    private List<CheckoutItemRequest> items;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    private String shippingCountry;
    private String paymentMethod;
    private String notes;
}
