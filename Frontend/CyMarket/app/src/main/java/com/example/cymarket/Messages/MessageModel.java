package com.example.cymarket.Messages;

/**
 * Represents a single chat message.
 * <p>
 * Contains the message text and a flag indicating whether
 * it was sent by the current user.
 *
 * @author Tyler Mestery
 */
public class MessageModel {
    private String message;
    private boolean sentByMe;

    /**
     * Constructs a new MessageModel.
     *
     * @param message   The content of the message.
     * @param sentByMe  True if the message was sent by the current user, false otherwise.
     */
    public MessageModel(String message, boolean sentByMe) {
        this.message = message;
        this.sentByMe = sentByMe;
    }

    /**
     * Returns the text of the message.
     *
     * @return The message content.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Checks if the message was sent by the current user.
     *
     * @return True if sent by the current user, false otherwise.
     */
    public boolean isSentByMe() {
        return sentByMe;
    }

    public boolean isMine() {
        return isSentByMe();
    }
}