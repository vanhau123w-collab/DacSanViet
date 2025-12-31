# Design Document - News Management System

## Overview

Hệ thống quản lý tin tức cho website Đặc Sản Việt được thiết kế theo kiến trúc MVC với Spring Boot. Hệ thống bao gồm backend API để quản lý tin tức và frontend responsive để hiển thị cho người dùng. Thiết kế tập trung vào hiệu suất, SEO và trải nghiệm người dùng tốt.

## Architecture

### System Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend UI   │    │   Admin Panel   │    │   Mobile App    │
│   (Thymeleaf)   │    │   (Bootstrap)   │    │   (Responsive)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌─────────────────────────────────────────────────┐
         │              Spring Boot REST API               │
         │  ┌─────────────┐  ┌─────────────┐  ┌──────────┐ │
         │  │ News        │  │ Comment     │  │ Analytics│ │
         │  │ Controller  │  │ Controller  │  │ Service  │ │
         │  └─────────────┘  └─────────────┘  └──────────┘ │
         └─────────────────────────────────────────────────┘
                                 │
         ┌─────────────────────────────────────────────────┐
         │              Service Layer                      │
         │  ┌─────────────┐  ┌─────────────┐  ┌──────────┐ │
         │  │ News        │  │ Image       │  │ SEO      │ │
         │  │ Service     │  │ Service     │  │ Service  │ │
         │  └─────────────┘  └─────────────┘  └──────────┘ │
         └─────────────────────────────────────────────────┘
                                 │
         ┌─────────────────────────────────────────────────┐
         │              Data Layer                         │
         │  ┌─────────────┐  ┌─────────────┐  ┌──────────┐ │
         │  │ News        │  │ Comment     │  │ Category │ │
         │  │ Repository  │  │ Repository  │  │ Repository│ │
         │  └─────────────┘  └─────────────┘  └──────────┘ │
         └─────────────────────────────────────────────────┘
                                 │
         ┌─────────────────────────────────────────────────┐
         │              Database (H2/MySQL)                │
         │  ┌─────────────┐  ┌─────────────┐  ┌──────────┐ │
         │  │ news        │  │ comments    │  │ news_    │ │
         │  │ _articles   │  │             │  │ categories│ │
         │  └─────────────┘  └─────────────┘  └──────────┘ │
         └─────────────────────────────────────────────────┘
```

### Technology Stack
- **Backend**: Spring Boot 3.3.5, Spring Data JPA, Spring Security
- **Database**: H2 (development), MySQL (production)
- **Frontend**: Thymeleaf, Bootstrap 5, JavaScript ES6
- **File Storage**: Local filesystem với tối ưu hóa hình ảnh
- **Caching**: Spring Cache với EhCache
- **Build Tool**: Maven

## Components and Interfaces

### 1. News Article Entity
```java
@Entity
@Table(name = "news_articles")
public class NewsArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(unique = true, nullable = false, length = 250)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 300)
    private String excerpt;
    
    @Column(name = "featured_image")
    private String featuredImage;
    
    @Column(name = "thumbnail_image")
    private String thumbnailImage;
    
    @Enumerated(EnumType.STRING)
    private NewsStatus status; // DRAFT, PUBLISHED, ARCHIVED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private NewsCategory category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "meta_description", length = 160)
    private String metaDescription;
    
    @Column(name = "meta_keywords", length = 255)
    private String metaKeywords;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 2. News Category Entity
```java
@Entity
@Table(name = "news_categories")
public class NewsCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(unique = true, length = 120)
    private String slug;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
```

### 3. Comment Entity
```java
@Entity
@Table(name = "news_comments")
public class NewsComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null for guest comments
    
    @Column(name = "guest_name", length = 100)
    private String guestName;
    
    @Column(name = "guest_email", length = 100)
    private String guestEmail;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private NewsComment parent; // for replies
    
    @Enumerated(EnumType.STRING)
    private CommentStatus status; // PENDING, APPROVED, REJECTED
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### 4. News Service Interface
```java
@Service
public interface NewsService {
    // CRUD Operations
    NewsArticle createArticle(NewsArticleDto articleDto);
    NewsArticle updateArticle(Long id, NewsArticleDto articleDto);
    void deleteArticle(Long id);
    Optional<NewsArticle> findById(Long id);
    Optional<NewsArticle> findBySlug(String slug);
    
    // Query Operations
    Page<NewsArticle> findPublishedArticles(Pageable pageable);
    Page<NewsArticle> findByCategory(Long categoryId, Pageable pageable);
    Page<NewsArticle> searchArticles(String keyword, Pageable pageable);
    List<NewsArticle> findFeaturedArticles(int limit);
    List<NewsArticle> findRecentArticles(int limit);
    
