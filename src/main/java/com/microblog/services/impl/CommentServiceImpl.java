package com.microblog.services.impl;

import com.microblog.dto.request.CommentRequest;
import com.microblog.dto.response.CommentResponse;
import com.microblog.entities.Comment;
import com.microblog.entities.Post;
import com.microblog.entities.Role;
import com.microblog.entities.User;
import com.microblog.exceptions.AccessDeniedException;
import com.microblog.exceptions.ResourceNotFoundException;
import com.microblog.repositories.CommentRepository;
import com.microblog.repositories.PostRepository;
import com.microblog.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        Comment comment = Comment.builder()
                .text(request.getText())
                .post(post)
                .user(currentUser)
                .build();

        comment = commentRepository.save(comment);
        return mapToCommentResponse(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        return commentRepository.findByPostOrderByCreatedAtDesc(post).stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isModerator = currentUser.getRole() == Role.ROLE_MANAGER
                || currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isAuthor && !isModerator) {
            throw new AccessDeniedException("You do not have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorUsername(comment.getUser().getUsername())
                .authorId(comment.getUser().getId())
                .postId(comment.getPost().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
