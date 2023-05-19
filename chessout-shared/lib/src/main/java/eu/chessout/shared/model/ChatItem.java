package eu.chessout.shared.model;

import java.util.Date;

public class ChatItem {

    public enum LocationType {
        CLUB_TOURNAMENT_PAIRINGS_AVAILABLE,
        POST
    }

    public enum ItemType {
        TEXT
    }

    private LocationType locationType;
    private String chatId;
    private String chatClubId;
    private String chatTournamentId;
    private String chatRoundId;

    /**
     * Not intended to be stored in database
     */
    private Boolean showOtherUserSection;

    private ItemType itemType;
    private String postId;
    private String userId;
    private String userName;
    private String userPictureUrl;
    private String textValue;

    private long timeStampCreate;
    private long timeStampEdit;


    public ChatItem() {
        // default constructor
    }

    public static ChatItem newGenericInstance(LocationType locationType,
                                              String chatClubId, String chatTournamentId,
                                              String chatRoundId, String userId) {
        ChatItem chat = new ChatItem();
        chat.locationType = locationType;
        chat.chatClubId = chatClubId;
        chat.chatTournamentId = chatTournamentId;
        chat.chatRoundId = chatRoundId;
        chat.userId = userId;
        chat.timeStampCreate = (new Date()).getTime();
        chat.timeStampEdit = chat.timeStampCreate;

        return chat;
    }

    public static ChatItem newTextInstance(LocationType locationType,
                                           String chatClubId, String chatTournamentId,
                                           String chatRoundId, ItemType itemType,
                                           String userId, String userName,
                                           String textValue) {
        ChatItem chat = newGenericInstance(locationType, chatClubId, chatTournamentId, chatRoundId, userId);

        chat.itemType = itemType;
        chat.userId = userId;
        chat.userName = userName;
        chat.textValue = textValue;

        return chat;
    }

    // <getters and setters>

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatClubId() {
        return chatClubId;
    }

    public void setChatClubId(String chatClubId) {
        this.chatClubId = chatClubId;
    }

    public String getChatTournamentId() {
        return chatTournamentId;
    }

    public void setChatTournamentId(String chatTournamentId) {
        this.chatTournamentId = chatTournamentId;
    }

    public String getChatRoundId() {
        return chatRoundId;
    }

    public void setChatRoundId(String chatRoundId) {
        this.chatRoundId = chatRoundId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPictureUrl() {
        return userPictureUrl;
    }

    public void setUserPictureUrl(String userPictureUrl) {
        this.userPictureUrl = userPictureUrl;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public long getTimeStampCreate() {
        return timeStampCreate;
    }

    public void setTimeStampCreate(long timeStampCreate) {
        this.timeStampCreate = timeStampCreate;
    }

    public long getTimeStampEdit() {
        return timeStampEdit;
    }

    public void setTimeStampEdit(long timeStampEdit) {
        this.timeStampEdit = timeStampEdit;
    }

    public Boolean getShowOtherUserSection() {
        return showOtherUserSection;
    }

    public void setShowOtherUserSection(Boolean showOtherUserSection) {
        this.showOtherUserSection = showOtherUserSection;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    // </getters and setters>
}
