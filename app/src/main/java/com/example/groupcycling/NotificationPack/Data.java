package com.example.groupcycling.NotificationPack;

public class Data {
    private String title;
    private String body;

    public Data(String title, String message) {
        this.title = title;
        this.body = message;
    }

    public Data() {
    }

    public String getTite() {
        return title;
    }

    public void setTitle(String tite) {
        this.title = tite;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
