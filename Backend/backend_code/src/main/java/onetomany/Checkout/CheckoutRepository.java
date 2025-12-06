package onetomany.Checkout;

import onetomany.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CheckoutRepository extends JpaRepository<Checkout, Long> {
    
    List<Checkout> findByUser(User user);
    
    List<Checkout> findByUserId(int userId);
    
    List<Checkout> findByStatus(OrderStatus status);
    
    @Query("SELECT c FROM Checkout c WHERE c.user.id = :userId ORDER BY c.orderDate DESC")
    List<Checkout> findByUserIdOrderByOrderDateDesc(@Param("userId") int userId);
    
    @Query("SELECT c FROM Checkout c WHERE c.user.id = :userId AND c.status = :status")
    List<Checkout> findByUserIdAndStatus(@Param("userId") int userId, @Param("status") OrderStatus status);

    @Query("SELECT SUM(c.totalPrice) FROM Checkout c")
    Double getTotalSales();

    long countByUserId(int userId);
}
