package com.microblog.controllers;

import com.microblog.dto.request.PostRequest;
import com.microblog.dto.response.PostResponse;
import com.microblog.entities.User;
import com.microblog.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post creation, feeds, and deletion")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Create a new post", description = "Creates a new microblog post")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        PostResponse response = postService.createPost(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/global")
    @Operation(summary = "Global feed", description = "Returns all posts sorted by date (newest first), paginated")
    public ResponseEntity<Page<PostResponse>> getGlobalFeed(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(postService.getGlobalFeed(pageable, currentUser));
    }

    @GetMapping("/feed")
    @Operation(summary = "Personal feed", description = "Returns posts only from users you follow, sorted by date, paginated")
    public ResponseEntity<Page<PostResponse>> getPersonalFeed(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(postService.getPersonalFeed(pageable, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post", description = "Deletes a post (author, manager, or admin)")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        postService.deletePost(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }
}
