package com.microblog.controller;

import com.microblog.dto.UserDTO;
import com.microblog.security.UserPrincipal;
import com.microblog.service.FollowService;
import com.microblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    private final FollowService followService;
    
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /users - Fetching all users");
        List<UserDTO> users = userService.getAllUsers(currentUser.getId());
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("GET /users/{} - Fetching user by id", id);
        UserDTO user = userService.getUserById(id, currentUser.getId());
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody Map<String, String> updates,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("PUT /users/profile - Updating profile for user: {}", currentUser.getId());
        UserDTO user = userService.updateProfile(
                currentUser.getId(),
                updates.get("displayName"),
                updates.get("bio")
        );
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followUser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("POST /users/{}/follow - User {} following user {}", id, currentUser.getId(), id);
        followService.followUser(currentUser.getId(), id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unfollowUser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("DELETE /users/{}/follow - User {} unfollowing user {}", id, currentUser.getId(), id);
        followService.unfollowUser(currentUser.getId(), id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable Long id) {
        log.info("GET /users/{}/followers - Fetching followers", id);
        List<UserDTO> followers = followService.getFollowers(id);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserDTO>> getFollowing(@PathVariable Long id) {
        log.info("GET /users/{}/following - Fetching following", id);
        List<UserDTO> following = followService.getFollowing(id);
        return ResponseEntity.ok(following);
    }
}
