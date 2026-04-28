package com.chat.app.repository;

import com.chat.app.entity.FriendRequest;
import com.chat.app.entity.FriendRequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
    List<FriendRequest> findByReceiverClerkIdAndStatus(String receiverClerkId, FriendRequestStatus status);
    List<FriendRequest> findBySenderClerkIdAndReceiverClerkId(String senderClerkId, String receiverClerkId);
}
