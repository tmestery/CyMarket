package com.example.cymarket.Messages;

/**
 * Represents a chat group with an identifier and a name.
 * <p>
 * Each Group object contains a unique ID and a human-readable name.
 * This class is typically used in messaging modules to display and manage
 * group chats.
 * </p>
 *
 * @author Tyler
 */
public class Group {
    private int id;
    private String name;

    /**
     * Constructs a new Group with the specified ID and name.
     *
     * @param id   the unique identifier for the group
     * @param name the name of the group
     */
    public Group(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the unique identifier of the group.
     *
     * @return the group ID as an integer
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the display name of the group.
     *
     * @return the group's name as a String
     */
    public String getName() {
        return name;
    }
}