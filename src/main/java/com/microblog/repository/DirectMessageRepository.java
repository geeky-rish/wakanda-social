package com.microblog.repository;

import com.microblog.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Query("SELECT dm FROM DirectMessage dm WHERE " +
            "(dm.sender.id = :userId1 AND dm.receiver.id = :userId2) OR " +
            "(dm.sender.id = :userId2 AND dm.receiver.id = :userId1) " +
            "ORDER BY dm.timestamp ASC")
    List<DirectMessage> findConversationBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT dm FROM DirectMessage dm WHERE " +
            "dm.receiver.id = :userId AND dm.isRead = false")
    List<DirectMessage> findUnreadMessages(@Param("userId") Long userId);

    @Query("SELECT COUNT(dm) FROM DirectMessage dm WHERE " +
            "dm.receiver.id = :userId AND dm.isRead = false")
    Long countUnreadMessages(@Param("userId") Long userId);

    @Query("SELECT DISTINCT CASE " +
            "WHEN dm.sender.id = :userId THEN dm.receiver " +
            "ELSE dm.sender END " +
            "FROM DirectMessage dm WHERE " +
            "dm.sender.id = :userId OR dm.receiver.id = :userId")
    List<com.microblog.entity.User> findConversationPartners(@Param("userId") Long userId);

    @Query("SELECT dm FROM DirectMessage dm WHERE " +
            "((dm.sender.id = :userId1 AND dm.receiver.id = :userId2) OR " +
            "(dm.sender.id = :userId2 AND dm.receiver.id = :userId1)) " +
            "AND dm.timestamp = (SELECT MAX(dm2.timestamp) FROM DirectMessage dm2 WHERE " +
            "((dm2.sender.id = :userId1 AND dm2.receiver.id = :userId2) OR " +
            "(dm2.sender.id = :userId2 AND dm2.receiver.id = :userId1)))")
    DirectMessage findLastMessageBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
