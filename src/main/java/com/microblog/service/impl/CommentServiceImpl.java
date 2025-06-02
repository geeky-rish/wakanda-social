package com.microblog.service.impl;

import com.microblog.dto.CommentDTO;
import com.microblog.entity.Comment;
import com.microblog.entity.Post;
import com.microblog.entity.User;
import com.microblog.repository.CommentRepository;
import com.microblog.repository.PostRepository;
import com.microblog.service.CommentService;
import com.microblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    
    @Override
    public CommentDTO createComment(Long postId, String content, Long userId) {
        log.info("Creating comment for post {} by user {}", postId, userId);
        
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Comment content cannot be empty");
        }
        
        User user = userService.getUserEntityById(userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);
        comment.setTimestamp(LocalDateTime.now());
        
        Comment savedComment = commentRepository.save(comment);
        
        // Update post's comment count
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        
        return convertToDTO(savedComment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByTimestampDesc(postId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteComment(Long commentId, Long userId) {
        log.info("Deleting comment {} by user {}", commentId, userId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
        
        // Update post's comment count
        Post post = comment.getPost();
        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.save(post);
    }
    
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setUsername(comment.getUser().getUsername());
        dto.setDisplayName(comment.getUser().getDisplayName());
        dto.setProfileImageUrl(comment.getUser().getProfileImageUrl());
        dto.setTimestamp(comment.getTimestamp());
        dto.setUserId(comment.getUser().getId());
        return dto;
    }
}
