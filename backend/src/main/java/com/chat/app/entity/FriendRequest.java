package com.chat.app.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "friend_requests")
public class FriendRequest {
    @Id
    private String id;
    private String senderClerkId;
    private String receiverClerkId;
    private FriendRequestStatus status = FriendRequestStatus.PENDING;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();

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

    public FriendRequestStatus getStatus() {
        return status;
    }

    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
