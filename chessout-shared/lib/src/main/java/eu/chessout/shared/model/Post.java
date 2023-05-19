package eu.chessout.shared.model;

import java.util.HashMap;
import java.util.List;

import eu.chessout.shared.Constants;

public class Post {
    public enum PostType {
        CLUB_POST,
        USER_POST,
        TOURNAMENT_CREATED,
        TOURNAMENT_PAIRINGS_AVAILABLE,
        USER_COMMENT
    }

    private String postId;

    private String userId;
    private String clubId;
    private String tournamentClubId;
    private String tournamentId;
    private int roundId;

    private String userPictureUrl;
    private String clubPictureUrl;

    private String userName;
    private String clubShortName;

    private int likesCount;
    private int commentsCount;

    private PostType postType;
    private Link link;
    private List<Picture> pictures;

    private String message;
    private List<Link> messageLinks;

    private HashMap<String, Object> dateCreated;
    private HashMap<String, Object> updateStamp;

    private long reversedDateCreated;

    /**
     * It set the signature for date fields so firebase knows to replace them with
     * the current timestamp
     */
    public void setTimeStamps() {
        HashMap<String, Object> timeStamp = new HashMap<>();
        timeStamp.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        this.dateCreated = timeStamp;
        this.updateStamp = timeStamp;
    }

    public long dateCreatedStamp() {
        return (long) dateCreated.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    // <getters and setters>

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public List<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HashMap<String, Object> getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(HashMap<String, Object> dateCreated) {
        this.dateCreated = dateCreated;
    }

    public HashMap<String, Object> getUpdateStamp() {
        return updateStamp;
    }

    public void setUpdateStamp(HashMap<String, Object> updateStamp) {
        this.updateStamp = updateStamp;
    }

    public long getReversedDateCreated() {
        return reversedDateCreated;
    }

    public void setReversedDateCreated(long reversedDateCreated) {
        this.reversedDateCreated = reversedDateCreated;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public List<Link> getMessageLinks() {
        return messageLinks;
    }

    public void setMessageLinks(List<Link> messageLinks) {
        this.messageLinks = messageLinks;
    }

    public String getUserPictureUrl() {
        return userPictureUrl;
    }

    public void setUserPictureUrl(String userPictureUrl) {
        this.userPictureUrl = userPictureUrl;
    }

    public String getClubPictureUrl() {
        return clubPictureUrl;
    }

    public void setClubPictureUrl(String clubPictureUrl) {
        this.clubPictureUrl = clubPictureUrl;
    }

    public String getClubShortName() {
        return clubShortName;
    }

    public void setClubShortName(String clubShortName) {
        this.clubShortName = clubShortName;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getTournamentClubId() {
        return tournamentClubId;
    }

    public void setTournamentClubId(String tournamentClubId) {
        this.tournamentClubId = tournamentClubId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public int getRoundId() {
        return roundId;
    }

    public void setRoundId(int roundId) {
        this.roundId = roundId;
    }

    // </getters and setters>
}
