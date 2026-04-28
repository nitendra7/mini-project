package com.chat.app.service;

import com.chat.app.entity.FriendRequest;
import com.chat.app.entity.FriendRequestStatus;
import com.chat.app.entity.User;
import com.chat.app.repository.FriendRequestRepository;
import com.chat.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendService {
    @Autowired
    private FriendRequestRepository friendRequestRepository;
    @Autowired
    private UserRepository userRepository;

    public FriendRequest sendRequest(String senderClerkId, String receiverUsername) throws Exception {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new Exception("User not found"));
        if (senderClerkId.equals(receiver.getClerkUserId()))
            throw new Exception("Cannot friend yourself");

        User sender = userRepository.findByClerkUserId(senderClerkId)
                .orElseThrow(() -> new Exception("Sender not found"));
        if (sender.getFriendIds().contains(receiver.getClerkUserId()))
            throw new Exception("Already friends");

        FriendRequest request = new FriendRequest();
        request.setSenderClerkId(senderClerkId);
        request.setReceiverClerkId(receiver.getClerkUserId());
        return friendRequestRepository.save(request);
    }

    public FriendRequest acceptRequest(String requestId, String currentUserClerkId) throws Exception {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new Exception("Request not found"));
        if (!request.getReceiverClerkId().equals(currentUserClerkId))
            throw new Exception("Unauthorized");

        request.setStatus(FriendRequestStatus.ACCEPTED);
        User sender = userRepository.findByClerkUserId(request.getSenderClerkId())
                .orElseThrow(() -> new Exception("Sender not found"));
        User receiver = userRepository.findByClerkUserId(currentUserClerkId)
                .orElseThrow(() -> new Exception("Receiver not found"));

        sender.getFriendIds().add(currentUserClerkId);
        receiver.getFriendIds().add(request.getSenderClerkId());

        userRepository.save(sender);
        userRepository.save(receiver);
        return friendRequestRepository.save(request);
    }

    public List<FriendRequest> getPendingRequests(String clerkUserId) {
        return friendRequestRepository.findByReceiverClerkIdAndStatus(clerkUserId, FriendRequestStatus.PENDING);
    }

    public List<User> getFriends(String clerkUserId) {
        User user = userRepository.findByClerkUserId(clerkUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userRepository.findByClerkUserIdIn(user.getFriendIds());
    }
}
