package com.dacsanviet.controller;

import com.dacsanviet.dto.ReviewDto;
import com.dacsanviet.model.User;
import com.dacsanviet.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ProductReviewController {
    
    private final ProductReviewService reviewService;
    
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<ReviewDto> createReview(
            @RequestParam("productId") Long productId,
            @RequestParam("reviewerName") String reviewerName,
            @RequestParam("reviewerEmail") String reviewerEmail,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = null;
        // Get current user if logged in
        // You'll need to implement this based on your User service
        
        ReviewDto review = reviewService.createReview(
                productId, reviewerName, reviewerEmail, rating, title, content, images, currentUser);
        
        return ResponseEntity.ok(review);
    }
    
    @GetMapping("/product/{productId}")
    @ResponseBody
    public ResponseEntity<List<ReviewDto>> getProductReviews(@PathVariable Long productId) {
        List<ReviewDto> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }
}
