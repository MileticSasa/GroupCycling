package com.example.groupcycling.model;

public class ModelGroupMessage {

    private String message, sender, senderId, timestamp, type;

    public ModelGroupMessage() {

    }

    public ModelGroupMessage(String message, String sender, String senderId, String timestamp, String type) {
        this.message = message;
        this.sender = sender;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

}
