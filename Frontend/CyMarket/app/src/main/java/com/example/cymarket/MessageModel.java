package com.example.cymarket;

public class MessageModel {
    private String message;
    private boolean sentByMe;

    public MessageModel(String message, boolean sentByMe) {
        this.message = message;
        this.sentByMe = sentByMe;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }

    public boolean isMine() {
        return isSentByMe();
    }
}