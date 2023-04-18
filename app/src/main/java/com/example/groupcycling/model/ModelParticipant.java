package com.example.groupcycling.model;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public class ModelParticipant implements Serializable {

    private String uid, name, image;
    private double speed;
    private GeoPoint latlng;

    public ModelParticipant(){
    }

    public ModelParticipant(String uid, String name, String image, double speed, GeoPoint latlng) {
        this.uid = uid;
        this.name = name;
        this.image = image;
        this.speed = speed;
        this.latlng = latlng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public GeoPoint getLatlng() {
        return latlng;
    }

    public void setLatlng(GeoPoint latlng) {
        this.latlng = latlng;
    }

}
