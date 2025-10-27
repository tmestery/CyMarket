package onetomany.Notifications;

public class TestNotificationRequest {
    private String type;
    private String message;
    private Long relatedEntityId;
    private String relatedEntityType;

    public TestNotificationRequest() {
    }

    public TestNotificationRequest(String type, String message) {
        this.type = type;
        this.message = message;
    }

    // Getters and Setters
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
}
