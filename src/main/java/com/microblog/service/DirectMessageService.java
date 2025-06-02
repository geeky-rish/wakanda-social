package com.microblog.service;

import com.microblog.dto.ConversationDTO;
import com.microblog.dto.DirectMessageDTO;
import java.util.List;

public interface DirectMessageService {
    DirectMessageDTO sendMessage(Long senderId, Long receiverId, String content);
    List<DirectMessageDTO> getConversation(Long userId1, Long userId2);
    List<ConversationDTO> getConversations(Long userId);
    void markMessagesAsRead(Long senderId, Long receiverId);
    Long getUnreadCount(Long userId);
}
