package com.chat.app.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "groups")
public class Group {
    @Id
    private String id;
    private String name;
    private String description;
    private String adminClerkId;
    private List<String> memberClerkIds = new ArrayList<>();
    private Date createdAt = new Date();
    private Date updatedAt = new Date();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdminClerkId() {
        return adminClerkId;
    }

    public void setAdminClerkId(String adminClerkId) {
        this.adminClerkId = adminClerkId;
    }

    public List<String> getMemberClerkIds() {
        return memberClerkIds;
    }

    public void setMemberClerkIds(List<String> memberClerkIds) {
        this.memberClerkIds = memberClerkIds;
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