    // Admin Operations
    Page<NewsArticle> findAllForAdmin(NewsStatus status, Pageable pageable);
    void publishArticle(Long id);
    void unpublishArticle(Long id);
    void toggleFeatured(Long id);
    
    // Analytics
    void incrementViewCount(Long id);
    List<NewsArticle> findMostViewedArticles(int limit);
    Map<String, Long> getArticleStatsByStatus();
    Map<String, Long> getViewStatsByMonth(int months);
}
```

### 5. News Controller
```java
@Controller
@RequestMapping("/news")
public class NewsController {
    
    // Public endpoints
    @GetMapping
    public String listNews(Model model, Pageable pageable);
    
    @GetMapping("/{slug}")
    public String viewArticle(@PathVariable String slug, Model model);
    
    @GetMapping("/category/{categorySlug}")
    public String listByCategory(@PathVariable String categorySlug, Model model, Pageable pageable);
    
    @GetMapping("/search")
    public String searchNews(@RequestParam String q, Model model, Pageable pageable);
}

@RestController
@RequestMapping("/api/admin/news")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class NewsAdminController {
    
    // Admin CRUD endpoints
    @GetMapping
    public ResponseEntity<Page<NewsArticleDto>> getAllArticles(Pageable pageable);
    
    @PostMapping
    public ResponseEntity<NewsArticleDto> createArticle(@Valid @RequestBody NewsArticleDto articleDto);
    
    @PutMapping("/{id}")
    public ResponseEntity<NewsArticleDto> updateArticle(@PathVariable Long id, @Valid @RequestBody NewsArticleDto articleDto);
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id);
    
    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishArticle(@PathVariable Long id);
    
    @PostMapping("/{id}/feature")
    public ResponseEntity<Void> toggleFeatured(@PathVariable Long id);
    
    @GetMapping("/analytics")
    public ResponseEntity<NewsAnalyticsDto> getAnalytics();
}
```

## Data Models

### Database Schema
```sql
-- News Categories
CREATE TABLE news_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) UNIQUE,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- News Articles
CREATE TABLE news_articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(250) NOT NULL UNIQUE,
    content TEXT,
    excerpt VARCHAR(300),
    featured_image VARCHAR(500),
    thumbnail_image VARCHAR(500),
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    category_id BIGINT,
    author_id BIGINT,
    view_count BIGINT DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    meta_description VARCHAR(160),
    meta_keywords VARCHAR(255),
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES news_categories(id),
    FOREIGN KEY (author_id) REFERENCES users(id),
    INDEX idx_status (status),
    INDEX idx_published_at (published_at),
    INDEX idx_category (category_id),
    INDEX idx_featured (is_featured),
    INDEX idx_slug (slug)
);

