package com.example.cymarket.Messages;

/**
 * Represents a single chat message within a group conversation.
 * <p>
 * Each message stores the sender's username and the message content,
 * and provides a helper method to determine message ownership.
 *
 * @author Tyler Mestery
 */
public class Message {
    private String sender;
    private String content;

    /**
     * Constructs a new {@code Message}.
     *
     * @param sender  username of the sender
     * @param content message text
     */
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    /**
     * Returns the sender's username.
     *
     * @return sender username
     */
    public String getSender() {
        return sender;
    }

    /**
     * Returns the message content.
     *
     * @return message text
     */
    public String getContent() {
        return content;
    }

    /**
     * Determines whether this message was sent by the current user.
     *
     * @param currentUsername username of the current user
     * @return {@code true} if the message was sent by the current user,
     *         {@code false} otherwise
     */
    public boolean isSentByMe(String currentUsername) {
        return sender.equals(currentUsername);
    }
}