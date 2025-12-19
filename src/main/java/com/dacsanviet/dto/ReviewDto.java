package com.dacsanviet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long productId;
    private String reviewerName;
    private String reviewerEmail;
    private Integer rating;
    private String title;
    private String content;
    private Boolean isVerifiedBuyer;
    private List<String> imageUrls = new ArrayList<>();
    private LocalDateTime createdAt;
    private String timeAgo;
}
