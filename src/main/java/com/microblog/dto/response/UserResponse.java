package com.microblog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String bio;
    private String role;
    private boolean blocked;
    private LocalDateTime createdAt;
    private long postsCount;
    private long followersCount;
    private long followingCount;
}
