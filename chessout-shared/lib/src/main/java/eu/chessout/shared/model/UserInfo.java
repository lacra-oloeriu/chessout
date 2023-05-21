package eu.chessout.shared.model;

/**
 * Used to collect in a single object relevant information about a user
 */
public class UserInfo {
    private String userId;
    private String userName;
    private String pictureUri;

    // <getters and setters>

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userKey) {
        this.userId = userKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPictureUri() {
        return pictureUri;
    }

    public void setPictureUri(String pictureUri) {
        this.pictureUri = pictureUri;
    }


    // </getters and setters>
}
