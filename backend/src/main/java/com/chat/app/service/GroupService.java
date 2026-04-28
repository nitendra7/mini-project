package com.chat.app.service;

import com.chat.app.entity.Group;
import com.chat.app.repository.GroupRepository;
import com.chat.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;

    public Group createGroup(String name, String description, String adminClerkId, List<String> initialMembers) {
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setAdminClerkId(adminClerkId);

        List<String> members = new ArrayList<>();
        members.add(adminClerkId);
        if (initialMembers != null) {
            for (String member : initialMembers) {
                if (!members.contains(member))
                    members.add(member);
            }
        }
        group.setMemberClerkIds(members);
        return groupRepository.save(group);
    }

    public List<Group> getUserGroups(String clerkUserId) {
        return groupRepository.findByMemberClerkIdsContaining(clerkUserId);
    }

    public Group addMember(String groupId, String adminClerkId, String newMemberClerkId) throws Exception {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));
        if (!group.getAdminClerkId().equals(adminClerkId))
            throw new Exception("Unauthorized");
        if (!group.getMemberClerkIds().contains(newMemberClerkId))
            group.getMemberClerkIds().add(newMemberClerkId);
        return groupRepository.save(group);
    }
}
