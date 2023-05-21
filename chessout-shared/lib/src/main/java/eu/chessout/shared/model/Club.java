package eu.chessout.shared.model;

import java.text.Collator;
import java.util.HashMap;
import java.util.Objects;

import eu.chessout.shared.Constants;

/**
 * Created by Bogdan Oloeriu on 5/25/2016.
 */
public class Club implements Comparable<Club> {
    private String clubId;
    private String name;
    private String shortName;
    private Picture picture;
    private String email;
    private String country;
    private String city;
    private String homePage;
    private String description;
    private HashMap<String, Object> dateCreated;
    private HashMap<String, Object> updateStamp;

    public Club() {

    }

    public Club(String name, String shortName, String email, String country, String city, String homePage, String description) {
        HashMap<String, Object> timeStamp = new HashMap<>();
        timeStamp.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        this.name = name;
        this.shortName = shortName;
        this.email = email;
        this.country = country;
        this.city = city;
        this.homePage = homePage;
        this.description = description;
        this.dateCreated = timeStamp;
        this.updateStamp = timeStamp;
    }

    public long dateCreatedGetLong() {
        return (long) dateCreated.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    public String valMapKey() {
        return this.getName() + dateCreatedGetLong();
    }

    @Override
    public int compareTo(Club another) {
        Collator defaultCollator = Collator.getInstance();
        return defaultCollator.compare(this.getName(), another.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Club club = (Club) o;
        return clubId.equals(club.clubId) &&
                Objects.equals(name, club.name) &&
                Objects.equals(shortName, club.shortName) &&
                Objects.equals(picture, club.picture) &&
                Objects.equals(email, club.email) &&
                Objects.equals(country, club.country) &&
                Objects.equals(city, club.city) &&
                Objects.equals(homePage, club.homePage) &&
                Objects.equals(description, club.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clubId, name, shortName, picture, email, country, city, homePage, description);
    }

    // <getters and setters>

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public Picture getPicture() {
        return picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }

    // </getters and setters>
}
