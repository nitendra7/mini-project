package com.chat.app.service;

import com.chat.app.entity.Message;
import com.chat.app.entity.MessageStatus;
import com.chat.app.entity.MessageType;
import com.chat.app.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public List<Message> getDmHistory(String user1, String user2) {
        return messageRepository.findBySenderClerkIdAndReceiverClerkIdOrReceiverClerkIdAndSenderClerkIdOrderByTimestampAsc(
                user1, user2, user1, user2
        );
    }

    public List<Message> getGroupMessages(String groupId) {
        return messageRepository.findByGroupIdOrderByTimestampAsc(groupId);
    }
}
