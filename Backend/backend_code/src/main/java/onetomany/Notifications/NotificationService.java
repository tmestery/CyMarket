package onetomany.Notifications;

import onetomany.Users.User;
import onetomany.Users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.databind.ObjectMapper; // Add this import

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationWebSocket notificationWebSocket;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Add object mapper

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               @Lazy NotificationWebSocket notificationWebSocket) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationWebSocket = notificationWebSocket;
    }

    public List<NotificationDTO> getAllForUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return List.of();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(NotificationDTO::from).toList();
    }

    public Page<NotificationDTO> getPageForUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username);
        if (user == null) return Page.empty(pageable);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(NotificationDTO::from);
    }

    public long countUnread(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return 0L;
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markRead(Long id) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n != null && !n.isRead()) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }

    @Transactional
    public int markAllRead(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return 0;
        List<Notification> list = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
        return list.size();
    }

    @Transactional
    public void delete(Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
        }
    }

    @Transactional
    public int deleteAllForUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return 0;
        List<Notification> list = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        int count = list.size();
        if (count > 0) notificationRepository.deleteAll(list);
        return count;
    }

    @Transactional
    public NotificationDTO createAndSendNotification(
            User user,
            NotificationType type,
            String message,
            Long relatedEntityId,
            String relatedEntityType,
            String actionUrl
    ) {
        if (user == null) throw new IllegalArgumentException("User required");
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setMessage(message);
        n.setRelatedEntityId(relatedEntityId);
        n.setRelatedEntityType(relatedEntityType);
        n.setActionUrl(actionUrl);

        n = notificationRepository.save(n);
        NotificationDTO dto = NotificationDTO.from(n);

        String username = user.getUsername();
        if (username != null && !username.isBlank()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(dto);
                notificationWebSocket.sendNotificationToUser(username, jsonMessage);
            } catch (Exception e) {
                // Prevent WebSocket failures (common in tests/CI) from rolling back the transaction
                System.err.println("Warning: Failed to send real-time notification to user " + username + ": " + e.getMessage());
            }
        }
        return dto;
    }

    public NotificationDTO createAndSendNotification(
            String username,
            NotificationType type,
            String message
    ) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("User not found: " + username);
        return createAndSendNotification(user, type, message, null, null, null);
    }

    public NotificationDTO createAndSendNotification(
            String username,
            NotificationType type,
            String message,
            Long relatedEntityId,
            String relatedEntityType,
            String actionUrl
    ) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("User not found: " + username);
        return createAndSendNotification(user, type, message, relatedEntityId, relatedEntityType, actionUrl);
    }
}
