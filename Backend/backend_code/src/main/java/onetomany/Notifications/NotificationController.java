package onetomany.Notifications;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import onetomany.Users.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notification Management", description = "APIs for managing user notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;

    private static final String SUCCESS = "{\"message\":\"success\"}";
    private static final String FAILURE = "{\"message\":\"failure\"}";

    // Get all notifications for a user
    @GetMapping("/{username}")
    @Operation(summary = "Get all notifications for a user", 
               description = "Supports optional pagination with ?page=0&size=10")
    public ResponseEntity<?> getUserNotifications(@PathVariable String username,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            if (page < 0 || size <= 0) {
                return ResponseEntity.badRequest().body("{\"message\":\"Invalid pagination parameters\"}");
            }
            var list = notificationService.getUserNotifications(username, PageRequest.of(page, size));
            return list == null ? ResponseEntity.badRequest().body(FAILURE) : ResponseEntity.ok(list);
        }
        List<NotificationDTO> list = notificationService.getUserNotifications(username);
        return list == null ? ResponseEntity.badRequest().body(FAILURE) : ResponseEntity.ok(list);
    }


     // Get unread notifications for a user
    @GetMapping("/{username}/unread")
    @Operation(summary = "Get unread notifications for a user")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable String username) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(username);
        return notifications == null ? ResponseEntity.badRequest().body(FAILURE) : ResponseEntity.ok(notifications);
    }

     // Get unread notification count
    @GetMapping("/{username}/unread/count")
    @Operation(summary = "Get count of unread notifications")
    public Map<String, Long> getUnreadCount(@PathVariable String username) {
        return Map.of("count", notificationService.getUnreadCount(username));
    }

     // Mark a notification as read
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        boolean result = notificationService.markAsRead(notificationId);
        if (result) {
            return ResponseEntity.ok(Map.of("message", "success", "id", notificationId));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Notification not found", "id", notificationId));
    }

     // Mark all notifications as read
    @PutMapping("/{username}/read-all")
    @Operation(summary = "Mark all notifications as read for a user")
    public String markAllAsRead(@PathVariable String username) {
        return notificationService.markAllAsRead(username) ? SUCCESS : FAILURE;
    }


     // Delete notification
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public String deleteNotification(@PathVariable Long notificationId) {
        return notificationService.deleteNotification(notificationId) ? SUCCESS : FAILURE;
    }

     // Delete all notifications
    @DeleteMapping("/{username}/all")
    @Operation(summary = "Delete all notifications for a user")
    public String deleteAllNotifications(@PathVariable String username) {
        return notificationService.deleteAllUserNotifications(username) ? SUCCESS : FAILURE;
    }


     // Test endpoint to send notification for testing
    @PostMapping("/test/{username}")
    @Operation(summary = "Send a test notification", description = "Useful for testing the notification system")
    public ResponseEntity<String> sendTestNotification(@PathVariable String username, 
                                                       @RequestBody TestNotificationRequest request) {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body("{\"message\":\"user not found\"}");
        }
        
        try {
            if (request.getRelatedEntityId() != null && request.getRelatedEntityType() != null) {
                notificationService.createAndSendNotification(
                    user,
                    NotificationType.valueOf(request.getType()),
                    request.getMessage(),
                    request.getRelatedEntityId(),
                    request.getRelatedEntityType()
                );
            } else {
                notificationService.createAndSendNotification(
                    user,
                    NotificationType.valueOf(request.getType()),
                    request.getMessage()
                );
            }
            return ResponseEntity.ok(SUCCESS);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"message\":\"Invalid notification type\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(FAILURE);
        }
    }
}
