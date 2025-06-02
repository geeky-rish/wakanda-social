package com.microblog.service.impl;

import com.microblog.dto.CommentDTO;
import com.microblog.dto.PostDTO;
import com.microblog.entity.Post;
import com.microblog.entity.User;
import com.microblog.exception.InvalidPostContentException;
import com.microblog.repository.CommentRepository;
import com.microblog.repository.PostRepository;
import com.microblog.service.LikeService;
import com.microblog.service.PostService;
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
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final LikeService likeService;

    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getAllPosts(Long currentUserId) {
        log.info("Fetching all posts for user: {}", currentUserId);
        return postRepository.findAllOrderByTimestampDesc()
                .stream()
                .map(post -> convertToDTO(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getUserFeed(Long userId) {
        log.info("Fetching feed for user: {}", userId);
        return postRepository.findUserFeed(userId)
                .stream()
                .map(post -> convertToDTO(post, userId))
                .collect(Collectors.toList());
    }

    @Override
    public PostDTO createPost(Long userId, String content) {
        log.info("Creating post for user id: {} with content: {}", userId, content);

        validatePostContent(content);

        User user = userService.getUserEntityById(userId);

        Post post = new Post();
        post.setContent(content);
        post.setUser(user);
        post.setTimestamp(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        // Update user's post count
        user.setPostsCount(user.getPostsCount() + 1);

        log.info("Post created successfully: {}", savedPost);

        return convertToDTO(savedPost, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getPostsByUserId(Long userId, Long currentUserId) {
        log.info("Fetching posts for user id: {}", userId);
        return postRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(post -> convertToDTO(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public void deletePost(Long postId, Long userId) {
        log.info("Deleting post {} by user {}", postId, userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(post);

        // Update user's post count
        User user = post.getUser();
        user.setPostsCount(Math.max(0, user.getPostsCount() - 1));
    }

    private void validatePostContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidPostContentException("Post content cannot be empty");
        }
        if (content.length() > 280) {
            throw new InvalidPostContentException("Post content cannot exceed 280 characters");
        }
    }

    @Override
    public PostDTO convertToDTO(Post post, Long currentUserId) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setUsername(post.getUser().getUsername());
        dto.setDisplayName(post.getUser().getDisplayName());
        dto.setProfileImageUrl(post.getUser().getProfileImageUrl());
        dto.setTimestamp(post.getTimestamp());
        dto.setUserId(post.getUser().getId());
        dto.setLikesCount(post.getLikesCount());
        dto.setCommentsCount(post.getCommentsCount());

        if (currentUserId != null) {
            dto.setIsLiked(likeService.isPostLikedByUser(post.getId(), currentUserId));
        } else {
            dto.setIsLiked(false);
        }

        // Get comments for the post
        List<CommentDTO> comments = commentRepository.findByPostIdOrderByTimestampDesc(post.getId())
                .stream()
                .map(this::convertCommentToDTO)
                .collect(Collectors.toList());
        dto.setComments(comments);

        return dto;
    }

    private CommentDTO convertCommentToDTO(com.microblog.entity.Comment comment) {
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
