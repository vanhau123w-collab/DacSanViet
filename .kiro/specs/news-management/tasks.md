# Implementation Plan: News Management System

## Overview

Implementation plan cho hệ thống quản lý tin tức với CRUD operations, image upload, SEO optimization, và comment system. Sử dụng Spring Boot, JPA, và jqwik cho property-based testing.

## Tasks

- [x] 1. Set up core entities and database schema
  - Create NewsArticle, NewsCategory, NewsComment entities
  - Create database migration files
  - Set up enum classes (NewsStatus, CommentStatus)
  - _Requirements: 1.1, 2.1, 7.1_

- [ ]* 1.1 Write property test for entity validation
  - **Property 1: Article CRUD Operations**
  - **Validates: Requirements 1.1, 1.2, 1.3**

- [x] 2. Implement repository layer
  - Create NewsArticleRepository with custom queries
  - Create NewsCategoryRepository
  - Create NewsCommentRepository
  - Add search and filtering methods
  - _Requirements: 1.4, 2.1, 3.1, 3.2_

- [ ]* 2.1 Write property test for repository queries
  - **Property 2: Article Search and Filtering**
  - **Validates: Requirements 1.4, 3.1, 3.2, 3.5**

- [x] 3. Create service layer
  - Implement NewsService with CRUD operations
  - Implement CategoryService
  - Implement CommentService
  - Add business logic for soft delete, view counting
  - _Requirements: 1.1, 1.2, 1.3, 2.2_

- [ ]* 3.1 Write property test for service operations
  - **Property 3: Published Article Display**
  - **Validates: Requirements 2.1, 2.5**

- [ ]* 3.2 Write property test for view count tracking
  - **Property 5: View Count Tracking**
  - **Validates: Requirements 2.2, 6.1**

- [x] 4. Implement SEO and slug generation
  - Create SEOService for slug generation
  - Add slug uniqueness validation
  - Implement meta description validation
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [ ]* 4.1 Write property test for SEO functionality
  - **Property 9: SEO Slug Generation**
  - **Validates: Requirements 5.2, 5.3**

- [ ]* 4.2 Write property test for metadata validation
  - **Property 10: SEO Metadata Validation**
  - **Validates: Requirements 5.1, 5.4**

- [x] 5. Create image management system
  - Implement ImageService for file upload
  - Add file validation (format, size)
  - Implement image resizing and thumbnail generation
  - Set up file storage directory structure
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ]* 5.1 Write property test for file validation
  - **Property 7: File Upload Validation**
  - **Validates: Requirements 4.1, 4.2**

- [ ]* 5.2 Write property test for image processing
  - **Property 8: Image Processing Pipeline**
  - **Validates: Requirements 4.3, 4.4**

- [x] 6. Checkpoint - Core backend functionality complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement admin REST API controllers
  - Create NewsAdminController with CRUD endpoints
  - Create CategoryAdminController
  - Add validation and error handling
  - Implement pagination and sorting
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ]* 7.1 Write integration tests for admin API
  - Test CRUD operations end-to-end
  - Test error handling and validation
  - _Requirements: 1.1, 1.2, 1.3, 1.5_

- [x] 8. Implement public news controllers
  - Create NewsController for public endpoints
  - Add news listing with pagination
  - Implement article detail view
  - Add category filtering and search
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2_

- [ ]* 8.1 Write property test for pagination
  - **Property 6: Pagination Consistency**
  - **Validates: Requirements 2.4**

- [ ]* 8.2 Write property test for featured articles
  - **Property 4: Featured Article Priority**
  - **Validates: Requirements 2.3**

- [x] 9. Create admin frontend templates
  - Create admin news management interface
  - Add article create/edit forms with rich text editor
  - Implement image upload interface
  - Add category management interface
  - _Requirements: 1.1, 1.2, 4.1, 4.2_

- [x] 10. Create public news templates
  - Create news listing page (replace current static news page)
  - Create article detail page
  - Add category filtering interface
  - Implement search functionality
  - Add responsive design for mobile
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 8.1_

- [x] 11. Implement comment system
  - Add comment form to article detail page
  - Create comment display with threading
  - Implement admin comment moderation
  - Add guest comment support
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ]* 11.1 Write property test for comment system
  - **Property 11: Comment System Integrity**
  - **Validates: Requirements 7.1, 7.2, 7.5**

- [ ]* 11.2 Write property test for comment moderation
  - **Property 12: Comment Moderation**
  - **Validates: Requirements 7.3, 7.4**

- [x] 12. Add analytics and reporting
  - Implement analytics dashboard
  - Add view statistics tracking
  - Create most viewed articles widget
  - Add article statistics by category/status
  - _Requirements: 6.1, 6.2, 6.3, 6.5_

- [ ]* 12.1 Write property test for analytics
  - **Property 13: Analytics Data Accuracy**
  - **Validates: Requirements 6.2, 6.3, 6.5**

- [x] 13. Security and validation
  - Add Spring Security configuration for admin endpoints
  - Implement input validation with custom validators
  - Add CSRF protection for forms
  - Implement file upload security
  - _Requirements: 1.5, 4.1_

- [ ]* 13.1 Write property test for validation
  - **Property 14: Data Validation and Error Handling**
  - **Validates: Requirements 1.5**

- [ ]* 13.2 Write property test for image retention
  - **Property 15: Image Retention Policy**
  - **Validates: Requirements 4.5**

- [x] 14. Integration and testing
  - Wire all components together
  - Add sample data initialization
  - Test complete workflows
  - Fix any integration issues
  - _Requirements: All_

- [ ]* 14.1 Write comprehensive integration tests
  - Test complete news management workflow
  - Test public news browsing workflow
  - Test comment system workflow
  - _Requirements: All_

- [x] 15. Final checkpoint - Complete system testing
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties using jqwik framework
- Unit tests validate specific examples and edge cases
- Integration tests validate end-to-end workflows
- Use H2 database for development and testing
- Follow existing project structure and coding patterns
- Implement Vietnamese language support throughout UI