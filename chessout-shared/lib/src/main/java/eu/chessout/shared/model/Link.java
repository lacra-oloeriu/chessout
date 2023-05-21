package eu.chessout.shared.model;

public class Link {
    enum LinkType {
        NO_LINK,
        TOURNAMENT
    }

    private LinkType linkType;
    private String linkedText;
    private String clubId;
    private String playerId;
    private String tournamentId;
    private String roundId;
    private String gameId;

    /**
     * It check is the provided message contains the linked text
     *
     * @param message
     * @return
     */
    public boolean isValid(String message) {
        if (message.contains(linkedText)) {
            return true;
        }
        return false;
    }

    // <getters and setters>

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {

        this.linkType = linkType;
    }

    public String getLinkedText() {
        return linkedText;
    }

    public void setLinkedText(String linkedText) {
        this.linkedText = linkedText;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
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

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    // </getters and setters>
}
