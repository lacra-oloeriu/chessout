package eu.chessout.shared.model;

import eu.chessout.shared.model.enums.PlayerType;

import java.text.Collator;
import java.util.Objects;

/**
 * Created by Bogdan Oloeriu on 6/4/2016.
 */
public class Player implements Comparable<Player> {
    private String name;
    private String email;
    private int elo;
    private int clubElo;

    private String userKey;
    private String playerKey;
    private String clubKey;

    /**
     * thirdPartyKey was introduced in order to handel unique id's from an external swar tournament import
     */
    private String thirdPartyKey;
    private String profilePictureUri;
    private String fideId;

    private String multiversXAddress;
    private PlayerType playerType;
    private String externalClubKey;
    private String externalPlayerKey;


    private boolean archived;


    public Player() {
    }

    public Player(String name, String email, String clubKey, int elo, int clubElo) {
        this.name = name;
        this.email = email;
        this.clubKey = clubKey;
        this.elo = elo;
        this.clubElo = clubElo;
    }

    @Override
    public int compareTo(Player another) {
        Collator defaultCollator = Collator.getInstance();
        return defaultCollator.compare(this.getName(), another.getName());
    }

    // <getters and setters>
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClubElo() {
        return clubElo;
    }

    public void setClubElo(int clubElo) {
        this.clubElo = clubElo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getPlayerKey() {
        return playerKey;
    }

    public void setPlayerKey(String playerKey) {
        this.playerKey = playerKey;
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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getThirdPartyKey() {
        return thirdPartyKey;
    }

    public String getFideId() {
        return fideId;
    }

    public void setFideId(String fideId) {
        this.fideId = fideId;
    }

    public void setThirdPartyKey(String thirdPartyKey) {
        this.thirdPartyKey = thirdPartyKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return playerKey.equals(player.playerKey) &&
                clubKey.equals(player.clubKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerKey, clubKey);
    }

    public String getMultiversXAddress() {
        return multiversXAddress;
    }

    public void setMultiversXAddress(String multiversXAddress) {
        this.multiversXAddress = multiversXAddress;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public String getExternalClubKey() {
        return externalClubKey;
    }

    public void setExternalClubKey(String externalClubKey) {
        this.externalClubKey = externalClubKey;
    }

    public String getExternalPlayerKey() {
        return externalPlayerKey;
    }

    public void setExternalPlayerKey(String externalPlayerKey) {
        this.externalPlayerKey = externalPlayerKey;
    }

    // </getters and setters>

}
