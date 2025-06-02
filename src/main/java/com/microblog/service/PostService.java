package com.microblog.service;

import com.microblog.dto.PostDTO;
import java.util.List;

public interface PostService {
    List<PostDTO> getAllPosts(Long currentUserId);
    List<PostDTO> getUserFeed(Long userId);
    PostDTO createPost(Long userId, String content);
    List<PostDTO> getPostsByUserId(Long userId, Long currentUserId);
    void deletePost(Long postId, Long userId);
    PostDTO convertToDTO(com.microblog.entity.Post post, Long currentUserId);
}
