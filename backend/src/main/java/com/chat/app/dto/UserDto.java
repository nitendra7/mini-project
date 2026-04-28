package com.chat.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserDto {

    @NotBlank(message = "Clerk User ID cannot be blank")
    private String clerkUserId;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    private String firstName;
    private String lastName;
    private String profileImageUrl;

    public UserDto() {
    }

    public UserDto(String clerkUserId, String email, String firstName, String lastName) {
        this.clerkUserId = clerkUserId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
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

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
