package com.microblog.services.impl;

import com.microblog.dto.request.PostRequest;
import com.microblog.dto.response.PostResponse;
import com.microblog.entities.Post;
import com.microblog.entities.Role;
import com.microblog.entities.Subscription;
import com.microblog.entities.User;
import com.microblog.exceptions.AccessDeniedException;
import com.microblog.exceptions.ResourceNotFoundException;
import com.microblog.repositories.CommentRepository;
import com.microblog.repositories.LikeRepository;
import com.microblog.repositories.PostRepository;
import com.microblog.repositories.SubscriptionRepository;
import com.microblog.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public PostResponse createPost(PostRequest request, User currentUser) {
        Post post = Post.builder()
                .content(request.getContent())
                .user(currentUser)
                .build();

        post = postRepository.save(post);
        return mapToPostResponse(post, currentUser);
    }

    @Override
    public Page<PostResponse> getGlobalFeed(Pageable pageable, User currentUser) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(post -> mapToPostResponse(post, currentUser));
    }

    @Override
    public Page<PostResponse> getPersonalFeed(Pageable pageable, User currentUser) {
        List<Subscription> subscriptions = subscriptionRepository.findByFollower(currentUser);

        List<User> feedUsers = new ArrayList<>(subscriptions.stream()
                .map(Subscription::getFollowed)
                .toList());

        // Include the current user's own posts in the personal feed
        feedUsers.add(currentUser);

        return postRepository.findByUserInOrderByCreatedAtDesc(feedUsers, pageable)
                .map(post -> mapToPostResponse(post, currentUser));
    }

    @Override
    @Transactional
    public void deletePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());
        boolean isModerator = currentUser.getRole() == Role.ROLE_MANAGER
                || currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isAuthor && !isModerator) {
            throw new AccessDeniedException("You do not have permission to delete this post");
        }

        // Remove related entities before deleting the post to avoid FK constraint violations
        likeRepository.deleteByPost(post);
        commentRepository.deleteByPost(post);

        postRepository.delete(post);
    }

    private PostResponse mapToPostResponse(Post post, User currentUser) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .authorUsername(post.getUser().getUsername())
                .authorId(post.getUser().getId())
                .createdAt(post.getCreatedAt())
                .likesCount(likeRepository.countByPost(post))
                .likedByCurrentUser(
                        currentUser != null && likeRepository.existsByUserAndPost(currentUser, post))
                .build();
    }
}
