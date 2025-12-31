package com.dacsanviet.model;

/**
 * News Article Status Enum
 */
public enum NewsStatus {
    DRAFT("Bản nháp"),
    PUBLISHED("Đã xuất bản"),
    ARCHIVED("Đã lưu trữ");
    
    private final String displayName;
    
    NewsStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}