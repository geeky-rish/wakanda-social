package com.microblog.service.impl;

import com.microblog.dto.UserDTO;
import com.microblog.entity.User;
import com.microblog.repository.UserRepository;
import com.microblog.service.FollowService;
import com.microblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FollowServiceImpl implements FollowService {
    
    private final UserRepository userRepository;
    private final UserService userService;
    
    @Override
    public void followUser(Long userId, Long targetUserId) {
        log.info("User {} following user {}", userId, targetUserId);
        
        if (userId.equals(targetUserId)) {
            throw new RuntimeException("You cannot follow yourself");
        }
        
        User user = userService.getUserEntityById(userId);
        User targetUser = userService.getUserEntityById(targetUserId);
        
        if (!user.getFollowing().contains(targetUser)) {
            user.getFollowing().add(targetUser);
            targetUser.getFollowers().add(user);
            
            // Update counts
            user.setFollowingCount(user.getFollowingCount() + 1);
            targetUser.setFollowersCount(targetUser.getFollowersCount() + 1);
            
            userRepository.save(user);
            userRepository.save(targetUser);
        }
    }
    
    @Override
    public void unfollowUser(Long userId, Long targetUserId) {
        log.info("User {} unfollowing user {}", userId, targetUserId);
        
        User user = userService.getUserEntityById(userId);
        User targetUser = userService.getUserEntityById(targetUserId);
        
        if (user.getFollowing().contains(targetUser)) {
            user.getFollowing().remove(targetUser);
            targetUser.getFollowers().remove(user);
            
            // Update counts
            user.setFollowingCount(Math.max(0, user.getFollowingCount() - 1));
            targetUser.setFollowersCount(Math.max(0, targetUser.getFollowersCount() - 1));
            
            userRepository.save(user);
            userRepository.save(targetUser);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, Long targetUserId) {
        return userRepository.isFollowing(targetUserId, userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getFollowers(Long userId) {
        User user = userService.getUserEntityById(userId);
        return user.getFollowers().stream()
                .map(follower -> userService.convertToDTO(follower, userId))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getFollowing(Long userId) {
        User user = userService.getUserEntityById(userId);
        return user.getFollowing().stream()
                .map(following -> userService.convertToDTO(following, userId))
                .collect(Collectors.toList());
    }
}
