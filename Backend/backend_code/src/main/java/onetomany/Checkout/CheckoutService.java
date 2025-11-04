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

            item.setQuantity(item.getQuantity() - itemRequest.getQuantity());
            if (item.getQuantity() == 0) {
                item.setIfAvailable(false);
            }
            itemsRepository.save(item);

            seller.setTotalSales(seller.getTotalSales() + itemRequest.getQuantity());
            sellerRepository.save(seller);

            // Send notifications for item purchase
            notificationService.notifyItemPurchased(user, Long.valueOf(item.getId()), item.getName());
            
            User sellerUser = seller.getUserLogin() != null ? seller.getUserLogin().getUser() : null;
            if (sellerUser != null) {
                notificationService.notifyItemSold(sellerUser, Long.valueOf(item.getId()), item.getName(), user.getUsername());
            }

            // Check and notify about low stock at 5 items
            checkAndNotifyLowStock(item, 5);
        }

        checkout.setPaymentTransactionId("TXN-" + UUID.randomUUID());
        checkout.setStatus(OrderStatus.PROCESSING);

        Checkout savedCheckout = checkoutRepository.save(checkout);

        // Send order confirmation notification to buyer
        String orderMessage = "Order #" + savedCheckout.getId() + " has been created and is being processed. Total items: " + savedCheckout.getItems().size();
        notificationService.createAndSendNotification(
            user, 
            NotificationType.TRANSACTION_PENDING, 
            orderMessage,
            savedCheckout.getId(),
            "ORDER"
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

        // Send status update notifications
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

            // Notify seller about order cancellation
            User sellerUser = seller.getUserLogin() != null ? seller.getUserLogin().getUser() : null;
            if (sellerUser != null) {
                String cancelMessage = "Order #" + checkout.getId() + " containing your item '" + item.getName() + "' has been cancelled";
                notificationService.createAndSendNotification(
                    sellerUser,
                    NotificationType.TRANSACTION_CANCELLED,
                    cancelMessage,
                    checkout.getId(),
                    "ORDER"
                );
            }
        }

        checkout.cancelOrder();
        Checkout savedCheckout = checkoutRepository.save(checkout);

        // Notify buyer about cancellation
        String buyerMessage = "Your order #" + checkout.getId() + " has been cancelled. Items have been returned to inventory.";
        notificationService.createAndSendNotification(
            checkout.getUser(),
            NotificationType.TRANSACTION_CANCELLED,
            buyerMessage,
            checkout.getId(),
            "ORDER"
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

     // Send notification based on order status change
    private void sendOrderStatusNotification(Checkout checkout, OrderStatus previousStatus, OrderStatus newStatus) {
        User buyer = checkout.getUser();
        String orderReference = "Order #" + checkout.getId();
        
        switch (newStatus) {
            case PROCESSING:
                if (previousStatus != OrderStatus.PROCESSING) {
                    String message = orderReference + " is now being processed";
                    notificationService.createAndSendNotification(
                        buyer, 
                        NotificationType.TRANSACTION_PENDING, 
                        message,
                        checkout.getId(),
                        "ORDER"
                    );
                }
                break;
                
            case SHIPPED:
                String shippedMessage = orderReference + " has been shipped to " + checkout.getShippingAddress();
                notificationService.createAndSendNotification(
                    buyer, 
                    NotificationType.TRANSACTION_PENDING, 
                    shippedMessage,
                    checkout.getId(),
                    "ORDER"
                );
                break;
                
            case COMPLETED:
                String completedMessage = orderReference + " has been completed. Thank you for your purchase!";
                notificationService.createAndSendNotification(
                    buyer, 
                    NotificationType.TRANSACTION_COMPLETED, 
                    completedMessage,
                    checkout.getId(),
                    "ORDER"
                );
                
                // Notify all sellers about completed sale
                for (CheckoutItem item : checkout.getItems()) {
                    User sellerUser = item.getSeller().getUserLogin() != null ? item.getSeller().getUserLogin().getUser() : null;
                    if (sellerUser != null) {
                        String sellerMessage = "Your item '" + item.getItem().getName() + "' from order #" + checkout.getId() + " has been delivered successfully";
                        notificationService.createAndSendNotification(
                            sellerUser,
                            NotificationType.TRANSACTION_COMPLETED,
                            sellerMessage,
                            checkout.getId(),
                            "ORDER"
                        );
                    }
                }
                break;
                
            case CANCELLED:
                break;
        }
    }

    // Notify seller about low stock
    public void checkAndNotifyLowStock(Item item, int threshold) {
        if (item.getQuantity() <= threshold && item.getQuantity() > 0) {
            User sellerUser = item.getSeller().getUserLogin() != null ? item.getSeller().getUserLogin().getUser() : null;
            if (sellerUser != null) {
                String message = "Low stock alert: Your item '" + item.getName() + "' has only " + item.getQuantity() + " units remaining";
                notificationService.createAndSendNotification(
                    sellerUser,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    message,
                    Long.valueOf(item.getId()),
                    "ITEM"
                );
            }
        } else if (item.getQuantity() == 0) {
            User sellerUser = item.getSeller().getUserLogin() != null ? item.getSeller().getUserLogin().getUser() : null;
            if (sellerUser != null) {
                String message = "Your item '" + item.getName() + "' is now out of stock";
                notificationService.createAndSendNotification(
                    sellerUser,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    message,
                    Long.valueOf(item.getId()),
                    "ITEM"
                );
            }
        }
    }
}
