package onetomany.Notifications;

import onetomany.Users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);

    @Transactional
    void deleteByUser(User user);
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    long countByUserAndIsReadFalse(User user);
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

}
