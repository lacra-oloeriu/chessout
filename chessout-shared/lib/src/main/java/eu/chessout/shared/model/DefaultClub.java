package eu.chessout.shared.model;

/**
 * Created by Bogdan Oloeriu on 5/31/2016.
 */
public class DefaultClub {
    private String clubKey;
    private String clubName;

    public DefaultClub(){}
    public DefaultClub(String clubKey, String clubName){
        this.clubKey = clubKey;
        this.clubName = clubName;
    }

    public String getClubKey() {
        return clubKey;
    }

    public void setClubKey(String clubKey) {
        this.clubKey = clubKey;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }
}
