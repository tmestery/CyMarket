package onetomany.Checkout;

import onetomany.Users.User;
import onetomany.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:3000") // Add this annotation
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckoutRepository checkoutRepository;

    // create order
    @PostMapping
    public ResponseEntity<?> createCheckout(@RequestBody CheckoutRequest request) {
        try {
            Checkout savedCheckout = checkoutService.createCheckout(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CheckoutResponse(savedCheckout));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating checkout: " + e.getMessage());
        }
    }

    // get orders by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable int userId) {
        try {
            User user = userRepository.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id: " + userId);
            }

            List<Checkout> orders = checkoutService.getUserOrders(userId);
            List<CheckoutResponse> response = orders.stream()
                    .map(CheckoutResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching orders: " + e.getMessage());
        }
    }

    // get order by id
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            Checkout checkout = checkoutService.getOrderById(orderId);

            if (checkout == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Order not found with id: " + orderId);
            }

            return ResponseEntity.ok(new CheckoutResponse(checkout));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching order: " + e.getMessage());
        }
    }

    // update order status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        try {
            Checkout updatedCheckout = checkoutService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(new CheckoutResponse(updatedCheckout));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating order status: " + e.getMessage());
        }
    }

    // cancel order
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            Checkout cancelledCheckout = checkoutService.cancelOrder(orderId);
            return ResponseEntity.ok(new CheckoutResponse(cancelledCheckout));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling order: " + e.getMessage());
        }
    }

    // Get all orders
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Checkout> orders = checkoutService.getAllOrders();
            List<CheckoutResponse> response = orders.stream()
                    .map(CheckoutResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching orders: " + e.getMessage());
        }
    }

    // Get order status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<Checkout> orders = checkoutService.getOrdersByStatus(status);
            List<CheckoutResponse> response = orders.stream()
                    .map(CheckoutResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching orders: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<?> getUserOrderCount(@PathVariable int userId) {
        return new ResponseEntity<>(checkoutService.getUserOrderCount(userId), HttpStatus.OK);
    }

    @GetMapping("/sales/total")
    public ResponseEntity<Double> getTotalSales() {
        return new ResponseEntity<>(checkoutService.getTotalSales(), HttpStatus.OK);
    }

    // Delete order
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        try {
            checkoutService.deleteOrder(orderId);
            return ResponseEntity.ok("Order deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting order: " + e.getMessage());
        }
    }
}