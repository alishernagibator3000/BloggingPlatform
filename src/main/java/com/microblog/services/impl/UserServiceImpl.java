package com.microblog.services.impl;

import com.microblog.dto.request.RoleUpdateRequest;
import com.microblog.dto.response.UserResponse;
import com.microblog.entities.Subscription;
import com.microblog.entities.User;
import com.microblog.exceptions.BadRequestException;
import com.microblog.exceptions.DuplicateResourceException;
import com.microblog.exceptions.ResourceNotFoundException;
import com.microblog.repositories.PostRepository;
import com.microblog.repositories.SubscriptionRepository;
import com.microblog.repositories.UserRepository;
import com.microblog.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PostRepository postRepository;

    @Override
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return mapToUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void followUser(Long targetUserId, User currentUser) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new BadRequestException("You cannot follow yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        if (subscriptionRepository.existsByFollowerAndFollowed(currentUser, targetUser)) {
            throw new DuplicateResourceException("You are already following this user");
        }

        Subscription subscription = Subscription.builder()
                .follower(currentUser)
                .followed(targetUser)
                .build();

        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unfollowUser(Long targetUserId, User currentUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        Subscription subscription = subscriptionRepository
                .findByFollowerAndFollowed(currentUser, targetUser)
                .orElseThrow(() -> new ResourceNotFoundException("You are not following this user"));

        subscriptionRepository.delete(subscription);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, RoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setRole(request.getRole());
        userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse blockUser(Long userId, boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setBlocked(blocked);
        userRepository.save(user);
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .role(user.getRole().name())
                .blocked(user.isBlocked())
                .createdAt(user.getCreatedAt())
                .postsCount(postRepository.countByUser(user))
                .followersCount(subscriptionRepository.countByFollowed(user))
                .followingCount(subscriptionRepository.countByFollower(user))
                .build();
    }
}
