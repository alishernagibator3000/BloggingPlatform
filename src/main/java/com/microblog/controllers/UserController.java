package com.microblog.controllers;

import com.microblog.dto.request.RoleUpdateRequest;
import com.microblog.dto.response.UserResponse;
import com.microblog.entities.User;
import com.microblog.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and subscription management")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile", description = "Returns user profile with statistics (posts, followers, following)")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping
    @Operation(summary = "Get all users (ADMIN only)", description = "Returns a list of all registered users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/{id}/follow")
    @Operation(summary = "Follow a user", description = "Subscribe to the specified user's posts")
    public ResponseEntity<Map<String, String>> followUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.followUser(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Successfully followed user"));
    }

    @DeleteMapping("/{id}/follow")
    @Operation(summary = "Unfollow a user", description = "Unsubscribe from the specified user's posts")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.unfollowUser(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Successfully unfollowed user"));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role (ADMIN only)", description = "Change the role of a user")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    @PutMapping("/{id}/block")
    @Operation(summary = "Block/unblock user (ADMIN only)", description = "Block or unblock a user account")
    public ResponseEntity<UserResponse> blockUser(
            @PathVariable Long id,
            @RequestParam boolean blocked
    ) {
        return ResponseEntity.ok(userService.blockUser(id, blocked));
    }
}
