package com.example.ai_ride.Models;

import java.util.List;

public class User {
    private String name;
    private String email;
    private String userImage;

    private String state;
    private String zipCode;
    private String phoneNumber;
    private String password;
    private List<String> preferences;

    public User() {
        // Default constructor required for Firebase
    }


    public User(String name, String email, String state, String zipCode, String userImage, String phoneNumber, String password, List<String> preferences) {
        this.name = name;
        this.email = email;
        this.state = state;
        this.zipCode = zipCode;
        this.userImage = userImage;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.preferences = preferences;

    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }
    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

}
