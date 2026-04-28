package com.chat.app.controller;

import com.chat.app.entity.Group;
import com.chat.app.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestParam String name,
                                             @RequestParam(required = false) String description,
                                             HttpServletRequest request) {
        try {
            String clerkUserId = (String) request.getAttribute("clerkUserId");
            Group result = groupService.createGroup(name, description, clerkUserId, null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Group>> getUserGroups(HttpServletRequest request) {
        String clerkUserId = (String) request.getAttribute("clerkUserId");
        return ResponseEntity.ok(groupService.getUserGroups(clerkUserId));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Group> addMember(@PathVariable String groupId,
                                            @RequestParam String newMemberClerkId,
                                            HttpServletRequest request) {
        try {
            String clerkUserId = (String) request.getAttribute("clerkUserId");
            Group result = groupService.addMember(groupId, clerkUserId, newMemberClerkId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
