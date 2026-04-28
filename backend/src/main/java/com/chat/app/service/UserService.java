package com.chat.app.service;

import com.chat.app.dto.UserDto;
import com.chat.app.entity.User;
import com.chat.app.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get or create a user based on Clerk authentication
     * Called when a Clerk-authenticated request comes in
     */
    public User getOrCreateUser(UserDto userDto) {
        return userRepository.findByClerkUserId(userDto.getClerkUserId())
                .orElseGet(() -> {
                    User newUser = new User(
                            userDto.getClerkUserId(),
                            userDto.getEmail(),
                            userDto.getFirstName(),
                            userDto.getLastName()
                    );
                    if (userDto.getProfileImageUrl() != null) {
                        newUser.setProfileImageUrl(userDto.getProfileImageUrl());
                    }
                    return userRepository.save(newUser);
                });
    }

    /**
     * Find user by Clerk user ID
     */
    public User findByClerkUserId(String clerkUserId) {
        return userRepository.findByClerkUserId(clerkUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Update user profile
     */
    public User updateUser(String clerkUserId, UserDto userDto) {
        User user = findByClerkUserId(clerkUserId);
        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getProfileImageUrl() != null) {
            user.setProfileImageUrl(userDto.getProfileImageUrl());
        }
        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
        return userRepository.save(user);
    }

    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
}
