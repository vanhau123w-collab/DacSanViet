package com.dacsanviet.model;

/**
 * Comment Status Enum
 */
public enum CommentStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối");
    
    private final String displayName;
    
    CommentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}