package com.dacsanviet.controller;

import com.dacsanviet.entity.ChatMessage;
import com.dacsanviet.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling chat operations
 */
@Controller
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    /**
     * Handle user messages via WebSocket
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> messageData, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            String senderName = (String) messageData.get("senderName");
            String senderEmail = (String) messageData.get("senderEmail");
            String message = (String) messageData.get("message");
            
            if (sessionId == null || message == null || message.trim().isEmpty()) {
                return;
            }
            
            // Save and broadcast message
            chatService.sendUserMessage(sessionId, senderName, senderEmail, message.trim());
            
        } catch (Exception e) {
            System.err.println("Error handling chat message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle admin replies via WebSocket
     */
    @MessageMapping("/chat.adminReply")
    public void sendAdminReply(@Payload Map<String, Object> messageData, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            String message = (String) messageData.get("message");
            Long adminId = messageData.get("adminId") != null ? 
                Long.valueOf(messageData.get("adminId").toString()) : null;
            
            if (sessionId == null || message == null || message.trim().isEmpty()) {
                return;
            }
            
            // Save and broadcast admin reply
            chatService.sendAdminReply(sessionId, message.trim(), adminId);
            
        } catch (Exception e) {
            System.err.println("Error handling admin reply: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize a new chat session
     */
    @PostMapping("/api/chat/init")
    @ResponseBody
    public ResponseEntity<Map<String, String>> initializeChat() {
        try {
            String sessionId = chatService.initializeChatSession();
            
            Map<String, String> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("message", "Chat session initialized successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error initializing chat: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to initialize chat session");
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get chat history for a session
     */
    @GetMapping("/api/chat/history/{sessionId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        try {
            List<ChatMessage> messages = chatService.getRecentMessages(sessionId);
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            System.err.println("Error getting chat history: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Send a message via REST API (fallback for non-WebSocket clients)
     */
    @PostMapping("/api/chat/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> messageData) {
        try {
            String sessionId = (String) messageData.get("sessionId");
            String senderName = (String) messageData.get("senderName");
            String senderEmail = (String) messageData.get("senderEmail");
            String message = (String) messageData.get("message");
            
            if (sessionId == null || message == null || message.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Session ID and message are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            ChatMessage chatMessage = chatService.sendUserMessage(sessionId, senderName, senderEmail, message.trim());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", chatMessage);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to send message");
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Mark session messages as read
     */
    @PostMapping("/api/chat/mark-read/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable String sessionId) {
        try {
            chatService.markSessionAsRead(sessionId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Messages marked as read");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to mark messages as read");
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get unread message count (for admin)
     */
    @GetMapping("/api/chat/admin/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        try {
            Long count = chatService.countUnreadMessages();
            
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Long> error = new HashMap<>();
            error.put("count", 0L);
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Get sessions with unread messages (for admin)
     */
    @GetMapping("/api/chat/admin/unread-sessions")
    @ResponseBody
    public ResponseEntity<List<String>> getUnreadSessions() {
        try {
            List<String> sessions = chatService.getSessionsWithUnreadMessages();
            return ResponseEntity.ok(sessions);
            
        } catch (Exception e) {
            System.err.println("Error getting unread sessions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}