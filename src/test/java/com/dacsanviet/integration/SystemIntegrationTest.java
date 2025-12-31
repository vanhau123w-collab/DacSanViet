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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * System Integration Test to verify all news management components are properly wired
 * Tests the complete system integration including services, repositories, and business logic
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SystemIntegrationTest {

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
    private NewsCategoryDto testCategory;

    @BeforeEach
    void setUp() {
        // Create test admin user
        testAdmin = new User();
        testAdmin.setUsername("systemtestadmin");
        testAdmin.setEmail("systemtest@test.com");
        testAdmin.setPassword(passwordEncoder.encode("password"));
        testAdmin.setFullName("System Test Admin");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setIsActive(true);
        testAdmin = userRepository.save(testAdmin);

        // Create test category
        NewsCategoryDto categoryDto = new NewsCategoryDto();
        categoryDto.setName("System Test Category");
        categoryDto.setDescription("Category for system integration testing");
        categoryDto.setIsActive(true);
        testCategory = newsCategoryService.createCategory(categoryDto);
    }

    @Test
    void testCompleteSystemIntegration() {
        // Test 1: Verify all services are properly injected and functional
        assertThat(newsService).isNotNull();
        assertThat(newsCategoryService).isNotNull();
        assertThat(seoService).isNotNull();
        assertThat(userRepository).isNotNull();
        assertThat(passwordEncoder).isNotNull();

        // Test 2: Create and manage categories
        List<NewsCategoryDto> activeCategories = newsCategoryService.findActiveCategories();
        assertThat(activeCategories).isNotEmpty();
        assertThat(activeCategories.stream()
                .anyMatch(c -> c.getName().equals("System Test Category"))).isTrue();

        // Test 3: SEO Service integration
        String slug = seoService.generateUniqueSlug("Test Article Title with Special Characters!");
        assertThat(slug).isEqualTo("test-article-title-with-special-characters");

        SEOService.MetaDescriptionValidationResult validationResult = 
            seoService.validateMetaDescription("This is a valid meta description for testing purposes");
        assertThat(validationResult.isValid()).isTrue();

        // Test 4: Create comprehensive news article
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("System Integration Test Article");
        articleDto.setContent("This is a comprehensive test article to verify system integration. " +
                "It includes all necessary fields and tests the complete workflow from creation to publication.");
        articleDto.setExcerpt("System integration test article excerpt");
        articleDto.setAuthorId(testAdmin.getId());
        articleDto.setCategoryId(testCategory.getId());
        articleDto.setStatus(NewsStatus.DRAFT);
        articleDto.setMetaDescription("System integration test meta description");
        articleDto.setMetaKeywords("system, integration, test, news");
        articleDto.setIsFeatured(false);

        NewsArticleDto createdArticle = newsService.createArticle(articleDto);

        // Verify article creation
        assertThat(createdArticle).isNotNull();
        assertThat(createdArticle.getId()).isNotNull();
        assertThat(createdArticle.getSlug()).isEqualTo("system-integration-test-article");
        assertThat(createdArticle.getAuthorName()).isEqualTo("System Test Admin");
        assertThat(createdArticle.getCategoryName()).isEqualTo("System Test Category");

        // Test 5: Article lifecycle management
        // Publish article
        newsService.publishArticle(createdArticle.getId());
        var publishedArticle = newsService.findById(createdArticle.getId());
        assertThat(publishedArticle).isPresent();
        assertThat(publishedArticle.get().getStatus()).isEqualTo(NewsStatus.PUBLISHED);
        assertThat(publishedArticle.get().getPublishedAt()).isNotNull();

        // Toggle featured status
        newsService.toggleFeatured(createdArticle.getId());
        var featuredArticle = newsService.findById(createdArticle.getId());
        assertThat(featuredArticle).isPresent();
        assertThat(featuredArticle.get().getIsFeatured()).isTrue();

        // Increment view count
        Long initialViewCount = featuredArticle.get().getViewCount();
        newsService.incrementViewCount(createdArticle.getId());
        var viewedArticle = newsService.findById(createdArticle.getId());
        assertThat(viewedArticle).isPresent();
        assertThat(viewedArticle.get().getViewCount()).isEqualTo(initialViewCount + 1);

        // Test 6: Search and filtering functionality
        var searchResults = newsService.searchArticles("integration", 
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(searchResults.getContent()).isNotEmpty();
        assertThat(searchResults.getContent().stream()
                .anyMatch(a -> a.getId().equals(createdArticle.getId()))).isTrue();

        var categoryArticles = newsService.findByCategory(testCategory.getId(), 
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(categoryArticles.getContent()).isNotEmpty();
        assertThat(categoryArticles.getContent().stream()
                .anyMatch(a -> a.getId().equals(createdArticle.getId()))).isTrue();

        // Test 7: Featured articles functionality
        List<NewsArticleDto> featuredArticles = newsService.findFeaturedArticles(10);
        assertThat(featuredArticles.stream()
                .anyMatch(a -> a.getId().equals(createdArticle.getId()))).isTrue();

        // Test 8: Analytics and statistics
        Map<String, Long> statusStats = newsService.getArticleStatsByStatus();
        assertThat(statusStats).isNotEmpty();
        assertThat(statusStats.get("PUBLISHED")).isGreaterThan(0);

        List<NewsArticleDto> mostViewed = newsService.findMostViewedArticles(10);
        assertThat(mostViewed).isNotEmpty();

        Map<String, Long> viewStats = newsService.getViewStatsByMonth(6);
        assertThat(viewStats).isNotNull();

        // Test 9: Article update functionality
        createdArticle.setTitle("Updated System Integration Test Article");
        createdArticle.setContent("Updated content for system integration testing");
        
        NewsArticleDto updatedArticle = newsService.updateArticle(createdArticle.getId(), createdArticle);
        assertThat(updatedArticle.getTitle()).isEqualTo("Updated System Integration Test Article");
        assertThat(updatedArticle.getSlug()).isEqualTo("updated-system-integration-test-article");
        assertThat(updatedArticle.getUpdatedAt()).isAfter(createdArticle.getUpdatedAt());

        // Test 10: Soft delete functionality
        newsService.deleteArticle(createdArticle.getId());
        var deletedArticle = newsService.findById(createdArticle.getId());
        assertThat(deletedArticle).isPresent();
        assertThat(deletedArticle.get().getStatus()).isEqualTo(NewsStatus.ARCHIVED);

        // Test 11: Category management integration
        testCategory.setName("Updated System Test Category");
        NewsCategoryDto updatedCategory = newsCategoryService.updateCategory(testCategory.getId(), testCategory);
        assertThat(updatedCategory.getName()).isEqualTo("Updated System Test Category");
        assertThat(updatedCategory.getSlug()).isEqualTo("updated-system-test-category");

        // Test 12: Verify data consistency across services
        var categoriesWithArticles = newsCategoryService.findCategoriesWithPublishedArticles();
        // Note: The article is archived now, so it might not appear in published articles
        
        var allCategories = newsCategoryService.findActiveCategories();
        assertThat(allCategories.stream()
                .anyMatch(c -> c.getId().equals(testCategory.getId()))).isTrue();
    }

    @Test
    void testErrorHandlingAndValidation() {
        // Test invalid article creation
        NewsArticleDto invalidArticle = new NewsArticleDto();
        invalidArticle.setTitle(""); // Empty title should fail
        invalidArticle.setAuthorId(testAdmin.getId());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            newsService.createArticle(invalidArticle);
        });

        // Test non-existent author
        NewsArticleDto articleWithInvalidAuthor = new NewsArticleDto();
        articleWithInvalidAuthor.setTitle("Test Article");
        articleWithInvalidAuthor.setContent("Test content");
        articleWithInvalidAuthor.setAuthorId(99999L); // Non-existent

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            newsService.createArticle(articleWithInvalidAuthor);
        });

        // Test invalid meta description
        SEOService.MetaDescriptionValidationResult invalidMeta = 
            seoService.validateMetaDescription("A".repeat(200)); // Too long
        assertThat(invalidMeta.isValid()).isFalse();
        assertThat(invalidMeta.getMessages()).isNotEmpty();

        // Test duplicate category name
        NewsCategoryDto duplicateCategory = new NewsCategoryDto();
        duplicateCategory.setName("System Test Category"); // Already exists
        duplicateCategory.setDescription("Duplicate category");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            newsCategoryService.createCategory(duplicateCategory);
        });
    }

    @Test
    void testPerformanceAndScalability() {
        // Create multiple articles to test pagination and performance
        for (int i = 1; i <= 25; i++) {
            NewsArticleDto articleDto = new NewsArticleDto();
            articleDto.setTitle("Performance Test Article " + i);
            articleDto.setContent("Content for performance test article " + i);
            articleDto.setExcerpt("Excerpt " + i);
            articleDto.setAuthorId(testAdmin.getId());
            articleDto.setCategoryId(testCategory.getId());
            articleDto.setStatus(NewsStatus.PUBLISHED);

            newsService.createArticle(articleDto);
        }

        // Test pagination performance
        var page1 = newsService.findPublishedArticles(
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page1.getTotalElements()).isGreaterThanOrEqualTo(25);

        var page2 = newsService.findPublishedArticles(
                org.springframework.data.domain.PageRequest.of(1, 10));
        assertThat(page2.getContent()).hasSize(10);

        var page3 = newsService.findPublishedArticles(
                org.springframework.data.domain.PageRequest.of(2, 10));
        assertThat(page3.getContent()).hasSizeGreaterThanOrEqualTo(5);

        // Test search performance with multiple articles
        var searchResults = newsService.searchArticles("Performance", 
                org.springframework.data.domain.PageRequest.of(0, 20));
        assertThat(searchResults.getContent()).hasSizeGreaterThanOrEqualTo(20);

        // Test analytics with multiple articles
        Map<String, Long> statusStats = newsService.getArticleStatsByStatus();
        assertThat(statusStats.get("PUBLISHED")).isGreaterThanOrEqualTo(25);

        List<NewsArticleDto> recentArticles = newsService.findRecentArticles(15);
        assertThat(recentArticles).hasSize(15);
    }

    @Test
    void testConcurrentOperations() {
        // Test slug generation uniqueness under concurrent-like conditions
        String baseTitle = "Concurrent Test Article";
        
        NewsArticleDto article1 = new NewsArticleDto();
        article1.setTitle(baseTitle);
        article1.setContent("Content 1");
        article1.setAuthorId(testAdmin.getId());
        article1.setCategoryId(testCategory.getId());
        
        NewsArticleDto article2 = new NewsArticleDto();
        article2.setTitle(baseTitle);
        article2.setContent("Content 2");
        article2.setAuthorId(testAdmin.getId());
        article2.setCategoryId(testCategory.getId());

        NewsArticleDto created1 = newsService.createArticle(article1);
        NewsArticleDto created2 = newsService.createArticle(article2);

        // Verify unique slugs were generated
        assertThat(created1.getSlug()).isNotEqualTo(created2.getSlug());
        assertThat(created1.getSlug()).isEqualTo("concurrent-test-article");
        assertThat(created2.getSlug()).startsWith("concurrent-test-article-");

        // Test view count increments
        Long initialCount1 = created1.getViewCount();
        Long initialCount2 = created2.getViewCount();

        newsService.incrementViewCount(created1.getId());
        newsService.incrementViewCount(created2.getId());
        newsService.incrementViewCount(created1.getId());

        var updated1 = newsService.findById(created1.getId()).get();
        var updated2 = newsService.findById(created2.getId()).get();

        assertThat(updated1.getViewCount()).isEqualTo(initialCount1 + 2);
        assertThat(updated2.getViewCount()).isEqualTo(initialCount2 + 1);
    }
}