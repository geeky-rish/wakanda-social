package com.microblog.service;

public interface LikeService {
    void toggleLike(Long postId, Long userId);
    boolean isPostLikedByUser(Long postId, Long userId);
    Long getLikesCount(Long postId);
}
