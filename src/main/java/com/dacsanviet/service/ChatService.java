package com.dacsanviet.service;

import com.dacsanviet.entity.ChatMessage;
import com.dacsanviet.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling chat operations
 */
@Service
@Transactional
public class ChatService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send a user message
     */
    public ChatMessage sendUserMessage(String sessionId, String senderName, String senderEmail, String message) {
        ChatMessage chatMessage = new ChatMessage(sessionId, senderName, senderEmail, message, ChatMessage.MessageType.USER);
        chatMessage = chatMessageRepository.save(chatMessage);
        
        // Notify admin about new message
        messagingTemplate.convertAndSend("/topic/admin/new-message", chatMessage);
        
        return chatMessage;
    }
    
    /**
     * Send an admin reply
     */
    public ChatMessage sendAdminReply(String sessionId, String message, Long adminId) {
        ChatMessage chatMessage = new ChatMessage(sessionId, message, ChatMessage.MessageType.ADMIN);
        chatMessage.setAdminId(adminId);
        chatMessage.setIsRead(true); // Admin messages are automatically read
        chatMessage = chatMessageRepository.save(chatMessage);
        
        // Send message to specific user session
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, chatMessage);
        
        return chatMessage;
    }
    
    /**
     * Send a system message
     */
    public ChatMessage sendSystemMessage(String sessionId, String message) {
        ChatMessage chatMessage = new ChatMessage(sessionId, message, ChatMessage.MessageType.SYSTEM);
        chatMessage.setIsRead(true); // System messages are automatically read
        chatMessage = chatMessageRepository.save(chatMessage);
        
        // Send to user session
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, chatMessage);
        
        return chatMessage;
    }
    
    /**
     * Get chat history for a session
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
    
    /**
     * Get recent messages for a session (last 50)
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getRecentMessages(String sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findRecentMessagesBySessionId(sessionId);
        // Reverse to get chronological order (oldest first)
        java.util.Collections.reverse(messages);
        return messages;
    }
    
    /**
     * Get all unread user messages for admin
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getUnreadUserMessages() {
        return chatMessageRepository.findUnreadUserMessages();
    }
    
    /**
     * Get sessions with unread messages
     */
    @Transactional(readOnly = true)
    public List<String> getSessionsWithUnreadMessages() {
        return chatMessageRepository.findSessionsWithUnreadMessages();
    }
    
    /**
     * Count unread messages
     */
    @Transactional(readOnly = true)
    public Long countUnreadMessages() {
        return chatMessageRepository.countUnreadUserMessages();
    }
    
    /**
     * Mark messages in a session as read
     */
    public void markSessionAsRead(String sessionId) {
        chatMessageRepository.markSessionMessagesAsRead(sessionId);
        
        // Notify admin about read status update
        messagingTemplate.convertAndSend("/topic/admin/messages-read", sessionId);
    }
    
    /**
     * Generate a new session ID
     */
    public String generateSessionId() {
        return "chat_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
    /**
     * Get messages after a specific time (for polling)
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesAfter(String sessionId, LocalDateTime after) {
        return chatMessageRepository.findBySessionIdAndCreatedAtAfterOrderByCreatedAtAsc(sessionId, after);
    }
    
    /**
     * Clean up old messages (older than 30 days)
     */
    public void cleanupOldMessages() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        chatMessageRepository.deleteOldMessages(cutoffDate);
    }
    
    /**
     * Initialize chat session with welcome message
     */
    public String initializeChatSession() {
        String sessionId = generateSessionId();
        sendSystemMessage(sessionId, "Xin chào! Chúng tôi có thể giúp gì cho bạn hôm nay?");
        return sessionId;
    }
}