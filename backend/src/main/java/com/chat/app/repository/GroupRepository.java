package com.chat.app.repository;

import com.chat.app.entity.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    List<Group> findByMemberClerkIdsContaining(String clerkUserId);
    boolean existsByIdAndMemberClerkIdsContaining(String groupId, String clerkUserId);
}
