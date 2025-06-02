package com.microblog.service.impl;

import com.microblog.dto.PostDTO;
import com.microblog.dto.UserDTO;
import com.microblog.repository.PostRepository;
import com.microblog.repository.UserRepository;
import com.microblog.service.PostService;
import com.microblog.service.SearchService;
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
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    private final UserService userService;
    
    @Override
    public List<PostDTO> searchPosts(String query, Long currentUserId) {
        log.info("Searching posts with query: {}", query);
        return postRepository.searchPosts(query)
                .stream()
                .map(post -> postService.convertToDTO(post, currentUserId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserDTO> searchUsers(String query, Long currentUserId) {
        log.info("Searching users with query: {}", query);
        return userRepository.searchUsers(query)
                .stream()
                .map(user -> userService.convertToDTO(user, currentUserId))
                .collect(Collectors.toList());
    }
}
