package com.example.ai_ride.Models;

import java.io.Serializable;

public class AddOfferModel implements Serializable {
    private String title;
    private String description;
    private String category;
    private String imageURL;
    private double latitude;
    private double longitude;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id;


    public AddOfferModel() {
        // Default constructor required for Firebase
    }

    public AddOfferModel(String title, String description, String category, String imageURL, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageURL = imageURL;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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

