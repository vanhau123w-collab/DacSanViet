package com.specialtyfood.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;

/**
 * DTO for WebSocket notifications
 */
public class NotificationDto {
    private String type;
    private String message;
    private long timestamp;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    public NotificationDto() {}

    public NotificationDto(String type, String message, long timestamp) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "NotificationDto{" +
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", dateTime=" + dateTime +
                '}';
    }
}