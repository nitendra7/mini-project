package com.chat.app.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "messages")
public class Message {

    @Id
    private String id;
    private String senderClerkId;
    private String receiverClerkId;
    private String groupId;
    private MessageType type;
    private String content;
    private Date timestamp;
    private MessageStatus status;

    public Message() {
    }

    public Message(String senderClerkId, String receiverClerkId, String groupId, MessageType type, String content, Date timestamp, MessageStatus status) {
        this.senderClerkId = senderClerkId;
        this.receiverClerkId = receiverClerkId;
        this.groupId = groupId;
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderClerkId() {
        return senderClerkId;
    }

    public void setSenderClerkId(String senderClerkId) {
        this.senderClerkId = senderClerkId;
    }

    public String getReceiverClerkId() {
        return receiverClerkId;
    }

    public void setReceiverClerkId(String receiverClerkId) {
        this.receiverClerkId = receiverClerkId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
