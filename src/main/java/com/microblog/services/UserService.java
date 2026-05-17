package com.microblog.services;

import com.microblog.dto.request.RoleUpdateRequest;
import com.microblog.dto.response.UserResponse;
import com.microblog.entities.User;

import java.util.List;

public interface UserService {

    UserResponse getUserProfile(Long userId);

    List<UserResponse> getAllUsers();

    void followUser(Long targetUserId, User currentUser);

    void unfollowUser(Long targetUserId, User currentUser);

    UserResponse updateUserRole(Long userId, RoleUpdateRequest request);

    UserResponse blockUser(Long userId, boolean blocked);
}
