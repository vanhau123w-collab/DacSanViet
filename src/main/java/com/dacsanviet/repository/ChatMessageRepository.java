package com.dacsanviet.repository;

import com.dacsanviet.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Find all messages for a specific session ordered by creation time
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * Find unread messages for admin
     */
    @Query("SELECT c FROM ChatMessage c WHERE c.isRead = false AND c.messageType = 'USER' ORDER BY c.createdAt ASC")
    List<ChatMessage> findUnreadUserMessages();
    
    /**
     * Find all user sessions with unread messages
     */
    @Query("SELECT DISTINCT c.sessionId FROM ChatMessage c WHERE c.isRead = false AND c.messageType = 'USER'")
    List<String> findSessionsWithUnreadMessages();
    
    /**
     * Count unread messages for admin
     */
    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.isRead = false AND c.messageType = 'USER'")
    Long countUnreadUserMessages();
    
    /**
     * Find recent messages for a session (last 50)
     */
    @Query("SELECT c FROM ChatMessage c WHERE c.sessionId = :sessionId ORDER BY c.createdAt DESC LIMIT 50")
    List<ChatMessage> findRecentMessagesBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * Mark all messages in a session as read
     */
    @Query("UPDATE ChatMessage c SET c.isRead = true WHERE c.sessionId = :sessionId AND c.messageType = 'USER'")
    void markSessionMessagesAsRead(@Param("sessionId") String sessionId);
    
    /**
     * Find messages created after a specific time
     */
    List<ChatMessage> findBySessionIdAndCreatedAtAfterOrderByCreatedAtAsc(String sessionId, LocalDateTime after);
    
    /**
     * Delete old messages (older than specified days)
     */
    @Query("DELETE FROM ChatMessage c WHERE c.createdAt < :cutoffDate")
    void deleteOldMessages(@Param("cutoffDate") LocalDateTime cutoffDate);
}