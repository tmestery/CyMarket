package com.example.cymarket.LoginSignup;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a user in the system.
 * <p>
 * Stores basic user information such as ID, username, bio, and profile image URL.
 * </p>
 *
 * @author Tyler Mestery
 */
public class User {

    private int id;
    private String username;
    private String bio;

    @SerializedName("profileImageUrl")
    private String profileImageUrl; // frontend-only field to store image URL

    /**
     * Returns the user's ID.
     *
     * @return user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user's ID.
     *
     * @param id user ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the user's username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's username.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the URL of the user's profile image.
     *
     * @return profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the URL of the user's profile image.
     *
     * @param profileImageUrl profile image URL
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}