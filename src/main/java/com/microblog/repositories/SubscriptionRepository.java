package com.microblog.repositories;

import com.microblog.entities.Subscription;
import com.microblog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByFollowerAndFollowed(User follower, User followed);

    boolean existsByFollowerAndFollowed(User follower, User followed);

    List<Subscription> findByFollower(User follower);

    long countByFollower(User follower);

    long countByFollowed(User followed);
}
