package onetomany.Checkout;

import onetomany.Items.Item;
import onetomany.Items.ItemsRepository;
import onetomany.Notifications.NotificationService;
import onetomany.Notifications.NotificationType;
import onetomany.Sellers.Seller;
import onetomany.Sellers.SellerRepository;
import onetomany.Users.User;
import onetomany.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CheckoutService {

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemsRepository itemsRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Checkout createCheckout(CheckoutRequest request) {
        // Validate card payment
        validateCardPaymentFields(request);
        
        User user = userRepository.findById(request.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + request.getUserId());
        }

        Checkout checkout = new Checkout(
                user,
                request.getShippingAddress(),
                request.getShippingCity(),
                request.getShippingState(),
                request.getShippingZipCode(),
                request.getShippingCountry()
        );
        
        // Set card payment information store last 4
        checkout.setCardInfo(request.getCardNumber(), request.getCardHolderName());
        
        // Set billing address
        checkout.setBillingAddress(request.getBillingAddress());
        checkout.setBillingCity(request.getBillingCity());
        checkout.setBillingState(request.getBillingState());
        checkout.setBillingZipCode(request.getBillingZipCode());
        checkout.setBillingCountry(request.getBillingCountry());
        
        checkout.setNotes(request.getNotes());

        for (CheckoutItemRequest itemRequest : request.getItems()) {
            Item item = itemsRepository.findById(itemRequest.getItemId()).orElse(null);
            if (item == null) {
                throw new IllegalArgumentException("Item not found with id: " + itemRequest.getItemId());
            }

            if (!item.isIfAvailable()) {
                throw new IllegalStateException("Item is not available: " + item.getName());
            }

            if (item.getQuantity() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Insufficient quantity for item: " + item.getName()
                        + ". Available: " + item.getQuantity()
                        + ", Requested: " + itemRequest.getQuantity());
            }

            Seller seller = item.getSeller();
            if (seller == null) {
                throw new IllegalStateException("Item has no seller: " + item.getName());
            }

            CheckoutItem checkoutItem = new CheckoutItem(item, itemRequest.getQuantity(), seller);
            checkout.addItem(checkoutItem);

            // update inventory
            item.setQuantity(item.getQuantity() - itemRequest.getQuantity());
            if (item.getQuantity() == 0) {
                item.setIfAvailable(false);
            }
            itemsRepository.save(item);

            // update seller sales count
            seller.setTotalSales(seller.getTotalSales() + itemRequest.getQuantity());
            sellerRepository.save(seller);

            // Notify buyer that item was added or purchased
            String buyerMsg = "Added '" + item.getName() + "' x" + itemRequest.getQuantity() + " to your order.";
            notificationService.createAndSendNotification(
                    user,
                    NotificationType.TRANSACTION_PENDING,
                    buyerMsg,
                    null,
                    "ITEM",
                    null
            );

            // Notify seller item was purchased
            User sellerUser = seller.getUserLogin() != null ? seller.getUserLogin().getUser() : null;
            if (sellerUser != null) {
                String sellerMsg = "Your item '" + item.getName() + "' was purchased by " + user.getUsername()
                        + " (qty " + itemRequest.getQuantity() + ") via card payment.";
                notificationService.createAndSendNotification(
                        sellerUser,
                        NotificationType.TRANSACTION_PENDING,
                        sellerMsg,
                        Long.valueOf(item.getId()),
                        "ITEM",
                        null
                );
            }
            checkAndNotifyLowStock(item, 5);
        }

        // Process card payment
        String transactionId = processCardPayment(request, checkout.getTotalPrice());
        checkout.setPaymentTransactionId(transactionId);
        checkout.setStatus(OrderStatus.PROCESSING);

        Checkout savedCheckout = checkoutRepository.save(checkout);

        // Order confirmation
        String orderMessage = "Order #" + savedCheckout.getId()
                + " created and is being processed. Card ending in " + savedCheckout.getCardLastFour() 
                + " charged $" + String.format("%.2f", savedCheckout.getTotalPrice()) 
                + ". Total items: " + savedCheckout.getItems().size();
        notificationService.createAndSendNotification(
                user,
                NotificationType.TRANSACTION_PENDING,
                orderMessage,
                savedCheckout.getId(),
                "ORDER",
                null
        );

        return savedCheckout;
    }

    public List<Checkout> getUserOrders(int userId) {
        return checkoutRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public Checkout getOrderById(Long orderId) {
        return checkoutRepository.findById(orderId).orElse(null);
    }

    @Transactional
    public Checkout updateOrderStatus(Long orderId, OrderStatus status) {
        Checkout checkout = checkoutRepository.findById(orderId).orElse(null);
        if (checkout == null) {
            throw new IllegalArgumentException("Order not found with id: " + orderId);
        }

        OrderStatus previousStatus = checkout.getStatus();
        checkout.setStatus(status);
        if (status == OrderStatus.COMPLETED) {
            checkout.completeOrder();
        }

        Checkout savedCheckout = checkoutRepository.save(checkout);

        //  status update
        sendOrderStatusNotification(savedCheckout, previousStatus, status);

        return savedCheckout;
    }

    @Transactional
    public Checkout cancelOrder(Long orderId) {
        Checkout checkout = checkoutRepository.findById(orderId).orElse(null);
        if (checkout == null) {
            throw new IllegalArgumentException("Order not found with id: " + orderId);
        }

        if (checkout.getStatus() == OrderStatus.COMPLETED ||
                checkout.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order with status: " + checkout.getStatus());
        }

        for (CheckoutItem checkoutItem : checkout.getItems()) {
            Item item = checkoutItem.getItem();
            item.setQuantity(item.getQuantity() + checkoutItem.getQuantity());
            item.setIfAvailable(true);
            itemsRepository.save(item);

            Seller seller = checkoutItem.getSeller();
            seller.setTotalSales(seller.getTotalSales() - checkoutItem.getQuantity());
            sellerRepository.save(seller);

            // Notify for cancellation (seller)
            User sellerUser = seller.getUserLogin() != null ? seller.getUserLogin().getUser() : null;
            if (sellerUser != null) {
                String cancelMessage = "Order #" + checkout.getId()
                        + " containing your item '" + item.getName() + "' has been cancelled.";
                notificationService.createAndSendNotification(
                        sellerUser,
                        NotificationType.TRANSACTION_CANCELLED,
                        cancelMessage,
                        checkout.getId(),
                        "ORDER",
                        null
                );
            }
        }

        checkout.cancelOrder();
        Checkout savedCheckout = checkoutRepository.save(checkout);

        // Notify buyer about cancellation
        String buyerMessage = "Your order #" + checkout.getId()
                + " has been cancelled. Items have been returned to inventory.";
        notificationService.createAndSendNotification(
                checkout.getUser(),
                NotificationType.TRANSACTION_CANCELLED,
                buyerMessage,
                checkout.getId(),
                "ORDER",
                null
        );

        return savedCheckout;
    }

    public List<Checkout> getAllOrders() {
        return checkoutRepository.findAll();
    }

    public List<Checkout> getOrdersByStatus(OrderStatus status) {
        return checkoutRepository.findByStatus(status);
    }

    public long getUserOrderCount(int userId) {
        return checkoutRepository.countByUserId(userId);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Checkout checkout = checkoutRepository.findById(orderId).orElse(null);
        if (checkout == null) {
            throw new IllegalArgumentException("Order not found with id: " + orderId);
        }
        checkoutRepository.delete(checkout);
    }

    private void sendOrderStatusNotification(Checkout checkout, OrderStatus previousStatus, OrderStatus newStatus) {
        User buyer = checkout.getUser();
        String orderReference = "Order #" + checkout.getId();

        switch (newStatus) {
            case PROCESSING -> {
                if (previousStatus != OrderStatus.PROCESSING) {
                    String message = orderReference + " is now being processed.";
                    notificationService.createAndSendNotification(
                            buyer,
                            NotificationType.TRANSACTION_PENDING,
                            message,
                            checkout.getId(),
                            "ORDER",
                            null
                    );
                }
            }
            case SHIPPED -> {
                String shippedMessage = orderReference + " has been shipped to " + checkout.getShippingAddress() + ".";
                notificationService.createAndSendNotification(
                        buyer,
                        NotificationType.TRANSACTION_PENDING,
                        shippedMessage,
                        checkout.getId(),
                        "ORDER",
                        null
                );
            }
            case COMPLETED -> {
                String completedMessage = orderReference + " has been completed. Thank you for your purchase!";
                notificationService.createAndSendNotification(
                        buyer,
                        NotificationType.TRANSACTION_COMPLETED,
                        completedMessage,
                        checkout.getId(),
                        "ORDER",
                        null
                );

                for (CheckoutItem item : checkout.getItems()) {
                    User sellerUser = item.getSeller().getUserLogin() != null
                            ? item.getSeller().getUserLogin().getUser() : null;
                    if (sellerUser != null) {
                        String sellerMessage = "Your item '" + item.getItem().getName()
                                + "' from " + orderReference + " has been delivered successfully.";
                        notificationService.createAndSendNotification(
                                sellerUser,
                                NotificationType.TRANSACTION_COMPLETED,
                                sellerMessage,
                                checkout.getId(),
                                "ORDER",
                                null
                        );
                    }
                }
            }
            case CANCELLED -> { /* already handled in cancelOrder */ }
        }
    }

    // Low stock notification (seller)
    public void checkAndNotifyLowStock(Item item, int threshold) {
        User sellerUser = item.getSeller().getUserLogin() != null
                ? item.getSeller().getUserLogin().getUser() : null;
        if (sellerUser == null) return;

        if (item.getQuantity() <= threshold && item.getQuantity() > 0) {
            String message = "Low stock alert: Your item '" + item.getName()
                    + "' has only " + item.getQuantity() + " units remaining.";
            notificationService.createAndSendNotification(
                    sellerUser,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    message,
                    Long.valueOf(item.getId()),
                    "ITEM",
                    null
            );
        } else if (item.getQuantity() == 0) {
            String message = "Your item '" + item.getName() + "' is now out of stock.";
            notificationService.createAndSendNotification(
                    sellerUser,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    message,
                    Long.valueOf(item.getId()),
                    "ITEM",
                    null
            );
        }
    }

    private void validateCardPaymentFields(CheckoutRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Card holder name is required");
        }
        
        if (request.getExpirationMonth() == null || request.getExpirationMonth().trim().isEmpty()) {
            throw new IllegalArgumentException("Expiration month is required");
        }
        
        if (request.getExpirationYear() == null || request.getExpirationYear().trim().isEmpty()) {
            throw new IllegalArgumentException("Expiration year is required");
        }
        
        if (request.getCvv() == null || request.getCvv().trim().isEmpty()) {
            throw new IllegalArgumentException("CVV is required");
        }
        
        // Validate card number format (basic validation)
        String cardNumber = request.getCardNumber().replaceAll("\\s+", "");
        if (!cardNumber.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Invalid card number format");
        }
        
        // Validate expiration month
        try {
            int month = Integer.parseInt(request.getExpirationMonth());
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Invalid expiration month");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid expiration month format");
        }
        
        // Validate expiration year
        try {
            int year = Integer.parseInt(request.getExpirationYear());
            int currentYear = java.time.Year.now().getValue();
            if (year < currentYear) {
                throw new IllegalArgumentException("Card has expired");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid expiration year format");
        }
        
        // Validate CVV
        if (!request.getCvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Invalid CVV format");
        }
        
        // Validate billing address fields
        if (request.getBillingAddress() == null || request.getBillingAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Billing address is required");
        }
        
        if (request.getBillingCity() == null || request.getBillingCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Billing city is required");
        }
        
        if (request.getBillingState() == null || request.getBillingState().trim().isEmpty()) {
            throw new IllegalArgumentException("Billing state is required");
        }
        
        if (request.getBillingZipCode() == null || request.getBillingZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Billing zip code is required");
        }
        
        if (request.getBillingCountry() == null || request.getBillingCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Billing country is required");
        }
    }
    
    private String processCardPayment(CheckoutRequest request, Double totalAmount) {
        // Simulate payment processing
        try {
            String cardNumber = request.getCardNumber().replaceAll("\\s+", "");
            
            // Generate a mock transaction ID
            String transactionId = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            System.out.println("Processing card payment: $" + totalAmount + 
                             " for card ending in " + cardNumber.substring(cardNumber.length() - 4));
            
            return transactionId;
            
        } catch (Exception e) {
            throw new IllegalStateException("Payment processing failed: " + e.getMessage());
        }
    }
}
