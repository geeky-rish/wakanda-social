package com.microblog.service;

import com.microblog.dto.UserDTO;
import com.microblog.entity.User;
import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers(Long currentUserId);
    UserDTO getUserById(Long id, Long currentUserId);
    User getUserEntityById(Long id);
    User createUser(String username, String email, String password);
    UserDTO updateProfile(Long userId, String displayName, String bio);
    UserDTO convertToDTO(User user, Long currentUserId);
}
