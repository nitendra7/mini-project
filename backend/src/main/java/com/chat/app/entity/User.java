package com.chat.app.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String clerkUserId;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    @Indexed(unique = true)
    private String username;
    private List<String> friendIds = new ArrayList<>();
    private Date createdAt;
    private Date updatedAt;

    public User() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.friendIds = new ArrayList<>();
    }

    public User(String clerkUserId, String email, String firstName, String lastName) {
        this.clerkUserId = clerkUserId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.friendIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClerkUserId() {
        return clerkUserId;
    }

    public void setClerkUserId(String clerkUserId) {
        this.clerkUserId = clerkUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }
}
