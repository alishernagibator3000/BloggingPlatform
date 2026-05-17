package com.microblog.services;

import com.microblog.dto.request.PostRequest;
import com.microblog.dto.response.PostResponse;
import com.microblog.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostResponse createPost(PostRequest request, User currentUser);

    Page<PostResponse> getGlobalFeed(Pageable pageable, User currentUser);

    Page<PostResponse> getPersonalFeed(Pageable pageable, User currentUser);

    void deletePost(Long postId, User currentUser);
}
