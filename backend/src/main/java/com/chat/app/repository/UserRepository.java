package com.chat.app.repository;

import com.chat.app.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByClerkUserId(String clerkUserId);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByClerkUserId(String clerkUserId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByClerkUserIdIn(List<String> clerkUserIds);
}
