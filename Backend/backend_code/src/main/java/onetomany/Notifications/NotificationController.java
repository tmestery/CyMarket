package onetomany.Notifications;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetomany.Users.User;
import onetomany.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /* -------------------- Reads -------------------- */

    @GetMapping("/{username}")
    @Operation(summary = "List notifications", description = "Optional pagination with ?page=0&size=20")
    public ResponseEntity<?> listForUser(
            @PathVariable String username,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            if (page < 0 || size <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid pagination parameters"));
            }
            return ResponseEntity.ok(
                    notificationService.getPageForUser(username, PageRequest.of(page, size))
            );
        }
        List<NotificationDTO> list = notificationService.getAllForUser(username);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{username}/unread")
    @Operation(summary = "List unread notifications")
    public ResponseEntity<?> listUnread(@PathVariable String username) {
        List<NotificationDTO> all = notificationService.getAllForUser(username);
        List<NotificationDTO> unread = all.stream().filter(n -> !n.isRead()).toList();
        return ResponseEntity.ok(unread);
    }

    @GetMapping("/{username}/unread-count")
    @Operation(summary = "Get unread count")
    public Map<String, Long> unreadCount(@PathVariable String username) {
        return Map.of("count", notificationService.countUnread(username));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("message", "success", "id", id));
    }

    @PatchMapping("/{username}/read-all")
    @Operation(summary = "Mark all notifications as read for a user")
    public ResponseEntity<?> markAllRead(@PathVariable String username) {
        int updated = notificationService.markAllRead(username);
        return ResponseEntity.ok(Map.of("message", "success", "updated", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification by id")
    public ResponseEntity<?> deleteOne(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.ok(Map.of("message", "success", "id", id));
    }

    @DeleteMapping("/{username}/all")
    @Operation(summary = "Delete all notifications for a user")
    public ResponseEntity<?> deleteAll(@PathVariable String username) {
        int deleted = notificationService.deleteAllForUser(username);
        return ResponseEntity.ok(Map.of("message", "success", "deleted", deleted));
    }

    @PostMapping("/test/{username}")
    @Operation(summary = "Send a test notification (persist + live push)")
    public ResponseEntity<?> testSend(
            @PathVariable String username,
            @RequestBody TestNotificationRequest req
    ) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "user not found"));
        }
        NotificationType type;
        try {
            type = NotificationType.valueOf(req.getType());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid notification type"));
        }

        NotificationDTO dto = notificationService.createAndSendNotification(
                user,
                type,
                (req.getMessage() == null ? "Test notification" : req.getMessage()),
                req.getRelatedEntityId(),
                req.getRelatedEntityType(),
                req.getActionUrl()   // ok to be null
        );

        return ResponseEntity.ok(dto);
    }
}

