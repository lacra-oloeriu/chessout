package eu.chessout.shared.model;

import eu.chessdata.chesspairing.model.ChesspairingPlayer;

/**
 * Created by Bogdan Oloeriu on 01/03/2018.
 */

public class RankedPlayer {
    private String tournamentKey;
    private String playerKey;
    private String clubKey;
    /**
     * used only for round standings. It represents the round rank
     */
    private int rankNumber;
    private int tournamentInitialOrder;
    private float points;
    private float buchholzPoints;
    private int elo;
    private String playerName;
    private String profilePictureUri;

    public RankedPlayer() {
        //default constructor
    }

    public RankedPlayer(
            ChesspairingPlayer player,
            String tournamentKey,
            String clubKey,
            String profilePictureUri) {
        this.tournamentKey = tournamentKey;
        this.playerKey = player.getPlayerKey();
        this.clubKey = clubKey;
        //this.rankNumber = player.getInitialOrderId();
        this.tournamentInitialOrder = player.getInitialOrderId();
        this.elo = player.getElo();
        this.playerName = player.getName();
        this.profilePictureUri = profilePictureUri;
    }

    public String getTournamentKey() {
        return tournamentKey;
    }

    public void setTournamentKey(String tournamentKey) {
        this.tournamentKey = tournamentKey;
    }

    public String getPlayerKey() {
        return playerKey;
    }

    public void setPlayerKey(String playerKey) {
        this.playerKey = playerKey;
    }

    public int getRankNumber() {
        return rankNumber;
    }

    public void setRankNumber(int rankNumber) {
        this.rankNumber = rankNumber;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getTournamentInitialOrder() {
        return tournamentInitialOrder;
    }

    public void setTournamentInitialOrder(int tournamentInitialOrder) {
        this.tournamentInitialOrder = tournamentInitialOrder;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public String getClubKey() {
        return clubKey;
    }

    public void setClubKey(String clubKey) {
        this.clubKey = clubKey;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
    }

    public float getBuchholzPoints() {
        return buchholzPoints;
    }

    public void setBuchholzPoints(float buchholzPoints) {
        this.buchholzPoints = buchholzPoints;
    }

    public float getPoints() {
        return points;
    }

    public void setPoints(float points) {
        this.points = points;
    }
}
