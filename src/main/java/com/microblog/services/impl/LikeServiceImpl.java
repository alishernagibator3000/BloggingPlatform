package com.microblog.services.impl;

import com.microblog.entities.Like;
import com.microblog.entities.Post;
import com.microblog.entities.User;
import com.microblog.exceptions.DuplicateResourceException;
import com.microblog.exceptions.ResourceNotFoundException;
import com.microblog.repositories.LikeRepository;
import com.microblog.repositories.PostRepository;
import com.microblog.services.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void likePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        if (likeRepository.existsByUserAndPost(currentUser, post)) {
            throw new DuplicateResourceException("You have already liked this post");
        }

        Like like = Like.builder()
                .user(currentUser)
                .post(post)
                .build();

        likeRepository.save(like);
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        Like like = likeRepository.findByUserAndPost(currentUser, post)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found for this post"));

        likeRepository.delete(like);
    }
}
