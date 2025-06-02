package com.microblog.service;

import com.microblog.dto.UserDTO;
import java.util.List;

public interface FollowService {
    void followUser(Long userId, Long targetUserId);
    void unfollowUser(Long userId, Long targetUserId);
    boolean isFollowing(Long userId, Long targetUserId);
    List<UserDTO> getFollowers(Long userId);
    List<UserDTO> getFollowing(Long userId);
}
