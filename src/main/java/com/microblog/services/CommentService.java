package com.microblog.services;

import com.microblog.dto.request.CommentRequest;
import com.microblog.dto.response.CommentResponse;
import com.microblog.entities.User;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(Long postId, CommentRequest request, User currentUser);

    List<CommentResponse> getCommentsByPost(Long postId);

    void deleteComment(Long commentId, User currentUser);
}
