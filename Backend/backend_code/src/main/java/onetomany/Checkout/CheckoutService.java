package onetomany.Checkout;

import onetomany.Items.Item;
import onetomany.Items.ItemsRepository;
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
        }

        checkout.setPaymentTransactionId("TXN-" + UUID.randomUUID());
        checkout.setStatus(OrderStatus.PROCESSING);

        return checkoutRepository.save(checkout);
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

        checkout.setStatus(status);
        if (status == OrderStatus.COMPLETED) {
            checkout.completeOrder();
        }

        return checkoutRepository.save(checkout);
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
        }

        checkout.cancelOrder();
        return checkoutRepository.save(checkout);
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
}
