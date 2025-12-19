package com.dacsanviet.repository;

import com.dacsanviet.model.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    
    // Find reviews by product
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    Page<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    
    // Get average rating for product
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId")
    Double getAverageRating(@Param("productId") Long productId);
    
    // Count reviews by product
    Long countByProductId(Long productId);
    
    // Count reviews by rating
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.rating = :rating")
    Long countByProductIdAndRating(@Param("productId") Long productId, @Param("rating") Integer rating);
    
    // Check if user has purchased product (verified buyer)
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.user.id = :userId AND oi.order.status = 'DELIVERED'")
    Boolean isVerifiedBuyer(@Param("productId") Long productId, @Param("userId") Long userId);
}
