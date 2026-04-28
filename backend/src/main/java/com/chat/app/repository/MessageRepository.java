package com.chat.app.repository;

import com.chat.app.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findBySenderClerkIdAndReceiverClerkIdOrReceiverClerkIdAndSenderClerkIdOrderByTimestampAsc(
            String sender1, String receiver1, String receiver2, String sender2);
    List<Message> findByGroupIdOrderByTimestampAsc(String groupId);
}
