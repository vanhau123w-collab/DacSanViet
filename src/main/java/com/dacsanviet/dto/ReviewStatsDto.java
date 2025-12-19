package com.dacsanviet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDto {
    private Double averageRating;
    private Long totalReviews;
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;
    
    public Integer getFiveStarPercentage() {
        return totalReviews > 0 ? (int) Math.round((fiveStarCount * 100.0) / totalReviews) : 0;
    }
    
    public Integer getFourStarPercentage() {
        return totalReviews > 0 ? (int) Math.round((fourStarCount * 100.0) / totalReviews) : 0;
    }
    
    public Integer getThreeStarPercentage() {
        return totalReviews > 0 ? (int) Math.round((threeStarCount * 100.0) / totalReviews) : 0;
    }
    
    public Integer getTwoStarPercentage() {
        return totalReviews > 0 ? (int) Math.round((twoStarCount * 100.0) / totalReviews) : 0;
    }
    
    public Integer getOneStarPercentage() {
        return totalReviews > 0 ? (int) Math.round((oneStarCount * 100.0) / totalReviews) : 0;
    }
}
