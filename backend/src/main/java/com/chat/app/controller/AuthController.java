package com.chat.app.controller;

import com.chat.app.dto.UserDto;
import com.chat.app.entity.User;
import com.chat.app.service.UserService;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chat.app.service.ClerkJwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final ClerkJwtService clerkJwtService;

    public AuthController(UserService userService, ClerkJwtService clerkJwtService) {
        this.userService = userService;
        this.clerkJwtService = clerkJwtService;
    }

    /**
     * Sync user data from Clerk JWT token
     * Called after Clerk authentication
     */
    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(@Valid @RequestBody UserDto userDto) {
        try {
            User user = userService.getOrCreateUser(userDto);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current authenticated user
     * Requires valid Clerk JWT token in Authorization header
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        try {
            String clerkUserId = (String) request.getAttribute("clerkUserId");
            if (clerkUserId == null) {
                return ResponseEntity.status(401).build();
            }
            User user = userService.findByClerkUserId(clerkUserId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update user profile
     * Requires valid Clerk JWT token in Authorization header
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @Valid @RequestBody UserDto userDto,
            HttpServletRequest request) {
        try {
            String clerkUserId = (String) request.getAttribute("clerkUserId");
            if (clerkUserId == null) {
                return ResponseEntity.status(401).build();
            }
            User user = userService.updateUser(clerkUserId, userDto);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }

    /**
     * Check if username is available
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username, HttpServletRequest request) {
        try {
            String clerkUserId = (String) request.getAttribute("clerkUserId");
            if (clerkUserId == null) {
                return ResponseEntity.status(401).build();
            }
            boolean available = userService.isUsernameAvailable(username);
            return ResponseEntity.ok(java.util.Collections.singletonMap("available", available));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
