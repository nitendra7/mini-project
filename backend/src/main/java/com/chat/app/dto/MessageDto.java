package com.chat.app.dto;

import com.chat.app.entity.MessageStatus;
import com.chat.app.entity.MessageType;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public class MessageDto {

    private String id;
    private String senderClerkId;
    private String receiverClerkId;
    private String groupId;
    private MessageType type;
    private String content;
    private Date timestamp;
    private MessageStatus status;

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
