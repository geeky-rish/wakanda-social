package com.microblog.controller;

import com.microblog.dto.ConversationDTO;
import com.microblog.dto.DirectMessageDTO;
import com.microblog.security.UserPrincipal;
import com.microblog.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /messages/conversations - Getting conversations for user: {}", currentUser.getId());
        List<ConversationDTO> conversations = directMessageService.getConversations(currentUser.getId());
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<List<DirectMessageDTO>> getConversation(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /messages/conversation/{} - Getting conversation with user", userId);
        List<DirectMessageDTO> messages = directMessageService.getConversation(currentUser.getId(), userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<DirectMessageDTO> sendMessage(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("POST /messages/send - Sending message from user: {}", currentUser.getId());

        Long receiverId = Long.valueOf(request.get("receiverId").toString());
        String content = request.get("content").toString();

        DirectMessageDTO message = directMessageService.sendMessage(currentUser.getId(), receiverId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PutMapping("/read/{userId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("PUT /messages/read/{} - Marking messages as read", userId);
        directMessageService.markMessagesAsRead(userId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /messages/unread-count - Getting unread count for user: {}", currentUser.getId());
        Long count = directMessageService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
