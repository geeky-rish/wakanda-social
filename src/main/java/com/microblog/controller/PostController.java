package com.microblog.controller;

import com.microblog.dto.CommentDTO;
import com.microblog.dto.CreatePostRequest;
import com.microblog.dto.PostDTO;
import com.microblog.security.UserPrincipal;
import com.microblog.service.CommentService;
import com.microblog.service.LikeService;
import com.microblog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PostController {
    
    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;
    
    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /posts - Fetching all posts");
        List<PostDTO> posts = postService.getAllPosts(currentUser.getId());
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/feed")
    public ResponseEntity<List<PostDTO>> getUserFeed(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /posts/feed - Fetching feed for user: {}", currentUser.getId());
        List<PostDTO> posts = postService.getUserFeed(currentUser.getId());
        return ResponseEntity.ok(posts);
    }
    
    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("POST /posts - Creating post for user {} with content: {}", currentUser.getId(), request.getContent());
        PostDTO post = postService.createPost(currentUser.getId(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("DELETE /posts/{} - Deleting post by user {}", id, currentUser.getId());
        postService.deletePost(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDTO>> getPostsByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /posts/user/{} - Fetching posts for user", userId);
        List<PostDTO> posts = postService.getPostsByUserId(userId, currentUser.getId());
        return ResponseEntity.ok(posts);
    }
    
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("POST /posts/{}/like - Toggling like by user {}", id, currentUser.getId());
        likeService.toggleLike(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("POST /posts/{}/comments - Creating comment by user {}", id, currentUser.getId());
        CommentDTO comment = commentService.createComment(id, request.get("content"), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long id) {
        log.info("GET /posts/{}/comments - Fetching comments", id);
        List<CommentDTO> comments = commentService.getCommentsByPostId(id);
        return ResponseEntity.ok(comments);
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("DELETE /posts/comments/{} - Deleting comment by user {}", commentId, currentUser.getId());
        commentService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
