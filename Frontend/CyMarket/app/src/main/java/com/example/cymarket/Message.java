package com.example.cymarket;

public class Message {
    private String sender;
    private String content;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public boolean isSentByMe(String currentUsername) {
        return sender.equals(currentUsername);
    }
}

//public class MessageModel {
//    private String message;
//    private boolean sentByMe;
//
//    public MessageModel(String message, boolean sentByMe) {
//        this.message = message;
//        this.sentByMe = sentByMe;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public boolean isSentByMe() {
//        return sentByMe;
//    }
//
//    public boolean isMine() {
//        return isSentByMe();
//    }
//}