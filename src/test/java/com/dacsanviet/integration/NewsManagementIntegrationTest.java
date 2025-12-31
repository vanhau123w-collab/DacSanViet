package com.dacsanviet.integration;

import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.dto.NewsCategoryDto;
import com.dacsanviet.dto.NewsCommentDto;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.model.Role;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.service.NewsCategoryService;
import com.dacsanviet.service.NewsCommentService;
import com.dacsanviet.service.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for complete News Management System workflow
 * Tests all components working together: entities, repositories, services, controllers
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class NewsManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsCategoryService newsCategoryService;

    @Autowired
    private NewsCommentService newsCommentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testAdmin;
    private User testUser;
    private NewsCategoryDto testCategory;

    @BeforeEach
    void setUp() {
        // Create test admin user
        testAdmin = new User();
        testAdmin.setUsername("testadmin");
        testAdmin.setEmail("testadmin@test.com");
        testAdmin.setPassword(passwordEncoder.encode("password"));
        testAdmin.setFullName("Test Admin");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setIsActive(true);
        testAdmin = userRepository.save(testAdmin);

        // Create test regular user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@test.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setFullName("Test User");
        testUser.setRole(Role.USER);
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Create test category
        NewsCategoryDto categoryDto = new NewsCategoryDto();
        categoryDto.setName("Test Category");
        categoryDto.setDescription("Test category for integration testing");
        categoryDto.setIsActive(true);
        testCategory = newsCategoryService.createCategory(categoryDto);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCompleteNewsManagementWorkflow() throws Exception {
        // 1. Create a news article via service
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("Test Integration Article");
        articleDto.setContent("This is a test article for integration testing. It contains comprehensive content to test the news management system.");
        articleDto.setExcerpt("Test article excerpt for integration testing");
        articleDto.setAuthorId(testAdmin.getId());
        articleDto.setCategoryId(testCategory.getId());
        articleDto.setStatus(NewsStatus.DRAFT);
        articleDto.setMetaDescription("Test meta description for SEO testing");
        articleDto.setMetaKeywords("test, integration, news, article");
        articleDto.setIsFeatured(false);

        NewsArticleDto createdArticle = newsService.createArticle(articleDto);

        // Verify article was created correctly
        assertThat(createdArticle).isNotNull();
        assertThat(createdArticle.getId()).isNotNull();
        assertThat(createdArticle.getTitle()).isEqualTo("Test Integration Article");
        assertThat(createdArticle.getSlug()).isEqualTo("test-integration-article");
        assertThat(createdArticle.getStatus()).isEqualTo(NewsStatus.DRAFT);
        assertThat(createdArticle.getAuthorId()).isEqualTo(testAdmin.getId());
        assertThat(createdArticle.getCategoryId()).isEqualTo(testCategory.getId());

        // 2. Test admin API endpoints
        // Get article via admin API
        mockMvc.perform(get("/api/admin/news/{id}", createdArticle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Integration Article"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        // Update article via admin API
        articleDto.setTitle("Updated Integration Article");
        articleDto.setStatus(NewsStatus.PUBLISHED);

        mockMvc.perform(put("/api/admin/news/{id}", createdArticle.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Article"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        // 3. Test publishing workflow
        mockMvc.perform(post("/api/admin/news/{id}/publish", createdArticle.getId())
                .with(csrf()))
                .andExpect(status().isOk());

        // Verify article is published
        Optional<NewsArticleDto> publishedArticle = newsService.findById(createdArticle.getId());
        assertThat(publishedArticle).isPresent();
        assertThat(publishedArticle.get().getStatus()).isEqualTo(NewsStatus.PUBLISHED);
        assertThat(publishedArticle.get().getPublishedAt()).isNotNull();

        // 4. Test public news endpoints
        // Test news listing
        mockMvc.perform(get("/news"))
                .andExpect(status().isOk())
                .andExpect(view().name("news/news-list"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("categories"));

        // Test article detail view
        mockMvc.perform(get("/news/{slug}", publishedArticle.get().getSlug()))
                .andExpect(status().isOk())
                .andExpect(view().name("news/article-detail"))
                .andExpect(model().attributeExists("article"))
                .andExpect(model().attribute("article", 
                    org.hamcrest.Matchers.hasProperty("title", 
                        org.hamcrest.Matchers.containsString("Integration Article"))));

        // 5. Test search functionality
        mockMvc.perform(get("/news/search").param("q", "integration"))
                .andExpect(status().isOk())
                .andExpect(view().name("news/search"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attribute("searchKeyword", "integration"));

        // 6. Test category filtering
        mockMvc.perform(get("/news/category/{categorySlug}", testCategory.getSlug()))
                .andExpect(status().isOk())
                .andExpect(view().name("news/category"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("currentCategory"));

        // 7. Test view count increment
        Long initialViewCount = publishedArticle.get().getViewCount();
        newsService.incrementViewCount(createdArticle.getId());
        
        Optional<NewsArticleDto> articleAfterView = newsService.findById(createdArticle.getId());
        assertThat(articleAfterView).isPresent();
        assertThat(articleAfterView.get().getViewCount()).isEqualTo(initialViewCount + 1);

        // 8. Test featured article functionality
        mockMvc.perform(post("/api/admin/news/{id}/feature", createdArticle.getId())
                .with(csrf()))
                .andExpect(status().isOk());

        List<NewsArticleDto> featuredArticles = newsService.findFeaturedArticles(10);
        assertThat(featuredArticles).isNotEmpty();
        assertThat(featuredArticles.stream()
                .anyMatch(a -> a.getId().equals(createdArticle.getId()))).isTrue();

        // 9. Test analytics functionality
        mockMvc.perform(get("/api/admin/news/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusStats").exists())
                .andExpect(jsonPath("$.mostViewedArticles").exists());

        // 10. Test comment system (if implemented)
        if (newsCommentService != null) {
            NewsCommentDto commentDto = new NewsCommentDto();
            commentDto.setArticleId(createdArticle.getId());
            commentDto.setUserId(testUser.getId());
            commentDto.setContent("This is a test comment for integration testing");

            NewsCommentDto createdComment = newsCommentService.createUserComment(commentDto);
            assertThat(createdComment).isNotNull();
            assertThat(createdComment.getContent()).isEqualTo("This is a test comment for integration testing");
        }

        // 11. Test soft delete
        mockMvc.perform(delete("/api/admin/news/{id}", createdArticle.getId())
                .with(csrf()))
                .andExpect(status().isOk());

        Optional<NewsArticleDto> deletedArticle = newsService.findById(createdArticle.getId());
        assertThat(deletedArticle).isPresent();
        assertThat(deletedArticle.get().getStatus()).isEqualTo(NewsStatus.ARCHIVED);
    }

    @Test
    void testPublicNewsAccessWithoutAuthentication() throws Exception {
        // Create and publish an article
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("Public Test Article");
        articleDto.setContent("This is a public test article");
        articleDto.setExcerpt("Public test excerpt");
        articleDto.setAuthorId(testAdmin.getId());
        articleDto.setCategoryId(testCategory.getId());
        articleDto.setStatus(NewsStatus.PUBLISHED);

        NewsArticleDto createdArticle = newsService.createArticle(articleDto);
        newsService.publishArticle(createdArticle.getId());

        // Test public access to news listing
        mockMvc.perform(get("/news"))
                .andExpect(status().isOk())
                .andExpect(view().name("news/news-list"));

        // Test public access to article detail
        mockMvc.perform(get("/news/{slug}", createdArticle.getSlug()))
                .andExpect(status().isOk())
                .andExpect(view().name("news/article-detail"));

        // Test public search
        mockMvc.perform(get("/news/search").param("q", "public"))
                .andExpect(status().isOk())
                .andExpect(view().name("news/search"));
    }

    @Test
    void testAdminAccessControlForNewsManagement() throws Exception {
        // Test that admin endpoints require authentication
        mockMvc.perform(get("/api/admin/news"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/news")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden()); // CSRF token missing
    }

    @Test
    void testNewsServiceBusinessLogic() {
        // Test article creation with SEO slug generation
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("Test Article with Special Characters & Symbols!");
        articleDto.setContent("Test content");
        articleDto.setAuthorId(testAdmin.getId());
        articleDto.setCategoryId(testCategory.getId());

        NewsArticleDto createdArticle = newsService.createArticle(articleDto);
        assertThat(createdArticle.getSlug()).matches("[a-z0-9-]+"); // Should be URL-friendly

        // Test duplicate slug handling
        NewsArticleDto duplicateDto = new NewsArticleDto();
        duplicateDto.setTitle("Test Article with Special Characters & Symbols!");
        duplicateDto.setContent("Different content");
        duplicateDto.setAuthorId(testAdmin.getId());
        duplicateDto.setCategoryId(testCategory.getId());

        NewsArticleDto duplicateArticle = newsService.createArticle(duplicateDto);
        assertThat(duplicateArticle.getSlug()).isNotEqualTo(createdArticle.getSlug());
        assertThat(duplicateArticle.getSlug()).startsWith(createdArticle.getSlug());

        // Test pagination
        Page<NewsArticleDto> publishedArticles = newsService.findPublishedArticles(PageRequest.of(0, 10));
        assertThat(publishedArticles).isNotNull();
        assertThat(publishedArticles.getSize()).isEqualTo(10);

        // Test search functionality
        Page<NewsArticleDto> searchResults = newsService.searchArticles("test", PageRequest.of(0, 10));
        assertThat(searchResults).isNotNull();

        // Test analytics
        var statusStats = newsService.getArticleStatsByStatus();
        assertThat(statusStats).isNotNull();
        assertThat(statusStats).containsKey("DRAFT");

        var viewStats = newsService.getViewStatsByMonth(6);
        assertThat(viewStats).isNotNull();
    }

    @Test
    void testCategoryManagement() {
        // Test category creation
        NewsCategoryDto categoryDto = new NewsCategoryDto();
        categoryDto.setName("Integration Test Category");
        categoryDto.setDescription("Category for integration testing");
        categoryDto.setIsActive(true);

        NewsCategoryDto createdCategory = newsCategoryService.createCategory(categoryDto);
        assertThat(createdCategory).isNotNull();
        assertThat(createdCategory.getSlug()).isEqualTo("integration-test-category");

        // Test finding active categories
        List<NewsCategoryDto> activeCategories = newsCategoryService.findActiveCategories();
        assertThat(activeCategories).isNotEmpty();
        assertThat(activeCategories.stream()
                .anyMatch(c -> c.getName().equals("Integration Test Category"))).isTrue();

        // Test category with articles
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("Article in Test Category");
        articleDto.setContent("Test content");
        articleDto.setAuthorId(testAdmin.getId());
        articleDto.setCategoryId(createdCategory.getId());
        articleDto.setStatus(NewsStatus.PUBLISHED);

        newsService.createArticle(articleDto);

        List<NewsCategoryDto> categoriesWithArticles = newsCategoryService.findCategoriesWithPublishedArticles();
        assertThat(categoriesWithArticles.stream()
                .anyMatch(c -> c.getId().equals(createdCategory.getId()))).isTrue();
    }

    @Test
    void testErrorHandling() {
        // Test creating article with non-existent author
        NewsArticleDto invalidArticleDto = new NewsArticleDto();
        invalidArticleDto.setTitle("Invalid Article");
        invalidArticleDto.setContent("Test content");
        invalidArticleDto.setAuthorId(99999L); // Non-existent author
        invalidArticleDto.setCategoryId(testCategory.getId());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            newsService.createArticle(invalidArticleDto);
        });

        // Test finding non-existent article
        Optional<NewsArticleDto> nonExistentArticle = newsService.findById(99999L);
        assertThat(nonExistentArticle).isEmpty();

        // Test invalid slug
        Optional<NewsArticleDto> invalidSlugArticle = newsService.findBySlug("non-existent-slug");
        assertThat(invalidSlugArticle).isEmpty();
    }

    @Test
    void testDataIntegrity() {
        // Create article with all fields
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("Complete Test Article");
        articleDto.setContent("Complete test content with all fields populated");
        articleDto.setExcerpt("Complete test excerpt");
        articleDto.setAuthorId(testAdmin.getId());
        articleDto.setCategoryId(testCategory.getId());
        articleDto.setStatus(NewsStatus.PUBLISHED);
        articleDto.setMetaDescription("Complete meta description for SEO");
        articleDto.setMetaKeywords("complete, test, article, seo");
        articleDto.setIsFeatured(true);
        articleDto.setFeaturedImage("https://example.com/featured.jpg");
        articleDto.setThumbnailImage("https://example.com/thumbnail.jpg");

        NewsArticleDto createdArticle = newsService.createArticle(articleDto);

        // Verify all fields are preserved
        assertThat(createdArticle.getTitle()).isEqualTo("Complete Test Article");
        assertThat(createdArticle.getContent()).isEqualTo("Complete test content with all fields populated");
        assertThat(createdArticle.getExcerpt()).isEqualTo("Complete test excerpt");
        assertThat(createdArticle.getMetaDescription()).isEqualTo("Complete meta description for SEO");
        assertThat(createdArticle.getMetaKeywords()).isEqualTo("complete, test, article, seo");
        assertThat(createdArticle.getIsFeatured()).isTrue();
        assertThat(createdArticle.getFeaturedImage()).isEqualTo("https://example.com/featured.jpg");
        assertThat(createdArticle.getThumbnailImage()).isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(createdArticle.getPublishedAt()).isNotNull();
        assertThat(createdArticle.getCreatedAt()).isNotNull();
        assertThat(createdArticle.getUpdatedAt()).isNotNull();

        // Test update preserves data integrity
        createdArticle.setTitle("Updated Complete Article");
        NewsArticleDto updatedArticle = newsService.updateArticle(createdArticle.getId(), createdArticle);
        
        assertThat(updatedArticle.getTitle()).isEqualTo("Updated Complete Article");
        assertThat(updatedArticle.getSlug()).isEqualTo("updated-complete-article");
        assertThat(updatedArticle.getContent()).isEqualTo(createdArticle.getContent());
        assertThat(updatedArticle.getCreatedAt()).isEqualTo(createdArticle.getCreatedAt());
        assertThat(updatedArticle.getUpdatedAt()).isAfter(createdArticle.getUpdatedAt());
    }
}