package com.example.groupcycling.NotificationPack;

public class NotificationSender {
    public Data data;
    public String to;

    public NotificationSender(Data data, String receiver) {
        this.data = data;
        this.to = receiver;
    }

    public NotificationSender() {
    }
}
