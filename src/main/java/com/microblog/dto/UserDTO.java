package com.microblog.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String bio;
    private String profileImageUrl;
    private Integer followersCount;
    private Integer followingCount;
    private Integer postsCount;
    private Boolean isFollowing;
}
