package com.example.cymarket;

import com.google.gson.annotations.SerializedName;

public class User {

    private int id;
    private String username;
    private String bio;

    @SerializedName("profileImageUrl")
    private String profileImageUrl; // frontend-only field to store image URL

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}