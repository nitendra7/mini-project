    package com.chat.app.controller;

    import java.util.Date;
    import java.util.List;

    import org.springframework.http.ResponseEntity;
    import org.springframework.messaging.handler.annotation.MessageMapping;
    import org.springframework.messaging.handler.annotation.Payload;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    import com.chat.app.dto.MessageDto;
    import com.chat.app.entity.Message;
    import com.chat.app.entity.MessageStatus;
    import com.chat.app.entity.MessageType;
    import com.chat.app.repository.GroupRepository;
    import com.chat.app.service.MessageService;

    import jakarta.servlet.http.HttpServletRequest;

    @RestController
    @RequestMapping("/api")
    public class ChatController {

        private final SimpMessagingTemplate messagingTemplate;
        private final MessageService messageService;
        private final GroupRepository groupRepository;

        public ChatController(SimpMessagingTemplate messagingTemplate, MessageService messageService, GroupRepository groupRepository) {
            this.messagingTemplate = messagingTemplate;
            this.messageService = messageService;
            this.groupRepository = groupRepository;
        }

        /**
         * Handle incoming DM messages via STOMP WebSocket
         * Client sends senderClerkId in the message body
         */
        @MessageMapping("/chat")
        public void processMessage(@Payload MessageDto messageDto) {
            Message message = new Message();
            message.setSenderClerkId(messageDto.getSenderClerkId());
            message.setReceiverClerkId(messageDto.getReceiverClerkId());
            message.setType(MessageType.DM);
            message.setContent(messageDto.getContent());
            message.setTimestamp(new Date());
            message.setStatus(MessageStatus.SENT);

            Message savedMsg = messageService.saveMessage(message);
            messagingTemplate.convertAndSendToUser(
                    messageDto.getReceiverClerkId(), "/queue/messages", savedMsg
            );
        }

        /**
         * Handle incoming group messages via STOMP WebSocket
         */
        @MessageMapping("/group-chat")
        public void processGroupMessage(@Payload MessageDto messageDto) {
            Message message = new Message();
            message.setSenderClerkId(messageDto.getSenderClerkId());
            message.setGroupId(messageDto.getGroupId());
            message.setType(MessageType.GROUP);
            message.setContent(messageDto.getContent());
            message.setTimestamp(new Date());
            message.setStatus(MessageStatus.SENT);

            Message savedMsg = messageService.saveMessage(message);
            messagingTemplate.convertAndSend("/topic/group/" + messageDto.getGroupId(), savedMsg);
        }

        /**
         * Get DM history between two users
         */
        @GetMapping("/messages/dm/{user1}/{user2}")
        public ResponseEntity<List<Message>> getDmHistory(
                @PathVariable String user1,
                @PathVariable String user2,
                HttpServletRequest request) {

            String clerkUserId = (String) request.getAttribute("clerkUserId");
            if (clerkUserId == null || (!clerkUserId.equals(user1) && !clerkUserId.equals(user2))) {
                return ResponseEntity.status(401).build();
            }

            return ResponseEntity.ok(messageService.getDmHistory(user1, user2));
        }

        /**
         * Get group message history
         */
        @GetMapping("/messages/group/{groupId}")
        public ResponseEntity<List<Message>> getGroupHistory(
                @PathVariable String groupId,
                HttpServletRequest request) {

            String clerkUserId = (String) request.getAttribute("clerkUserId");
            if (clerkUserId == null || !groupRepository.existsByIdAndMemberClerkIdsContaining(groupId, clerkUserId)) {
                return ResponseEntity.status(401).build();
            }

            return ResponseEntity.ok(messageService.getGroupMessages(groupId));
        }
    }
