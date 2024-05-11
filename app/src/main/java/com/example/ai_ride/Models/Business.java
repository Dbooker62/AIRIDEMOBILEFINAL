package com.example.ai_ride.Models;

import java.util.List;

public class Business {
    private String ownerName;

    private String businessImage;
    private String businessName;
    private String ownerEmail;
    private String ownerState;
    private String ownerZipCode;
    private String ownerLocation;
    private String ownerPhoneNumber;
    private String password;

    public Business() {
        // Default constructor required for Firebase
    }

    public Business(String ownerName, String businessName, String ownerEmail, String ownerState,
                    String ownerZipCode, String ownerLocation, String ownerPhoneNumber, String password, String businessImage) {
        this.ownerName = ownerName;
        this.businessName = businessName;
        this.ownerEmail = ownerEmail;
        this.ownerState = ownerState;
        this.ownerZipCode = ownerZipCode;
        this.ownerLocation = ownerLocation;
        this.ownerPhoneNumber = ownerPhoneNumber;
        this.businessImage = businessImage;
        this.password = password;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessImage() {
        return businessImage;
    }

    public String setBusinessImage() {
        return businessImage;
    }


    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getOwnerState() {
        return ownerState;
    }

    public String getOwnerZipCode() {
        return ownerZipCode;
    }

    public String getOwnerLocation() {
        return ownerLocation;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setOwnerState(String ownerState) {
        this.ownerState = ownerState;
    }

    public void setOwnerPhoneNumber(String ownerPhoneNumber) {
        this.ownerPhoneNumber = ownerPhoneNumber;
    }

    public void setOwnerLocation(String ownerLocation) {
        this.ownerLocation = ownerLocation;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    public void setOwnerZipCode(String ownerZipCode) {
        this.ownerZipCode = ownerZipCode;
    }
}
