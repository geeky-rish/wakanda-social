package com.microblog.service.impl;

import com.microblog.dto.ConversationDTO;
import com.microblog.dto.DirectMessageDTO;
import com.microblog.entity.DirectMessage;
import com.microblog.entity.User;
import com.microblog.repository.DirectMessageRepository;
import com.microblog.service.DirectMessageService;
import com.microblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserService userService;

    @Override
    public DirectMessageDTO sendMessage(Long senderId, Long receiverId, String content) {
        log.info("Sending message from user {} to user {}", senderId, receiverId);

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty");
        }

        if (senderId.equals(receiverId)) {
            throw new RuntimeException("You cannot send a message to yourself");
        }

        User sender = userService.getUserEntityById(senderId);
        User receiver = userService.getUserEntityById(receiverId);

        // Update sender's last active time
        sender.setLastActive(LocalDateTime.now());

        DirectMessage message = new DirectMessage();
        message.setContent(content.trim());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());
        message.setIsRead(false);

        DirectMessage savedMessage = directMessageRepository.save(message);
        return convertToDTO(savedMessage, senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectMessageDTO> getConversation(Long userId1, Long userId2) {
        log.info("Getting conversation between users {} and {}", userId1, userId2);

        List<DirectMessage> messages = directMessageRepository.findConversationBetweenUsers(userId1, userId2);
        return messages.stream()
                .map(message -> convertToDTO(message, userId1))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversations(Long userId) {
        log.info("Getting conversations for user {}", userId);

        try {
            List<User> partners = directMessageRepository.findConversationPartners(userId);
            List<ConversationDTO> conversations = new ArrayList<>();

            for (User partner : partners) {
                DirectMessage lastMessage = directMessageRepository.findLastMessageBetweenUsers(userId, partner.getId());

                if (lastMessage != null) {
                    ConversationDTO conversation = new ConversationDTO();
                    conversation.setUserId(partner.getId());
                    conversation.setUsername(partner.getUsername());
                    conversation.setDisplayName(partner.getDisplayName() != null ? partner.getDisplayName() : partner.getUsername());
                    conversation.setProfileImageUrl(partner.getProfileImageUrl());
                    conversation.setLastMessage(lastMessage.getContent());
                    conversation.setLastMessageTime(lastMessage.getTimestamp());

                    // Count unread messages from this partner
                    long unreadCount = directMessageRepository.findConversationBetweenUsers(userId, partner.getId())
                            .stream()
                            .filter(msg -> msg.getReceiver().getId().equals(userId) && !msg.getIsRead())
                            .count();

                    conversation.setUnreadCount((int) unreadCount);

                    // Check if user is online (active within last 10 minutes)
                    conversation.setIsOnline(partner.isOnline());

                    conversations.add(conversation);
                }
            }

            // Sort by last message time (most recent first)
            conversations.sort((c1, c2) -> c2.getLastMessageTime().compareTo(c1.getLastMessageTime()));

            return conversations;

        } catch (Exception e) {
            log.error("Error getting conversations for user {}", userId, e);
            throw new RuntimeException("Failed to load conversations");
        }
    }

    @Override
    public void markMessagesAsRead(Long senderId, Long receiverId) {
        log.info("Marking messages as read from user {} to user {}", senderId, receiverId);

        List<DirectMessage> unreadMessages = directMessageRepository.findConversationBetweenUsers(senderId, receiverId)
                .stream()
                .filter(msg -> msg.getReceiver().getId().equals(receiverId) && !msg.getIsRead())
                .collect(Collectors.toList());

        for (DirectMessage message : unreadMessages) {
            message.setIsRead(true);
            directMessageRepository.save(message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return directMessageRepository.countUnreadMessages(userId);
    }

    private DirectMessageDTO convertToDTO(DirectMessage message, Long currentUserId) {
        DirectMessageDTO dto = new DirectMessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setIsRead(message.getIsRead());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setSenderDisplayName(message.getSender().getDisplayName() != null ? message.getSender().getDisplayName() : message.getSender().getUsername());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverUsername(message.getReceiver().getUsername());
        dto.setReceiverDisplayName(message.getReceiver().getDisplayName() != null ? message.getReceiver().getDisplayName() : message.getReceiver().getUsername());
        dto.setIsSentByCurrentUser(message.getSender().getId().equals(currentUserId));
        return dto;
    }
}
