package onetomany.Notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import onetomany.Users.User;
import onetomany.Users.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

     // Create and send notification to a user with webSocket
    public Notification createAndSendNotification(User user, NotificationType type, String message) {
        Notification notification = new Notification(user, type, message);
        notification = notificationRepository.save(notification);
        sendNotificationToUser(user.getUsername(), notification);
        
        return notification;
    }

     // Create and send a notification with entity details
    public Notification createAndSendNotification(User user, NotificationType type, String message, 
                                                 Long relatedEntityId, String relatedEntityType) {
        Notification notification = new Notification(user, type, message, relatedEntityId, relatedEntityType);
        notification = notificationRepository.save(notification);
        sendNotificationToUser(user.getUsername(), notification);
        
        return notification;
    }

    // Send notification to a specific user with webSocket
    private void sendNotificationToUser(String username, Notification notification) {
        NotificationDTO dto = new NotificationDTO(notification);
        messagingTemplate.convertAndSendToUser(
            username, 
            "/queue/notifications", 
            dto
        );
    }


     // get all notifications
    public List<NotificationDTO> getUserNotifications(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }


     // Get all notifications *paginated
    public Page<NotificationDTO> getUserNotifications(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return notifications.map(NotificationDTO::new);
    }


     // Get unread notifications
    public List<NotificationDTO> getUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }


     // Get unread notification count
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return 0;
        }
        
        return notificationRepository.countByUserAndIsReadFalse(user);
    }


     // Mark a notification as read
    @Transactional
    public boolean markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return false;
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
        return true;
    }


     // Mark all notifications as read
    @Transactional
    public boolean markAllAsRead(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
        return true;
    }
     // Delete notification
    @Transactional
    public boolean deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            return false;
        }
        
        notificationRepository.deleteById(notificationId);
        return true;
    }


     // Delete all notifications
    @Transactional
    public boolean deleteAllUserNotifications(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        
        notificationRepository.deleteByUser(user);
        return true;
    }


     // Send notification announcement to everyone
    @Transactional
    public void sendSystemAnnouncement(String message) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            createAndSendNotification(user, NotificationType.SYSTEM_ANNOUNCEMENT, message);
        }
    }
}
