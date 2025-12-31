package com.dacsanviet.controller;

import com.dacsanviet.dto.NewsCommentDto;
import com.dacsanviet.model.User;
import com.dacsanviet.service.NewsCommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for handling news comment operations
 */
@Controller
@RequestMapping("/news/comments")
@RequiredArgsConstructor
@Slf4j
public class NewsCommentController {
    
    private final NewsCommentService newsCommentService;
    
    /**
     * Submit a new comment (both user and guest comments)
     */
    @PostMapping("/submit")
    public String submitComment(@Valid @ModelAttribute NewsCommentDto commentDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request) {
        
        log.info("Submitting comment for article id: {}", commentDto.getArticleId());
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("commentError", "Vui lòng kiểm tra lại thông tin bình luận.");
            redirectAttributes.addFlashAttribute("commentDto", commentDto);
            return "redirect:" + request.getHeader("Referer");
        }
        
        try {
            // Check if user is authenticated
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                    !auth.getName().equals("anonymousUser");
            
            if (isAuthenticated) {
                // User comment
                User user = (User) auth.getPrincipal();
                commentDto.setUserId(user.getId());
                commentDto.setGuestName(null);
                commentDto.setGuestEmail(null);
                newsCommentService.createUserComment(commentDto);
                log.info("Created user comment for user: {}", user.getEmail());
            } else {
                // Guest comment - validate guest fields
                if (commentDto.getGuestName() == null || commentDto.getGuestName().trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("commentError", "Vui lòng nhập tên của bạn.");
                    redirectAttributes.addFlashAttribute("commentDto", commentDto);
                    return "redirect:" + request.getHeader("Referer");
                }
                if (commentDto.getGuestEmail() == null || commentDto.getGuestEmail().trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("commentError", "Vui lòng nhập email của bạn.");
                    redirectAttributes.addFlashAttribute("commentDto", commentDto);
                    return "redirect:" + request.getHeader("Referer");
                }
                
                commentDto.setUserId(null);
                newsCommentService.createGuestComment(commentDto);
                log.info("Created guest comment for: {}", commentDto.getGuestName());
            }
            
            redirectAttributes.addFlashAttribute("commentSuccess", 
                "Bình luận của bạn đã được gửi và đang chờ duyệt. Cảm ơn bạn!");
            
        } catch (Exception e) {
            log.error("Error submitting comment: ", e);
            redirectAttributes.addFlashAttribute("commentError", 
                "Có lỗi xảy ra khi gửi bình luận. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("commentDto", commentDto);
        }
        
        return "redirect:" + request.getHeader("Referer");
    }
    
    /**
     * Get comments for an article (AJAX endpoint)
     */
    @GetMapping("/article/{articleId}")
    @ResponseBody
    public ResponseEntity<List<NewsCommentDto>> getArticleComments(@PathVariable Long articleId) {
        try {
            List<NewsCommentDto> comments = newsCommentService.findApprovedCommentsByArticle(articleId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error fetching comments for article {}: ", articleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get paginated comments for an article
     */
    @GetMapping("/article/{articleId}/page")
    @ResponseBody
    public ResponseEntity<Page<NewsCommentDto>> getArticleCommentsPaginated(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<NewsCommentDto> comments = newsCommentService.findApprovedCommentsByArticle(articleId, pageable);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error fetching paginated comments for article {}: ", articleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}