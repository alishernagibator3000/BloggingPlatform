package com.microblog.controllers;

import com.microblog.dto.request.CommentRequest;
import com.microblog.dto.response.CommentResponse;
import com.microblog.entities.User;
import com.microblog.services.CommentService;
import com.microblog.services.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Interactions", description = "Comments and likes management")
public class InteractionController {

    private final CommentService commentService;
    private final LikeService likeService;

    // --- Comments ---

    @PostMapping("/api/posts/{postId}/comments")
    @Operation(summary = "Add a comment", description = "Add a comment to a post")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        CommentResponse response = commentService.createComment(postId, request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/posts/{postId}/comments")
    @Operation(summary = "Get comments for a post", description = "Returns all comments for a post sorted by date")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @DeleteMapping("/api/comments/{commentId}")
    @Operation(summary = "Delete a comment", description = "Deletes a comment (author, manager, or admin)")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser
    ) {
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    }

    // --- Likes ---

    @PostMapping("/api/posts/{postId}/like")
    @Operation(summary = "Like a post", description = "Like a post")
    public ResponseEntity<Map<String, String>> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser
    ) {
        likeService.likePost(postId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
    }

    @DeleteMapping("/api/posts/{postId}/like")
    @Operation(summary = "Unlike a post", description = "Remove a like from a post")
    public ResponseEntity<Map<String, String>> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser
    ) {
        likeService.unlikePost(postId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Post unliked successfully"));
    }
}
