package com.microblog.controller;

import com.microblog.dto.PostDTO;
import com.microblog.dto.UserDTO;
import com.microblog.security.UserPrincipal;
import com.microblog.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/posts")
    public ResponseEntity<List<PostDTO>> searchPosts(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /search/posts - Searching posts with query: {}", query);
        List<PostDTO> posts = searchService.searchPosts(query, currentUser.getId());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /search/users - Searching users with query: {}", query);
        List<UserDTO> users = searchService.searchUsers(query, currentUser.getId());
        return ResponseEntity.ok(users);
    }
}
