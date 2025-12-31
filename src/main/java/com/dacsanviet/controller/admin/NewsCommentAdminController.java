package com.dacsanviet.controller.admin;

import com.dacsanviet.dto.NewsCommentDto;
import com.dacsanviet.model.CommentStatus;
import com.dacsanviet.service.NewsCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin controller for managing news comments
 */
@Controller
@RequestMapping("/admin/comments")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
@Slf4j
public class NewsCommentAdminController {
    
    private final NewsCommentService newsCommentService;
    
    /**
     * Display comment management page
     */
    @GetMapping
    public String listComments(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(required = false) CommentStatus status,
                              @RequestParam(required = false) String search) {
        
        log.info("Loading comment management page - page: {}, size: {}, status: {}, search: {}", 
                page, size, status, search);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NewsCommentDto> comments;
        
        try {
            if (search != null && !search.trim().isEmpty()) {
                comments = newsCommentService.searchComments(search.trim(), pageable);
                model.addAttribute("search", search);
            } else if (status != null) {
                comments = newsCommentService.findCommentsByStatus(status, pageable);
                model.addAttribute("selectedStatus", status);
            } else {
                // Show pending comments by default
                comments = newsCommentService.findPendingComments(pageable);
                model.addAttribute("selectedStatus", CommentStatus.PENDING);
            }
            
            model.addAttribute("comments", comments);
            model.addAttribute("statuses", CommentStatus.values());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", comments.getTotalPages());
            model.addAttribute("totalElements", comments.getTotalElements());
            
            return "admin/comments/list";
            
        } catch (Exception e) {
            log.error("Error loading comments: ", e);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách bình luận.");
            return "admin/comments/list";
        }
    }
    
    /**
     * Approve a comment
     */
    @PostMapping("/{id}/approve")
    public String approveComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsCommentService.approveComment(id);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt bình luận thành công.");
            log.info("Approved comment with id: {}", id);
        } catch (Exception e) {
            log.error("Error approving comment {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi duyệt bình luận.");
        }
        return "redirect:/admin/comments";
    }
    
    /**
     * Reject a comment
     */
    @PostMapping("/{id}/reject")
    public String rejectComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsCommentService.rejectComment(id);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối bình luận thành công.");
            log.info("Rejected comment with id: {}", id);
        } catch (Exception e) {
            log.error("Error rejecting comment {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi từ chối bình luận.");
        }
        return "redirect:/admin/comments";
    }
    
    /**
     * Delete a comment
     */
    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsCommentService.deleteComment(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa bình luận thành công.");
            log.info("Deleted comment with id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting comment {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa bình luận.");
        }
        return "redirect:/admin/comments";
    }
    
    /**
     * Bulk approve comments
     */
    @PostMapping("/bulk-approve")
    public String bulkApproveComments(@RequestParam("commentIds") Long[] commentIds, 
                                    RedirectAttributes redirectAttributes) {
        try {
            int approvedCount = 0;
            for (Long id : commentIds) {
                try {
                    newsCommentService.approveComment(id);
                    approvedCount++;
                } catch (Exception e) {
                    log.warn("Failed to approve comment {}: ", id, e);
                }
            }
            redirectAttributes.addFlashAttribute("success", 
                String.format("Đã duyệt %d/%d bình luận thành công.", approvedCount, commentIds.length));
            log.info("Bulk approved {} comments", approvedCount);
        } catch (Exception e) {
            log.error("Error in bulk approve: ", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi duyệt hàng loạt.");
        }
        return "redirect:/admin/comments";
    }
    
    /**
     * Bulk reject comments
     */
    @PostMapping("/bulk-reject")
    public String bulkRejectComments(@RequestParam("commentIds") Long[] commentIds, 
                                   RedirectAttributes redirectAttributes) {
        try {
            int rejectedCount = 0;
            for (Long id : commentIds) {
                try {
                    newsCommentService.rejectComment(id);
                    rejectedCount++;
                } catch (Exception e) {
                    log.warn("Failed to reject comment {}: ", id, e);
                }
            }
            redirectAttributes.addFlashAttribute("success", 
                String.format("Đã từ chối %d/%d bình luận thành công.", rejectedCount, commentIds.length));
            log.info("Bulk rejected {} comments", rejectedCount);
        } catch (Exception e) {
            log.error("Error in bulk reject: ", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi từ chối hàng loạt.");
        }
        return "redirect:/admin/comments";
    }
    
    /**
     * AJAX endpoint for comment moderation
     */
    @PostMapping("/api/{id}/moderate")
    @ResponseBody
    public ResponseEntity<String> moderateComment(@PathVariable Long id, 
                                                @RequestParam CommentStatus status) {
        try {
            switch (status) {
                case APPROVED:
                    newsCommentService.approveComment(id);
                    break;
                case REJECTED:
                    newsCommentService.rejectComment(id);
                    break;
                case PENDING:
                    newsCommentService.setPendingComment(id);
                    break;
                default:
                    return ResponseEntity.badRequest().body("Invalid status");
            }
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error("Error moderating comment {}: ", id, e);
            return ResponseEntity.internalServerError().body("Error moderating comment");
        }
    }
}