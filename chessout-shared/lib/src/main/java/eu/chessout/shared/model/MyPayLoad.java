package eu.chessout.shared.model;

/**
 * Created by Bogdan Oloeriu on 04/07/2016.
 */
public class MyPayLoad {

    public enum Event {
        GAME_RESULT_UPDATED, CLUB_POST_CREATED,
        CLUB_POST_DELETED, CLUB_POST_PICTURE_UPLOAD_COMPLETE
    }

    private Event event;
    private String authToken;
    private String gameType;
    private String tournamentType;
    private String clubId;
    private String tournamentId;
    private String roundId;
    private String tableId;
    private String postId;
    private Integer pictureIndex;


    //getters and setters

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getRoundId() {
        return roundId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(String tournamentType) {
        this.tournamentType = tournamentType;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Integer getPictureIndex() {
        return pictureIndex;
    }

    public void setPictureIndex(Integer pictureIndex) {
        this.pictureIndex = pictureIndex;
    }
}
