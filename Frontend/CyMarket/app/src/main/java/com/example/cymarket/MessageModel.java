package com.example.cymarket;

public class ChatMessage {
    private String text;
    private boolean isMine;

    public ChatMessage(String text, boolean isMine) {
        this.text = text;
        this.isMine = isMine;
    }

    public String getText() {
        return text;
    }

    public boolean isMine() {
        return isMine;
    }
}
