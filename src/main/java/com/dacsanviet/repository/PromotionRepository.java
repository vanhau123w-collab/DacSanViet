package com.dacsanviet.repository;

import com.dacsanviet.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    Optional<Promotion> findByCode(String code);
    
    boolean existsByCode(String code);
    
    Page<Promotion> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<Promotion> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String code, String description, Pageable pageable);
    
    @Query("SELECT p FROM Promotion p WHERE p.code = :code AND p.isActive = true " +
           "AND :now BETWEEN p.startDate AND p.endDate " +
           "AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    Optional<Promotion> findValidPromotionByCode(@Param("code") String code, @Param("now") LocalDateTime now);
}
