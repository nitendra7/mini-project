package com.chat.app.controller;

import com.chat.app.entity.FriendRequest;
import com.chat.app.entity.User;
import com.chat.app.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestParam String receiverUsername, HttpServletRequest request) {
        try {
            String clerkUserId = (String) request.getAttribute("clerkUserId");
            if (clerkUserId == null) {
                return ResponseEntity.status(401).body("Unauthorized: Invalid or missing token");
            }
            FriendRequest result = friendService.sendRequest(clerkUserId, receiverUsername);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<FriendRequest> acceptRequest(@PathVariable String requestId, HttpServletRequest request) {
        try {
            String clerkUserId = (String) request.getAttribute("clerkUserId");
            FriendRequest result = friendService.acceptRequest(requestId, clerkUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequest>> getPendingRequests(HttpServletRequest request) {
        String clerkUserId = (String) request.getAttribute("clerkUserId");
        return ResponseEntity.ok(friendService.getPendingRequests(clerkUserId));
    }

    @GetMapping
    public ResponseEntity<List<User>> getFriends(HttpServletRequest request) {
        String clerkUserId = (String) request.getAttribute("clerkUserId");
        return ResponseEntity.ok(friendService.getFriends(clerkUserId));
    }
}
