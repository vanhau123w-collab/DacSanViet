package com.dacsanviet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * News Comment entity representing comments on news articles
 */
@Entity
@Table(name = "news_comments", indexes = {
    @Index(name = "idx_comment_article", columnList = "article_id"),
    @Index(name = "idx_comment_status", columnList = "status"),
    @Index(name = "idx_comment_created_at", columnList = "created_at"),
    @Index(name = "idx_comment_parent", columnList = "parent_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null for guest comments
    
    @Column(name = "guest_name", length = 100)
    @Size(max = 100, message = "Tên khách không được vượt quá 100 ký tự")
    private String guestName;
    
    @Column(name = "guest_email", length = 100)
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String guestEmail;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(min = 1, max = 1000, message = "Nội dung bình luận phải từ 1 đến 1000 ký tự")
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private NewsComment parent; // for replies
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NewsComment> replies = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructor for user comments
    public NewsComment(NewsArticle article, User user, String content) {
        this.article = article;
        this.user = user;
        this.content = content;
        this.status = CommentStatus.PENDING;
        this.replies = new ArrayList<>();
    }
    
    // Constructor for guest comments
    public NewsComment(NewsArticle article, String guestName, String guestEmail, String content) {
        this.article = article;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.content = content;
        this.status = CommentStatus.PENDING;
        this.replies = new ArrayList<>();
    }
    
    // Constructor for reply comments
    public NewsComment(NewsArticle article, User user, String content, NewsComment parent) {
        this.article = article;
        this.user = user;
        this.content = content;
        this.parent = parent;
        this.status = CommentStatus.PENDING;
        this.replies = new ArrayList<>();
    }
    
    // Helper methods
    public void addReply(NewsComment reply) {
        replies.add(reply);
        reply.setParent(this);
    }
    
    public void removeReply(NewsComment reply) {
        replies.remove(reply);
        reply.setParent(null);
    }
    
    public void approve() {
        this.status = CommentStatus.APPROVED;
    }
    
    public void reject() {
        this.status = CommentStatus.REJECTED;
    }
    
    public void setPending() {
        this.status = CommentStatus.PENDING;
    }
    
    public boolean isApproved() {
        return this.status == CommentStatus.APPROVED;
    }
    
    public boolean isPending() {
        return this.status == CommentStatus.PENDING;
    }
    
    public boolean isRejected() {
        return this.status == CommentStatus.REJECTED;
    }
    
    public boolean isGuestComment() {
        return this.user == null;
    }
    
    public boolean isUserComment() {
        return this.user != null;
    }
    
    public boolean isReply() {
        return this.parent != null;
    }
    
    public String getAuthorName() {
        return isUserComment() ? user.getFullName() : guestName;
    }
    
    public String getAuthorEmail() {
        return isUserComment() ? user.getEmail() : guestEmail;
    }
}