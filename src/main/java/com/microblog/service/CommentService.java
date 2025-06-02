package com.microblog.service;

import com.microblog.dto.CommentDTO;
import java.util.List;

public interface CommentService {
    CommentDTO createComment(Long postId, String content, Long userId);
    List<CommentDTO> getCommentsByPostId(Long postId);
    void deleteComment(Long commentId, Long userId);
}
