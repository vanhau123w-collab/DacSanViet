package com.dacsanviet.integration;

import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.dto.NewsCategoryDto;
import com.dacsanviet.model.NewsStatus;
import com.dacsanviet.model.Role;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.service.NewsCategoryService;
import com.dacsanviet.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic integration test for News Management System
 * Tests core functionality to verify components are properly wired
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BasicNewsIntegrationTest {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsCategoryService newsCategoryService;

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
        testAdmin.setUsername("basictest");
        testAdmin.setEmail("basictest@test.com");
        testAdmin.setPassword(passwordEncoder.encode("password"));
        testAdmin.setFullName("Basic Test Admin");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setIsActive(true);
        testAdmin = userRepository.save(testAdmin);

        // Create test category
        NewsCategoryDto categoryDto = new NewsCategoryDto();
        categoryDto.setName("Basic Test Category");
        categoryDto.setDescription("Category for basic integration testing");
        categoryDto.setIsActive(true);
        testCategory = newsCategoryService.createCategory(categoryDto);
    }

    @Test
    void testBasicNewsManagementWorkflow() {
        // Test 1: Verify services are injected
        assertThat(newsService).isNotNull();
        assertThat(newsCategoryService).isNotNull();
        assertThat(userRepository).isNotNull();

        // Test 2: Create a news article
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setTitle("Basic Integration Test Article");
        articleDto.setContent("This is a basic test article for integration testing.");
        articleDto.setExcerpt("Basic test article excerpt");
        articleDto.setAuthorId(testAdmin.getId());
        articleDto.setCategoryId(testCategory.getId());
        articleDto.setStatus(NewsStatus.DRAFT);

        NewsArticleDto createdArticle = newsService.createArticle(articleDto);

        // Verify article creation
        assertThat(createdArticle).isNotNull();
        assertThat(createdArticle.getId()).isNotNull();
        assertThat(createdArticle.getTitle()).isEqualTo("Basic Integration Test Article");
        assertThat(createdArticle.getSlug()).isEqualTo("basic-integration-test-article");
        assertThat(createdArticle.getStatus()).isEqualTo(NewsStatus.DRAFT);

        // Test 3: Find article by ID
        Optional<NewsArticleDto> foundArticle = newsService.findById(createdArticle.getId());
        assertThat(foundArticle).isPresent();
        assertThat(foundArticle.get().getTitle()).isEqualTo("Basic Integration Test Article");

        // Test 4: Find article by slug
        Optional<NewsArticleDto> foundBySlug = newsService.findBySlug(createdArticle.getSlug());
        assertThat(foundBySlug).isPresent();
        assertThat(foundBySlug.get().getId()).isEqualTo(createdArticle.getId());

        // Test 5: Publish article
        newsService.publishArticle(createdArticle.getId());
        Optional<NewsArticleDto> publishedArticle = newsService.findById(createdArticle.getId());
        assertThat(publishedArticle).isPresent();
        assertThat(publishedArticle.get().getStatus()).isEqualTo(NewsStatus.PUBLISHED);
        assertThat(publishedArticle.get().getPublishedAt()).isNotNull();

        // Test 6: Update article
        createdArticle.setTitle("Updated Basic Test Article");
        NewsArticleDto updatedArticle = newsService.updateArticle(createdArticle.getId(), createdArticle);
        assertThat(updatedArticle.getTitle()).isEqualTo("Updated Basic Test Article");
        assertThat(updatedArticle.getSlug()).isEqualTo("updated-basic-test-article");

        // Test 7: Soft delete article
        newsService.deleteArticle(createdArticle.getId());
        Optional<NewsArticleDto> deletedArticle = newsService.findById(createdArticle.getId());
        assertThat(deletedArticle).isPresent();
        assertThat(deletedArticle.get().getStatus()).isEqualTo(NewsStatus.ARCHIVED);
    }

    @Test
    void testCategoryManagement() {
        // Test category creation
        assertThat(testCategory).isNotNull();
        assertThat(testCategory.getId()).isNotNull();
        assertThat(testCategory.getName()).isEqualTo("Basic Test Category");
        assertThat(testCategory.getSlug()).isEqualTo("basic-test-category");

        // Test finding category by ID
        Optional<NewsCategoryDto> foundCategory = newsCategoryService.findById(testCategory.getId());
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Basic Test Category");

        // Test finding category by slug
        Optional<NewsCategoryDto> foundBySlug = newsCategoryService.findBySlug(testCategory.getSlug());
        assertThat(foundBySlug).isPresent();
        assertThat(foundBySlug.get().getId()).isEqualTo(testCategory.getId());

        // Test active categories
        var activeCategories = newsCategoryService.findActiveCategories();
        assertThat(activeCategories).isNotEmpty();
        assertThat(activeCategories.stream()
                .anyMatch(c -> c.getId().equals(testCategory.getId()))).isTrue();
    }

    @Test
    void testErrorHandling() {
        // Test creating article with non-existent author
        NewsArticleDto invalidArticle = new NewsArticleDto();
        invalidArticle.setTitle("Invalid Article");
        invalidArticle.setContent("Test content");
        invalidArticle.setAuthorId(99999L); // Non-existent
        invalidArticle.setCategoryId(testCategory.getId());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            newsService.createArticle(invalidArticle);
        });

        // Test finding non-existent article
        Optional<NewsArticleDto> nonExistent = newsService.findById(99999L);
        assertThat(nonExistent).isEmpty();

        // Test duplicate category name
        NewsCategoryDto duplicateCategory = new NewsCategoryDto();
        duplicateCategory.setName("Basic Test Category"); // Already exists
        duplicateCategory.setDescription("Duplicate");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            newsCategoryService.createCategory(duplicateCategory);
        });
    }
}