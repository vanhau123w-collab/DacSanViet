package com.dacsanviet.integration;

import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.dto.NewsCategoryDto;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.model.Role;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.service.NewsCategoryService;
import com.dacsanviet.service.NewsService;
import com.dacsanviet.service.SEOService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Complete workflow integration test for News Management System
 * Tests the entire workflow from category creation to article management and analytics
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CompleteWorkflowTest {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsCategoryService newsCategoryService;

    @Autowired
    private SEOService seoService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testAdmin;

    @BeforeEach
    void setUp() {
        // Create test admin user
        testAdmin = new User();
        testAdmin.setUsername("workflowtest");
        testAdmin.setEmail("workflow@test.com");
        testAdmin.setPassword(passwordEncoder.encode("password"));
        testAdmin.setFullName("Workflow Test Admin");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setIsActive(true);
        testAdmin = userRepository.save(testAdmin);
    }

    @Test
    void testCompleteNewsManagementWorkflow() {
        // Step 1: Create news categories
        NewsCategoryDto category1 = createCategory("Tin Tức", "Tin tức tổng hợp");
        NewsCategoryDto category2 = createCategory("Khuyến Mãi", "Thông tin khuyến mãi");
        NewsCategoryDto category3 = createCategory("Sự Kiện", "Các sự kiện đặc biệt");

        assertThat(category1.getId()).isNotNull();
        assertThat(category2.getId()).isNotNull();
        assertThat(category3.getId()).isNotNull();

        // Step 2: Create multiple articles in different categories
        NewsArticleDto article1 = createArticle("Tin Tức Mới Nhất Về Đặc Sản Việt", category1.getId(), true);
        NewsArticleDto article2 = createArticle("Chương Trình Khuyến Mãi Tháng 12", category2.getId(), true);
        NewsArticleDto article3 = createArticle("Sự Kiện Ẩm Thực Cuối Năm", category3.getId(), false);
        NewsArticleDto article4 = createArticle("Hướng Dẫn Chọn Đặc Sản Ngon", category1.getId(), false);
        NewsArticleDto article5 = createArticle("Giảm Giá Đặc Biệt Cuối Năm", category2.getId(), true);

        // Verify articles were created correctly
        assertThat(article1.getId()).isNotNull();
        assertThat(article2.getId()).isNotNull();
        assertThat(article3.getId()).isNotNull();
        assertThat(article4.getId()).isNotNull();
        assertThat(article5.getId()).isNotNull();

        // Step 3: Test article lifecycle management
        // Publish articles
        newsService.publishArticle(article1.getId());
        newsService.publishArticle(article2.getId());
        newsService.publishArticle(article3.getId());
        newsService.publishArticle(article4.getId());
        // Keep article5 as draft

        // Verify published status
        var publishedArticle1 = newsService.findById(article1.getId()).get();
        assertThat(publishedArticle1.getStatus()).isEqualTo(NewsStatus.PUBLISHED);
        assertThat(publishedArticle1.getPublishedAt()).isNotNull();

        // Step 4: Test search and filtering functionality
        // Search by keyword
        Page<NewsArticleDto> searchResults = newsService.searchArticles("đặc sản", PageRequest.of(0, 10));
        assertThat(searchResults.getContent()).isNotEmpty();
        assertThat(searchResults.getContent().stream()
                .anyMatch(a -> a.getTitle().toLowerCase().contains("đặc sản"))).isTrue();

        // Filter by category
        Page<NewsArticleDto> categoryResults = newsService.findByCategory(category1.getId(), PageRequest.of(0, 10));
        assertThat(categoryResults.getContent()).isNotEmpty();
        assertThat(categoryResults.getContent().stream()
                .allMatch(a -> a.getCategoryId().equals(category1.getId()))).isTrue();

        // Step 5: Test featured articles functionality
        List<NewsArticleDto> featuredArticles = newsService.findFeaturedArticles(10);
        assertThat(featuredArticles).hasSize(3); // article1, article2, article5 are featured
        assertThat(featuredArticles.stream()
                .allMatch(NewsArticleDto::getIsFeatured)).isTrue();

        // Step 6: Test view count tracking
        Long initialViewCount = publishedArticle1.getViewCount();
        newsService.incrementViewCount(article1.getId());
        newsService.incrementViewCount(article1.getId());
        newsService.incrementViewCount(article1.getId());

        var updatedArticle = newsService.findById(article1.getId()).get();
        assertThat(updatedArticle.getViewCount()).isEqualTo(initialViewCount + 3);

        // Step 7: Test analytics functionality
        Map<String, Long> statusStats = newsService.getArticleStatsByStatus();
        assertThat(statusStats).isNotEmpty();
        assertThat(statusStats.get("PUBLISHED")).isEqualTo(4L);
        assertThat(statusStats.get("DRAFT")).isEqualTo(1L);

        List<NewsArticleDto> mostViewed = newsService.findMostViewedArticles(5);
        assertThat(mostViewed).isNotEmpty();
        // The article we incremented should be at the top
        assertThat(mostViewed.get(0).getId()).isEqualTo(article1.getId());

        // Step 8: Test article updates
        article1.setTitle("Tin Tức Cập Nhật Về Đặc Sản Việt Nam");
        article1.setContent("Nội dung đã được cập nhật với thông tin mới nhất");
        
        NewsArticleDto updatedArticleDto = newsService.updateArticle(article1.getId(), article1);
        assertThat(updatedArticleDto.getTitle()).isEqualTo("Tin Tức Cập Nhật Về Đặc Sản Việt Nam");
        assertThat(updatedArticleDto.getSlug()).isEqualTo("tin-tuc-cap-nhat-ve-dac-san-viet-nam");

        // Step 9: Test category management
        List<NewsCategoryDto> activeCategories = newsCategoryService.findActiveCategories();
        assertThat(activeCategories).hasSize(3);

        // Update category
        category1.setName("Tin Tức Tổng Hợp");
        NewsCategoryDto updatedCategory = newsCategoryService.updateCategory(category1.getId(), category1);
        assertThat(updatedCategory.getName()).isEqualTo("Tin Tức Tổng Hợp");

        // Step 10: Test soft delete functionality
        newsService.deleteArticle(article5.getId());
        var deletedArticle = newsService.findById(article5.getId()).get();
        assertThat(deletedArticle.getStatus()).isEqualTo(NewsStatus.ARCHIVED);

        // Verify archived articles don't appear in published results
        Page<NewsArticleDto> publishedArticles = newsService.findPublishedArticles(PageRequest.of(0, 10));
        assertThat(publishedArticles.getContent().stream()
                .noneMatch(a -> a.getId().equals(article5.getId()))).isTrue();

        // Step 11: Test pagination
        Page<NewsArticleDto> page1 = newsService.findPublishedArticles(PageRequest.of(0, 2));
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(4); // 4 published articles

        Page<NewsArticleDto> page2 = newsService.findPublishedArticles(PageRequest.of(1, 2));
        assertThat(page2.getContent()).hasSize(2);

        // Step 12: Test SEO functionality
        String slug1 = seoService.generateUniqueSlug("Test Article Title");
        String slug2 = seoService.generateUniqueSlug("Test Article Title");
        assertThat(slug1).isNotEqualTo(slug2);
        assertThat(slug1).isEqualTo("test-article-title");
        assertThat(slug2).startsWith("test-article-title-");

        // Test meta description validation
        SEOService.MetaDescriptionValidationResult validResult = 
            seoService.validateMetaDescription("This is a valid meta description for testing");
        assertThat(validResult.isValid()).isTrue();

        SEOService.MetaDescriptionValidationResult invalidResult = 
            seoService.validateMetaDescription("A".repeat(200)); // Too long
        assertThat(invalidResult.isValid()).isFalse();

        // Step 13: Test recent articles
        List<NewsArticleDto> recentArticles = newsService.findRecentArticles(3);
        assertThat(recentArticles).hasSize(3);
        // Should be ordered by published date desc
        for (int i = 0; i < recentArticles.size() - 1; i++) {
            assertThat(recentArticles.get(i).getPublishedAt())
                .isAfterOrEqualTo(recentArticles.get(i + 1).getPublishedAt());
        }

        // Step 14: Test admin operations
        Page<NewsArticleDto> allArticlesForAdmin = newsService.findAllForAdmin(null, PageRequest.of(0, 10));
        assertThat(allArticlesForAdmin.getTotalElements()).isEqualTo(5); // All articles including archived

        Page<NewsArticleDto> draftArticlesForAdmin = newsService.findAllForAdmin(NewsStatus.DRAFT, PageRequest.of(0, 10));
        assertThat(draftArticlesForAdmin.getTotalElements()).isEqualTo(0); // No drafts left

        Page<NewsArticleDto> archivedArticlesForAdmin = newsService.findAllForAdmin(NewsStatus.ARCHIVED, PageRequest.of(0, 10));
        assertThat(archivedArticlesForAdmin.getTotalElements()).isEqualTo(1); // One archived article

        // Step 15: Verify data consistency
        // Check that category article counts are correct
        List<NewsCategoryDto> categoriesWithCounts = newsCategoryService.findActiveCategoriesWithArticleCount();
        NewsCategoryDto categoryWithCount = categoriesWithCounts.stream()
            .filter(c -> c.getId().equals(category1.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(categoryWithCount.getArticleCount()).isEqualTo(2L); // 2 published articles in category1

        log.info("Complete workflow test passed successfully!");
    }

    private NewsCategoryDto createCategory(String name, String description) {
        NewsCategoryDto categoryDto = new NewsCategoryDto();
        categoryDto.setName(name);
        categoryDto.setDescription(description);
        categoryDto.setIsActive(true);
        return newsCategoryService.createCategory(categoryDto);
    }

    private NewsArticleDto createArticle(String title, Long categoryId, boolean isFeatured) {
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle(title);
        articleDto.setContent("Nội dung chi tiết của bài viết " + title + ". " +
                "Đây là một bài viết test với nội dung đầy đủ để kiểm tra chức năng của hệ thống.");
        articleDto.setExcerpt("Tóm tắt ngắn gọn về " + title);
        articleDto.setAuthorId(testAdmin.getId());
        articleDto.setCategoryId(categoryId);
        articleDto.setStatus(NewsStatus.DRAFT);
        articleDto.setIsFeatured(isFeatured);
        articleDto.setMetaDescription("Meta description cho " + title);
        articleDto.setMetaKeywords("test, news, article, đặc sản");
        return newsService.createArticle(articleDto);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompleteWorkflowTest.class);
}