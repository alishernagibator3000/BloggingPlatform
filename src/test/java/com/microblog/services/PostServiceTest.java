package com.microblog.services;

import com.microblog.dto.request.PostRequest;
import com.microblog.dto.response.PostResponse;
import com.microblog.entities.Post;
import com.microblog.entities.Role;
import com.microblog.entities.User;
import com.microblog.exceptions.AccessDeniedException;
import com.microblog.exceptions.ResourceNotFoundException;
import com.microblog.repositories.LikeRepository;
import com.microblog.repositories.PostRepository;
import com.microblog.repositories.SubscriptionRepository;
import com.microblog.services.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Unit Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private User testUser;
    private User otherUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .build();

        testPost = Post.builder()
                .id(1L)
                .content("Hello, World!")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create a post")
    void createPost_Success() {
        // Arrange
        PostRequest request = PostRequest.builder()
                .content("Hello, World!")
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(likeRepository.countByPost(any(Post.class))).thenReturn(0L);
        when(likeRepository.existsByUserAndPost(any(User.class), any(Post.class))).thenReturn(false);

        // Act
        PostResponse response = postService.createPost(request, testUser);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hello, World!");
        assertThat(response.getAuthorUsername()).isEqualTo("testuser");
        assertThat(response.getAuthorId()).isEqualTo(1L);
        assertThat(response.getLikesCount()).isEqualTo(0);
        assertThat(response.isLikedByCurrentUser()).isFalse();

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should successfully delete a post by the author")
    void deletePost_ByAuthor_Success() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        // Act
        postService.deletePost(1L, testUser);

        // Assert
        verify(postRepository, times(1)).delete(testPost);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when a regular user tries to delete another user's post")
    void deletePost_ByNonAuthor_ThrowsAccessDenied() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        // Act & Assert
        assertThatThrownBy(() -> postService.deletePost(1L, otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to delete this post");

        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("Should allow a MANAGER to delete any post")
    void deletePost_ByManager_Success() {
        // Arrange
        User manager = User.builder()
                .id(3L)
                .username("manager")
                .email("manager@example.com")
                .password("encoded_password")
                .role(Role.ROLE_MANAGER)
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        // Act
        postService.deletePost(1L, manager);

        // Assert
        verify(postRepository, times(1)).delete(testPost);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting a non-existent post")
    void deletePost_NotFound_ThrowsResourceNotFound() {
        // Arrange
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.deletePost(99L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found with id: 99");

        verify(postRepository, never()).delete(any(Post.class));
    }
}
