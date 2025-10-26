package onetomany.Notifications;

import java.util.Date;

public class NotificationDTO {
    
    private Long id;
    private String username;
    private NotificationType type;
    private String message;
    private Long relatedEntityId;
    private String relatedEntityType;
    private boolean isRead;
    private Date createdAt;
    private String actionUrl;

    public NotificationDTO() {
    }

    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.username = notification.getUser().getUsername();
        this.type = notification.getType();
        this.message = notification.getMessage();
        this.relatedEntityId = notification.getRelatedEntityId();
        this.relatedEntityType = notification.getRelatedEntityType();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
        this.actionUrl = notification.getActionUrl();
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