-- News Comments
CREATE TABLE news_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    guest_name VARCHAR(100),
    guest_email VARCHAR(100),
    content TEXT NOT NULL,
    parent_id BIGINT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (article_id) REFERENCES news_articles(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_id) REFERENCES news_comments(id) ON DELETE CASCADE,
    INDEX idx_article (article_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

### DTOs
```java
public class NewsArticleDto {
    private Long id;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String featuredImage;
    private NewsStatus status;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private Long viewCount;
    private Boolean isFeatured;
    private String metaDescription;
    private String metaKeywords;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public class NewsCommentDto {
    private Long id;
    private Long articleId;
    private Long userId;
    private String guestName;
    private String guestEmail;
    private String content;
    private Long parentId;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private List<NewsCommentDto> replies;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, I identified several properties that can be consolidated to eliminate redundancy:

- Properties 1.4 and 3.2 both test search functionality - combined into comprehensive search property
- Properties 2.1 and 2.3 both test article display filtering - combined into display filtering property  
- Properties 4.1 and 4.2 both test file validation - combined into comprehensive file validation property
- Properties 6.1 and 6.2 both test view tracking - combined into view analytics property

### Core Properties

**Property 1: Article CRUD Operations**
*For any* valid news article data, creating, updating, and soft-deleting articles should maintain data integrity and proper status transitions
**Validates: Requirements 1.1, 1.2, 1.3**

**Property 2: Article Search and Filtering**
*For any* search keyword or filter criteria, the system should return only articles that match the criteria in title, content, or category
**Validates: Requirements 1.4, 3.1, 3.2, 3.5**

**Property 3: Published Article Display**
*For any* request to view news, only published articles should be displayed to public users, ordered by publication date (newest first)
**Validates: Requirements 2.1, 2.5**

**Property 4: Featured Article Priority**
*For any* news listing, featured articles should appear before non-featured articles in the display order
**Validates: Requirements 2.3**

**Property 5: View Count Tracking**
*For any* article view action, the view count should increment by exactly one and be persisted accurately
**Validates: Requirements 2.2, 6.1**

**Property 6: Pagination Consistency**
*For any* news listing with more than 12 articles, pagination should display exactly 12 articles per page with correct page navigation
**Validates: Requirements 2.4**

**Property 7: File Upload Validation**
*For any* image upload, the system should validate file format (JPG/PNG/WebP), size (≤5MB), and reject invalid files with clear error messages
**Validates: Requirements 4.1, 4.2**

**Property 8: Image Processing Pipeline**
*For any* successfully uploaded image, the system should generate both optimized full-size and thumbnail versions
**Validates: Requirements 4.3, 4.4**

**Property 9: SEO Slug Generation**
*For any* article title, the system should generate a unique URL slug, appending numbers for duplicates
**Validates: Requirements 5.2, 5.3**

**Property 10: SEO Metadata Validation**
*For any* SEO metadata input, meta descriptions should be validated for length (150-160 characters) and saved correctly
**Validates: Requirements 5.1, 5.4**

**Property 11: Comment System Integrity**
*For any* comment submission, the system should properly associate comments with articles and users, maintaining thread structure for replies
**Validates: Requirements 7.1, 7.2, 7.5**

**Property 12: Comment Moderation**
*For any* comment display request, only approved comments should be visible to public users, ordered by creation time
**Validates: Requirements 7.3, 7.4**

**Property 13: Analytics Data Accuracy**
*For any* analytics query, statistics should accurately reflect the current state of articles by category, status, and view counts
**Validates: Requirements 6.2, 6.3, 6.5**

**Property 14: Data Validation and Error Handling**
*For any* invalid input data, the system should reject the operation and return clear, specific error messages
**Validates: Requirements 1.5**

**Property 15: Image Retention Policy**
*For any* article deletion (soft delete), associated images should remain accessible to prevent broken links
**Validates: Requirements 4.5**

## Error Handling

### Input Validation Errors
- **Invalid Article Data**: Return 400 Bad Request with field-specific error messages
- **File Upload Errors**: Return 400 Bad Request for invalid format/size with clear descriptions
- **SEO Validation Errors**: Return 400 Bad Request for invalid meta description length or slug conflicts

### Business Logic Errors
- **Article Not Found**: Return 404 Not Found for non-existent articles
- **Unauthorized Access**: Return 403 Forbidden for non-admin users accessing admin endpoints
- **Duplicate Slug**: Automatically append number suffix to create unique slug

### System Errors
- **Database Connection**: Return 500 Internal Server Error with generic message, log detailed error
- **File System Errors**: Return 500 Internal Server Error for image processing failures
- **WebSocket Errors**: Graceful fallback to REST API for real-time features

### Error Response Format
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "details": [
    {
      "field": "title",
      "message": "Tiêu đề không được để trống"
    }
  ],
  "timestamp": "2024-12-30T10:30:00Z"
}
```

## Testing Strategy

### Dual Testing Approach
The system will use both unit testing and property-based testing for comprehensive coverage:

- **Unit Tests**: Verify specific examples, edge cases, and error conditions
- **Property Tests**: Verify universal properties across all inputs using jqwik framework
- **Integration Tests**: Test end-to-end workflows and component interactions

### Property-Based Testing Configuration
- **Framework**: jqwik (already included in pom.xml)
- **Test Iterations**: Minimum 100 iterations per property test
- **Test Tagging**: Each property test tagged with format: **Feature: news-management, Property {number}: {property_text}**

### Unit Testing Focus Areas
- **Specific Examples**: Test concrete scenarios like creating article with valid data
- **Edge Cases**: Test boundary conditions like maximum file size, character limits
- **Error Conditions**: Test invalid inputs, missing data, unauthorized access
- **Integration Points**: Test controller-service-repository interactions

### Property Testing Focus Areas
- **Universal Properties**: Test properties that hold for all valid inputs
- **Data Integrity**: Verify CRUD operations maintain consistency
- **Business Rules**: Test search, filtering, and display logic across random data sets
- **File Processing**: Test image upload and processing with various file types

### Test Data Management
- **Test Database**: Use H2 in-memory database for isolated testing
- **Sample Data**: Generate realistic Vietnamese news content for testing
- **File Fixtures**: Include sample images in various formats for upload testing
- **User Roles**: Test with different user roles (ADMIN, STAFF, USER, anonymous)

### Performance Testing
- **Load Testing**: Test pagination with large datasets (1000+ articles)
- **Image Processing**: Test upload and processing of maximum size files
- **Search Performance**: Test search functionality with large content volumes
- **Concurrent Access**: Test multiple users accessing and modifying articles simultaneously