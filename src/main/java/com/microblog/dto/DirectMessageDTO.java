package com.microblog.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectMessageDTO {
    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private Boolean isRead;
    private Long senderId;
    private String senderUsername;
    private String senderDisplayName;
    private Long receiverId;
    private String receiverUsername;
    private String receiverDisplayName;
    private Boolean isSentByCurrentUser;
}
