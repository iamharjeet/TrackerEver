package com.harjeet.trackerever.Structures;

public class NotificationStructure {
    String sender;
    String key;
    String message;

    NotificationStructure(){

    }
    public NotificationStructure(String sender,String message,String key) {
        this.sender = sender;
        this.key = key;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
