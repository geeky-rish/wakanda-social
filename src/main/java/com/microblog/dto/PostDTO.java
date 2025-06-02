package com.microblog.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    private Long id;
    private String content;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private LocalDateTime timestamp;
    private Long userId;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isLiked;
    private List<CommentDTO> comments;
}
