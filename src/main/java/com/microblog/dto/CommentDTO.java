package com.microblog.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private LocalDateTime timestamp;
    private Long userId;
}
