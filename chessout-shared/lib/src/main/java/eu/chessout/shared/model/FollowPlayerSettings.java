package eu.chessout.shared.model;

public class FollowPlayerSettings {
    private boolean sendNotificationWhenGameResultIsUpdated;
    private String userId;

    // <getters and setters>

    public boolean isSendNotificationWhenGameResultIsUpdated() {
        return sendNotificationWhenGameResultIsUpdated;
    }

    public void setSendNotificationWhenGameResultIsUpdated(boolean sendNotificationWhenGameResultIsUpdated) {
        this.sendNotificationWhenGameResultIsUpdated = sendNotificationWhenGameResultIsUpdated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


// </getters and setters>
}
