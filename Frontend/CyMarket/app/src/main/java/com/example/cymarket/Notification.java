package com.example.cymarket;

public class Notification {

    public Long id;
    public String username;
    public String type;
    public String message;
    public Long relatedEntityId;
    public String relatedEntityType;
    public boolean isRead;
    public String createdAt;
    public String actionUrl;

    public Notification(Long id,
                        String username,
                        String type,
                        String message,
                        Long relatedEntityId,
                        String relatedEntityType,
                        boolean isRead,
                        String createdAt,
                        String actionUrl) {
        this.id = id;
        this.username = username;
        this.type = type;
        this.message = message;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.actionUrl = actionUrl;
    }

    public Notification() {

    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
