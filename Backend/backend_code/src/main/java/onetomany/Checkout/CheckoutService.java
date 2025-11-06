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
        checkout.setPaymentMethod(request.getPaymentMethod());
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
                        + " (qty " + itemRequest.getQuantity() + ").";
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

        checkout.setPaymentTransactionId("TXN-" + UUID.randomUUID());
        checkout.setStatus(OrderStatus.PROCESSING);

        Checkout savedCheckout = checkoutRepository.save(checkout);

        // Order confirmation
        String orderMessage = "Order #" + savedCheckout.getId()
                + " created and is being processed. Total items: " + savedCheckout.getItems().size();
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
}
