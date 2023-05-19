package eu.chessout.shared.model;

import java.util.HashMap;

import eu.chessout.shared.Constants;

public class Device {
    private String deviceKey;
    private String deviceType;
    private HashMap<String, Object> dateCreated;

    public enum DeviceType {ANDROID}

    public Device() {
    }

    public Device(String deviceKey, DeviceType deviceType) {
        HashMap<String, Object> timeStamp = new HashMap<>();
        timeStamp.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        this.deviceKey = deviceKey;
        this.deviceType = String.valueOf(deviceType);
        this.dateCreated = timeStamp;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public HashMap<String, Object> getDateCreated() {
        return dateCreated;
    }


}
