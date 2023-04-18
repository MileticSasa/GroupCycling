package com.example.groupcycling.model;

import com.google.firebase.firestore.GeoPoint;

public class ModelUser {

    private Double distance, topSpeed;
    private String email, image, name, onlineStatus, uid;
    private GeoPoint latLng;

    public ModelUser() {
    }

    public ModelUser(Double distance, String email, String image, String name,
                     String onlineStatus, Double topSpeed, String uid, GeoPoint latLng) {
        this.distance = distance;
        this.email = email;
        this.image = image;
        this.name = name;
        this.onlineStatus = onlineStatus;
        this.topSpeed = topSpeed;
        this.uid = uid;
        this.latLng = latLng;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public Double getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(Double topSpeed) {
        this.topSpeed = topSpeed;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public GeoPoint getLatLng() {
        return latLng;
    }

    public void setLatLng(GeoPoint latLng) {
        this.latLng = latLng;
    }
}
