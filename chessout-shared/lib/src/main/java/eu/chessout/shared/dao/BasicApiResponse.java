package eu.chessout.shared.dao;

import eu.chessout.shared.model.MyPayLoad;

public class BasicApiResponse {
    public Boolean isSuccessful;
    public String message;
    public MyPayLoad myPayLoad;

    public BasicApiResponse() {
        // required by jackson
    }

    public BasicApiResponse(String message) {
        this.isSuccessful = true;
        this.message = message;
    }

    public static BasicApiResponse message(String message) {
        BasicApiResponse response = new BasicApiResponse(message);
        return response;
    }
}
