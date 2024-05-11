package com.example.ai_ride.Models;

public class UserOfferModel {
    private String title;
    private String description;
    private String category;
    private String imageURL;
    private double latitude;
    private double longitude;


    public UserOfferModel() {
    }

    public UserOfferModel(String title, String description, String category, String imageURL, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageURL = imageURL;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImageURL() {
        return imageURL;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
