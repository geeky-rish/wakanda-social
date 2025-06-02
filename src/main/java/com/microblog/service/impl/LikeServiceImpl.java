package com.microblog.service.impl;

import com.microblog.entity.Like;
import com.microblog.entity.Post;
import com.microblog.entity.User;
import com.microblog.repository.LikeRepository;
import com.microblog.repository.PostRepository;
import com.microblog.service.LikeService;
import com.microblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LikeServiceImpl implements LikeService {
    
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    
    @Override
    public void toggleLike(Long postId, Long userId) {
        log.info("Toggling like for post {} by user {}", postId, userId);
        
        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(userId, postId);
        
        if (existingLike.isPresent()) {
            // Unlike
            likeRepository.delete(existingLike.get());
            updatePostLikesCount(postId, -1);
        } else {
            // Like
            User user = userService.getUserEntityById(userId);
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            
            Like like = new Like(user, post);
            likeRepository.save(like);
            updatePostLikesCount(postId, 1);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPostLikedByUser(Long postId, Long userId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getLikesCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }
    
    private void updatePostLikesCount(Long postId, int delta) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikesCount(Math.max(0, post.getLikesCount() + delta));
        postRepository.save(post);
    }
}
