package eu.chessout.shared.model;

import java.util.Objects;

public class Picture {
    private String pictureId;
    private String stringUri;
    private String location;
    private boolean uploadComplete;

    public Picture() {
        // default public constructor
    }

    public Picture(String pictureId, String stringUri) {
        this.pictureId = pictureId;
        this.stringUri = stringUri;
    }

    public Picture(String pictureId, String stringUri, String location) {
        this.pictureId = pictureId;
        this.stringUri = stringUri;
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Picture picture = (Picture) o;
        return uploadComplete == picture.uploadComplete &&
                Objects.equals(pictureId, picture.pictureId) &&
                Objects.equals(stringUri, picture.stringUri) &&
                Objects.equals(location, picture.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pictureId, stringUri, location, uploadComplete);
    }

    // <getters and setters>

    public String getPictureId() {
        return pictureId;
    }

    public void setPictureId(String pictureId) {
        this.pictureId = pictureId;
    }

    public String getStringUri() {
        return stringUri;
    }

    public void setStringUri(String stringUri) {
        this.stringUri = stringUri;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isUploadComplete() {
        return uploadComplete;
    }

    public void setUploadComplete(boolean uploadComplete) {
        this.uploadComplete = uploadComplete;
    }

    // </getters and setters>
}
