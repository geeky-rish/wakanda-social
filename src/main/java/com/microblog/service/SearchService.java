package com.microblog.service;

import com.microblog.dto.PostDTO;
import com.microblog.dto.UserDTO;
import java.util.List;

public interface SearchService {
    List<PostDTO> searchPosts(String query, Long currentUserId);
    List<UserDTO> searchUsers(String query, Long currentUserId);
}
