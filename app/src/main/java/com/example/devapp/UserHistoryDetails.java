package com.example.devapp;

public class UserHistoryDetails {


    public UserHistoryDetails(String imageName, String imageUri, Object timestamp) {
        this.imageName = imageName;
        this.imageUri = imageUri;
        this.timestamp = timestamp;

    }


    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }


    private String imageName;
    private String imageUri;
    private Object timestamp;

    public UserHistoryDetails() {
    }

}
