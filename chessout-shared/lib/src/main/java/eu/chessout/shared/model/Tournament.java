package eu.chessout.shared.model;

import java.util.HashMap;

import eu.chessout.shared.Constants;

/**
 * Created by Bogdan Oloeriu on 6/4/2016.
 */
public class Tournament {
    private String clubId;
    private String tournamentId;
    private String name;
    private String description;
    private String location;
    private int totalRounds;
    private int firstTableNumber = 1;
    //players
    private HashMap<String, Object> dateCreated;
    private HashMap<String, Object> updateStamp;
    private long reversedDateCreated;


    public Tournament() {
    }

    public Tournament(String name, String description, String location, int totalRounds, int firstTableNumber, String clubId, String tournamentId) {
        HashMap<String, Object> timeStamp = new HashMap<>();
        timeStamp.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        this.name = name;
        this.description = description;
        this.location = location;
        this.totalRounds = totalRounds;
        this.firstTableNumber = firstTableNumber;
        this.dateCreated = timeStamp;
        this.updateStamp = timeStamp;
        this.clubId = clubId;
        this.tournamentId = tournamentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public int getFirstTableNumber() {
        return firstTableNumber;
    }

    public HashMap<String, Object> getDateCreated() {
        return dateCreated;
    }

    public HashMap<String, Object> getUpdateStamp() {
        return updateStamp;
    }

    public long getReversedDateCreated() {
        return reversedDateCreated;
    }

    public void setReversedDateCreated(long reversedDateCreated) {
        this.reversedDateCreated = reversedDateCreated;
    }

    public long dateCreatedGetLong() {
        return (long) dateCreated.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }
}
