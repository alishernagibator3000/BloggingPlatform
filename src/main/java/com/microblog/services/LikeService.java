package com.microblog.services;

import com.microblog.entities.User;

public interface LikeService {

    void likePost(Long postId, User currentUser);

    void unlikePost(Long postId, User currentUser);
}
