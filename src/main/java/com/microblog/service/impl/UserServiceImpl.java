package com.microblog.service.impl;

import com.microblog.dto.UserDTO;
import com.microblog.entity.User;
import com.microblog.exception.UserNotFoundException;
import com.microblog.repository.UserRepository;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers(Long currentUserId) {
        log.info("Fetching all users for user: {}", currentUserId);
        return userRepository.findAll().stream()
                .map(user -> convertToDTO(user, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id, Long currentUserId) {
        log.info("Fetching user with id: {} for user: {}", id, currentUserId);
        User user = getUserEntityById(id);
        return convertToDTO(user, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public User createUser(String username, String email, String password) {
        log.info("Creating user with username: {}", username);
        User user = new User(username, email, password);
        return userRepository.save(user);
    }

    @Override
    public UserDTO updateProfile(Long userId, String displayName, String bio) {
        log.info("Updating profile for user: {}", userId);
        User user = getUserEntityById(userId);

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (bio != null) {
            user.setBio(bio);
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser, userId);
    }

    @Override
    public UserDTO convertToDTO(User user, Long currentUserId) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setBio(user.getBio());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setFollowersCount(user.getFollowersCount());
        dto.setFollowingCount(user.getFollowingCount());
        dto.setPostsCount(user.getPostsCount());

        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            dto.setIsFollowing(userRepository.isFollowing(user.getId(), currentUserId));
        } else {
            dto.setIsFollowing(false);
        }

        return dto;
    }
}
