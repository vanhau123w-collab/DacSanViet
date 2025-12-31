package com.dacsanviet.service;

import com.dacsanviet.model.NewsArticle;
import com.dacsanviet.model.NewsCategory;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.NewsArticleRepository;
import com.dacsanviet.repository.NewsCategoryRepository;
import com.dacsanviet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for News Analytics functionality
 * Requirements: 6.1, 6.2, 6.3, 6.5
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NewsAnalyticsTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private NewsCategoryRepository newsCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User testAuthor;
    private NewsCategory testCategory;

    @BeforeEach
    void setUp() {
        // Create test author
        testAuthor = new User();
        testAuthor.setUsername("test-author");
        testAuthor.setEmail("author@test.com");
        testAuthor.setPassword("password");
        testAuthor = userRepository.save(testAuthor);

        // Create test category
        testCategory = new NewsCategory();
        testCategory.setName("Test Category");
        testCategory.setSlug("test-category");
        testCategory.setIsActive(true);
        testCategory = newsCategoryRepository.save(testCategory);
    }

    @Test
    void testNewsAnalyticsBasicStats() {
        // Get initial counts
        Map<String, Object> initialAnalytics = dashboardService.getNewsAnalytics("30days");
        Long initialPublished = (Long) initialAnalytics.get("totalPublishedArticles");
        Long initialViews = (Long) initialAnalytics.get("totalViews");
        
        // Create test articles with different statuses and view counts
        createTestArticle("Published Article 1", NewsStatus.PUBLISHED, 100L);
        createTestArticle("Published Article 2", NewsStatus.PUBLISHED, 50L);
        createTestArticle("Draft Article", NewsStatus.DRAFT, 0L);

        // Get analytics after adding articles
        Map<String, Object> analytics = dashboardService.getNewsAnalytics("30days");

        // Verify basic statistics
        assertNotNull(analytics);
        assertTrue(analytics.containsKey("totalPublishedArticles"));
        assertTrue(analytics.containsKey("totalViews"));
        assertTrue(analytics.containsKey("articlesByStatus"));

        // Check published articles count (should increase by 2)
        Long publishedCount = (Long) analytics.get("totalPublishedArticles");
        assertEquals(initialPublished + 2L, publishedCount);

        // Check total views (should increase by 150)
        Long totalViews = (Long) analytics.get("totalViews");
        assertEquals(initialViews + 150L, totalViews);
    }

    @Test
    void testMostViewedArticles() {
        // Create articles with different view counts
        NewsArticle article1 = createTestArticle("High Views Article", NewsStatus.PUBLISHED, 1000L);
        NewsArticle article2 = createTestArticle("Medium Views Article", NewsStatus.PUBLISHED, 500L);
        NewsArticle article3 = createTestArticle("Low Views Article", NewsStatus.PUBLISHED, 100L);

        // Get analytics
        Map<String, Object> analytics = dashboardService.getNewsAnalytics("30days");

        // Verify most viewed articles
        assertNotNull(analytics.get("mostViewedArticles"));
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> mostViewed = 
            (java.util.List<Map<String, Object>>) analytics.get("mostViewedArticles");

        assertFalse(mostViewed.isEmpty());
        
        // Check that articles are ordered by view count (descending)
        if (mostViewed.size() >= 2) {
            Long firstViews = (Long) mostViewed.get(0).get("viewCount");
            Long secondViews = (Long) mostViewed.get(1).get("viewCount");
            assertTrue(firstViews >= secondViews);
        }
    }

    @Test
    void testArticlesByCategory() {
        // Create articles in the test category
        createTestArticle("Category Article 1", NewsStatus.PUBLISHED, 10L);
        createTestArticle("Category Article 2", NewsStatus.PUBLISHED, 20L);

        // Get analytics
        Map<String, Object> analytics = dashboardService.getNewsAnalytics("30days");

        // Verify category statistics
        assertNotNull(analytics.get("articlesByCategory"));
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> categoryStats = 
            (java.util.List<Map<String, Object>>) analytics.get("articlesByCategory");

        // Should have at least one category with articles
        assertFalse(categoryStats.isEmpty());
        
        // Find our test category
        boolean foundTestCategory = categoryStats.stream()
            .anyMatch(cat -> "Test Category".equals(cat.get("categoryName")));
        assertTrue(foundTestCategory);
    }

    @Test
    void testViewsChartData() {
        // Create some test articles
        createTestArticle("Chart Test Article", NewsStatus.PUBLISHED, 100L);

        // Get chart data
        Map<String, Object> chartData = dashboardService.getNewsViewsChartData(6);

        // Verify chart data structure
        assertNotNull(chartData);
        assertTrue(chartData.containsKey("labels"));
        assertTrue(chartData.containsKey("data"));

        @SuppressWarnings("unchecked")
        java.util.List<String> labels = (java.util.List<String>) chartData.get("labels");
        @SuppressWarnings("unchecked")
        java.util.List<Long> data = (java.util.List<Long>) chartData.get("data");

        assertNotNull(labels);
        assertNotNull(data);
        assertEquals(labels.size(), data.size());
    }

    @Test
    void testTopCategoriesByArticleCount() {
        // Create articles in different categories
        createTestArticle("Test Article 1", NewsStatus.PUBLISHED, 10L);
        createTestArticle("Test Article 2", NewsStatus.PUBLISHED, 20L);

        // Get top categories
        java.util.List<Map<String, Object>> topCategories = 
            dashboardService.getTopCategoriesByArticleCount(5);

        // Verify results
        assertNotNull(topCategories);
        
        if (!topCategories.isEmpty()) {
            Map<String, Object> firstCategory = topCategories.get(0);
            assertTrue(firstCategory.containsKey("name"));
            assertTrue(firstCategory.containsKey("articleCount"));
        }
    }

    private NewsArticle createTestArticle(String title, NewsStatus status, Long viewCount) {
        NewsArticle article = new NewsArticle();
        article.setTitle(title);
        article.setSlug(title.toLowerCase().replaceAll("\\s+", "-"));
        article.setContent("Test content for " + title);
        article.setExcerpt("Test excerpt");
        article.setStatus(status);
        article.setAuthor(testAuthor);
        article.setCategory(testCategory);
        article.setViewCount(viewCount);
        article.setIsFeatured(false);
        
        if (status == NewsStatus.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }
        
        return newsArticleRepository.save(article);
    }
}