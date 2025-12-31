package com.dacsanviet.dto;

import com.dacsanviet.model.CommentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for NewsComment entity
 * Used for transferring news comment data between layers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsCommentDto {
    
    private Long id;
    
    private Long articleId;
    
    private String articleTitle;
    
    private Long userId;
    
    private String userName;
    
    @Size(max = 100, message = "Tên khách không được vượt quá 100 ký tự")
    private String guestName;
    
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String guestEmail;
    
    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(min = 1, max = 1000, message = "Nội dung bình luận phải từ 1 đến 1000 ký tự")
    private String content;
    
    private Long parentId;
    
    private String parentAuthorName;
    
    private CommentStatus status;
    
    private LocalDateTime createdAt;
    
    private List<NewsCommentDto> replies = new ArrayList<>();
    
    // Constructor for user comment creation
    public NewsCommentDto(Long articleId, Long userId, String content) {
        this.articleId = articleId;
        this.userId = userId;
        this.content = content;
        this.status = CommentStatus.PENDING;
        this.replies = new ArrayList<>();
    }
    
    // Constructor for guest comment creation
    public NewsCommentDto(Long articleId, String guestName, String guestEmail, String content) {
        this.articleId = articleId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.content = content;
        this.status = CommentStatus.PENDING;
        this.replies = new ArrayList<>();
    }
    
    // Constructor for reply creation
    public NewsCommentDto(Long articleId, Long userId, String content, Long parentId) {
        this.articleId = articleId;
        this.userId = userId;
        this.content = content;
        this.parentId = parentId;
        this.status = CommentStatus.PENDING;
        this.replies = new ArrayList<>();
    }
    
    // Constructor for comment display
    public NewsCommentDto(Long id, Long articleId, String articleTitle, Long userId, 
                         String userName, String guestName, String guestEmail, 
                         String content, Long parentId, CommentStatus status, 
                         LocalDateTime createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.articleTitle = articleTitle;
        this.userId = userId;
        this.userName = userName;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.content = content;
        this.parentId = parentId;
        this.status = status;
        this.createdAt = createdAt;
        this.replies = new ArrayList<>();
    }
    
    // Helper methods
    public boolean isGuestComment() {
        return userId == null;
    }
    
    public boolean isUserComment() {
        return userId != null;
    }
    
    public boolean isReply() {
        return parentId != null;
    }
    
    public String getAuthorName() {
        return isUserComment() ? userName : guestName;
    }
    
    public String getAuthorEmail() {
        return isUserComment() ? null : guestEmail; // Don't expose user emails
    }
}