package eu.chessout.shared.model;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Copied class from firebase decompiled class to add timestamp to data items in firebase
 */
public class ServerValue {
    public static final Map<String, String> TIMESTAMP = createServerValuePlaceholder("timestamp");

    public ServerValue() {
    }

    private static Map<String, String> createServerValuePlaceholder(String key) {
        Map<String, String> result = new HashMap();
        result.put(".sv", key);
        return Collections.unmodifiableMap(result);
    }
}