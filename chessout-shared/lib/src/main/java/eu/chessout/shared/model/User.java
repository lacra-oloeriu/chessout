package eu.chessout.shared.model;


import java.util.HashMap;

import eu.chessout.shared.Constants;


/**
 * Created by Bogdan Oloeriu on 5/24/2016.
 */
public class User {
    private String name;
    private String email;
    private String userKey;
    private HashMap<String, Object> dateCreated;
    private String pictureUri;

    public User() {
    }


    /**
     * Use this constructor to create new User.
     * Takes user name, email and timestampJoined as params
     *
     * @param name
     * @param email
     */
    public User(String name, String email) {
        HashMap<String, Object> timeStamp = new HashMap<>();
        timeStamp.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        this.name = name;
        this.email = email;
        this.dateCreated = timeStamp;
    }

    public User(String name, String email, String userKey) {
        HashMap<String, Object> timeStamp = new HashMap<>();
        timeStamp.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        this.name = name;
        this.email = email;
        this.dateCreated = timeStamp;
        this.userKey = userKey;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public HashMap<String, Object> getDateCreated() {
        return dateCreated;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}
