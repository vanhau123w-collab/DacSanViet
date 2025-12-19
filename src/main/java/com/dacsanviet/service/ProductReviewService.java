package com.dacsanviet.service;

import com.dacsanviet.dto.ReviewDto;
import com.dacsanviet.dto.ReviewStatsDto;
import com.dacsanviet.model.Product;
import com.dacsanviet.model.ProductReview;
import com.dacsanviet.model.ReviewImage;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.ProductRepository;
import com.dacsanviet.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewService {
    
    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/reviews/";
    
    @Transactional
    public ReviewDto createReview(Long productId, String reviewerName, String reviewerEmail,
                                   Integer rating, String title, String content,
                                   List<MultipartFile> images, User currentUser) {
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setReviewerName(reviewerName);
        review.setReviewerEmail(reviewerEmail);
        review.setRating(rating);
        review.setTitle(title);
        review.setContent(content);
        
        // Set user if logged in
        if (currentUser != null) {
            review.setUser(currentUser);
            // Check if verified buyer
            Boolean isVerified = reviewRepository.isVerifiedBuyer(productId, currentUser.getId());
            review.setIsVerifiedBuyer(isVerified != null && isVerified);
        }
        
        // Save review first to get ID
        review = reviewRepository.save(review);
        
        // Handle image uploads
        if (images != null && !images.isEmpty()) {
            int order = 0;
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = saveImage(image);
                    ReviewImage reviewImage = new ReviewImage();
                    reviewImage.setImageUrl(imageUrl);
                    reviewImage.setDisplayOrder(order++);
                    review.addImage(reviewImage);
                }
            }
            review = reviewRepository.save(review);
        }
        
        return convertToDto(review);
    }
    
    private String saveImage(MultipartFile file) {
        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return "/uploads/reviews/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
    }
    
    public List<ReviewDto> getProductReviews(Long productId) {
        List<ProductReview> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ReviewStatsDto getReviewStats(Long productId) {
        ReviewStatsDto stats = new ReviewStatsDto();
        stats.setAverageRating(reviewRepository.getAverageRating(productId));
        stats.setTotalReviews(reviewRepository.countByProductId(productId));
        stats.setFiveStarCount(reviewRepository.countByProductIdAndRating(productId, 5));
        stats.setFourStarCount(reviewRepository.countByProductIdAndRating(productId, 4));
        stats.setThreeStarCount(reviewRepository.countByProductIdAndRating(productId, 3));
        stats.setTwoStarCount(reviewRepository.countByProductIdAndRating(productId, 2));
        stats.setOneStarCount(reviewRepository.countByProductIdAndRating(productId, 1));
        return stats;
    }
    
    private ReviewDto convertToDto(ProductReview review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setReviewerName(review.getReviewerName());
        dto.setReviewerEmail(review.getReviewerEmail());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setIsVerifiedBuyer(review.getIsVerifiedBuyer());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setTimeAgo(getTimeAgo(review.getCreatedAt()));
        
        List<String> imageUrls = review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList());
        dto.setImageUrls(imageUrls);
        
        return dto;
    }
    
    private String getTimeAgo(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        
        if (seconds < 60) return "Just now";
        if (seconds < 3600) return duration.toMinutes() + " minutes ago";
        if (seconds < 86400) return duration.toHours() + " hours ago";
        if (seconds < 604800) return duration.toDays() + " days ago";
        if (seconds < 2592000) return (duration.toDays() / 7) + " weeks ago";
        return (duration.toDays() / 30) + " months ago";
    }
}
