package onetomany.Checkout;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutItemRequest {
    private Long itemId;
    private int quantity;
}
