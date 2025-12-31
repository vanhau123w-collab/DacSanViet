package com.dacsanviet.controller;

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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for News Analytics endpoints
 * Requirements: 6.1, 6.2, 6.3, 6.5
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NewsAnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

        // Create test articles
        createTestArticle("Test Article 1", NewsStatus.PUBLISHED, 100L);
        createTestArticle("Test Article 2", NewsStatus.PUBLISHED, 200L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testNewsAnalyticsEndpoint() throws Exception {
        mockMvc.perform(get("/admin/api/news/analytics")
                .param("period", "30days"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalPublishedArticles").exists())
                .andExpect(jsonPath("$.totalViews").exists())
                .andExpect(jsonPath("$.articlesByStatus").exists())
                .andExpect(jsonPath("$.mostViewedArticles").exists())
                .andExpect(jsonPath("$.articlesByCategory").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testNewsViewsChartEndpoint() throws Exception {
        mockMvc.perform(get("/admin/api/news/views-chart")
                .param("months", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.labels").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTopCategoriesEndpoint() throws Exception {
        mockMvc.perform(get("/admin/api/news/top-categories")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/admin/api/news/analytics"))
                .andExpect(status().isUnauthorized());
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