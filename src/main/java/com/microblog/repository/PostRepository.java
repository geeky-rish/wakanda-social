package com.microblog.repository;

import com.microblog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    @Query("SELECT p FROM Post p ORDER BY p.timestamp DESC")
    List<Post> findAllOrderByTimestampDesc();
    
    List<Post> findByUserIdOrderByTimestampDesc(Long userId);
    
    @Query("SELECT p FROM Post p WHERE p.content LIKE %:query% ORDER BY p.timestamp DESC")
    List<Post> searchPosts(@Param("query") String query);
    
    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds ORDER BY p.timestamp DESC")
    List<Post> findFeedPosts(@Param("userIds") List<Long> userIds);
    
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId OR p.user.id IN " +
           "(SELECT f.id FROM User u JOIN u.following f WHERE u.id = :userId) " +
           "ORDER BY p.timestamp DESC")
    List<Post> findUserFeed(@Param("userId") Long userId);
}
