package com.dacsanviet.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dacsanviet.model.Promotion;
import com.dacsanviet.repository.PromotionRepository;

/**
 * Admin Promotion Controller
 */
@Controller
@RequestMapping("/admin/promotions")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminPromotionController {

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * Show promotions management page
     */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("activePage", "promotions");
        return "admin/promotions/index";
    }

    /**
     * Get promotions with pagination (AJAX)
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Page<Promotion>> getPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Promotion> promotions;

        if (search != null && !search.trim().isEmpty()) {
            promotions = promotionRepository.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        } else if ("active".equals(status)) {
            promotions = promotionRepository.findByIsActive(true, pageable);
        } else if ("inactive".equals(status)) {
            promotions = promotionRepository.findByIsActive(false, pageable);
        } else {
            promotions = promotionRepository.findAll(pageable);
        }

        return ResponseEntity.ok(promotions);
    }

    /**
     * Get promotion by ID (AJAX)
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getPromotion(@PathVariable Long id) {
        return promotionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create promotion (AJAX)
     */
    @PostMapping("/api/create")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> createPromotion(@RequestBody Map<String, Object> requestData) {
        try {
            // Check if code already exists
            String code = (String) requestData.get("code");
            if (promotionRepository.existsByCode(code)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Mã khuyến mãi đã tồn tại"));
            }

            Promotion promotion = new Promotion();
            promotion.setCode(code);
            promotion.setDescription((String) requestData.get("description"));
            promotion.setDiscountType(Promotion.DiscountType.valueOf((String) requestData.get("discountType")));
            promotion.setDiscountValue(new java.math.BigDecimal(requestData.get("discountValue").toString()));
            
            if (requestData.get("maxDiscountAmount") != null) {
                promotion.setMaxDiscountAmount(new java.math.BigDecimal(requestData.get("maxDiscountAmount").toString()));
            }
            if (requestData.get("minOrderValue") != null) {
                promotion.setMinOrderValue(new java.math.BigDecimal(requestData.get("minOrderValue").toString()));
            }
            if (requestData.get("usageLimit") != null) {
                promotion.setUsageLimit(((Number) requestData.get("usageLimit")).intValue());
            }
            
            // Parse datetime as local time (not UTC)
            promotion.setStartDate(java.time.LocalDateTime.parse((String) requestData.get("startDate")));
            promotion.setEndDate(java.time.LocalDateTime.parse((String) requestData.get("endDate")));
            promotion.setIsActive((Boolean) requestData.get("isActive"));

            Promotion saved = promotionRepository.save(promotion);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update promotion (AJAX)
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updatePromotion(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            return promotionRepository.findById(id)
                    .map(existing -> {
                        String code = (String) requestData.get("code");
                        // Check if code is being changed and already exists
                        if (!existing.getCode().equals(code) && promotionRepository.existsByCode(code)) {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("error", "Mã khuyến mãi đã tồn tại"));
                        }

                        existing.setCode(code);
                        existing.setDescription((String) requestData.get("description"));
                        existing.setDiscountType(Promotion.DiscountType.valueOf((String) requestData.get("discountType")));
                        existing.setDiscountValue(new java.math.BigDecimal(requestData.get("discountValue").toString()));
                        
                        if (requestData.get("maxDiscountAmount") != null) {
                            existing.setMaxDiscountAmount(new java.math.BigDecimal(requestData.get("maxDiscountAmount").toString()));
                        } else {
                            existing.setMaxDiscountAmount(null);
                        }
                        if (requestData.get("minOrderValue") != null) {
                            existing.setMinOrderValue(new java.math.BigDecimal(requestData.get("minOrderValue").toString()));
                        }
                        if (requestData.get("usageLimit") != null) {
                            existing.setUsageLimit(((Number) requestData.get("usageLimit")).intValue());
                        } else {
                            existing.setUsageLimit(null);
                        }
                        
                        // Parse datetime as local time
                        existing.setStartDate(java.time.LocalDateTime.parse((String) requestData.get("startDate")));
                        existing.setEndDate(java.time.LocalDateTime.parse((String) requestData.get("endDate")));
                        existing.setIsActive((Boolean) requestData.get("isActive"));

                        Promotion updated = promotionRepository.save(existing);
                        return ResponseEntity.ok(updated);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete promotion (AJAX) - Only ADMIN
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePromotion(@PathVariable Long id) {
        try {
            if (!promotionRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            promotionRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa khuyến mãi thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Toggle promotion status (AJAX)
     */
    @PutMapping("/api/{id}/toggle")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            return promotionRepository.findById(id)
                    .map(promotion -> {
                        promotion.setIsActive(!promotion.getIsActive());
                        Promotion updated = promotionRepository.save(promotion);
                        return ResponseEntity.ok(updated);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
