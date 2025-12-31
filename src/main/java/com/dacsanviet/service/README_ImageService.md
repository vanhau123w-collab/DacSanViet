# Image Management System for News Articles

## Overview

The Image Management System provides comprehensive image upload, validation, processing, and storage capabilities for news articles. It includes automatic image optimization, thumbnail generation, and file validation.

## Features

- **File Validation**: Supports JPG, PNG, and WebP formats with 5MB size limit
- **Image Processing**: Automatic resizing and optimization for web display
- **Thumbnail Generation**: Creates 300x200 thumbnails for listing pages
- **Directory Organization**: Date-based folder structure (uploads/news/YYYY/MM/)
- **Error Handling**: Comprehensive validation with Vietnamese error messages
- **Security**: File type validation and safe filename generation

## Components

### 1. ImageService Interface
Main service interface for image operations:
- `uploadNewsImage()` - Upload single image
- `uploadMultipleNewsImages()` - Upload multiple images
- `deleteImage()` - Delete image and thumbnail
- `validateImageFile()` - Validate file format and size

### 2. ImageServiceImpl
Implementation with image processing capabilities:
- Uses imgscalr library for high-quality image resizing
- Generates unique filenames with UUID
- Creates optimized versions (max 1200x800)
- Generates thumbnails (300x200)

### 3. NewsImageController
REST API endpoints for admin image management:
- `POST /api/admin/news/images/upload` - Upload single image
- `POST /api/admin/news/images/upload-multiple` - Upload multiple images
- `DELETE /api/admin/news/images` - Delete image
- `GET /api/admin/news/images/config` - Get upload configuration

### 4. FileUploadConfig
Configuration for file upload settings and static resource serving:
- Multipart file configuration (5MB max)
- Static resource handlers for serving uploaded images
- Automatic directory creation on startup

### 5. ImageUtils
Utility class for image validation and processing:
- File format validation
- MIME type checking
- Safe filename generation
- Image dimension checking

## Usage Examples

### Upload Single Image
```java
@Autowired
private ImageService imageService;

public void uploadImage(MultipartFile file) {
    try {
        ImageUploadResult result = imageService.uploadNewsImage(file, articleId);
        String originalUrl = result.getOriginalUrl();
        String thumbnailUrl = result.getThumbnailUrl();
        // Save URLs to database
    } catch (IllegalArgumentException e) {
        // Handle validation error
    } catch (IOException e) {
        // Handle file processing error
    }
}
```

### REST API Usage
```javascript
// Upload image via AJAX
const formData = new FormData();
formData.append('file', imageFile);
formData.append('articleId', '123');

fetch('/api/admin/news/images/upload', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => {
    if (data.success) {
        console.log('Original URL:', data.data.originalUrl);
        console.log('Thumbnail URL:', data.data.thumbnailUrl);
    }
});
```

## File Structure

```
uploads/news/
├── 2024/
│   ├── 12/
│   │   ├── uuid_optimized.jpg    # Optimized original
│   │   ├── uuid_thumb.jpg        # Thumbnail
│   │   └── ...
│   └── 11/
└── 2025/
```

## Configuration

### Application Properties
```properties
# News image upload path
app.upload.news-images=uploads/news

# File upload limits
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```

### Image Processing Settings
- **Max dimensions**: 1200x800 pixels
- **Thumbnail size**: 300x200 pixels
- **Supported formats**: JPG, PNG, WebP
- **Max file size**: 5MB
- **Quality**: High-quality resizing with anti-aliasing

## Error Handling

The system provides comprehensive error handling with Vietnamese messages:

- **File validation errors**: Format, size, MIME type validation
- **Processing errors**: Image reading, resizing, saving failures
- **Storage errors**: Directory creation, file system issues

## Security Features

- **File type validation**: Checks both extension and MIME type
- **Safe filenames**: UUID-based naming prevents conflicts
- **Size limits**: Prevents large file uploads
- **Directory traversal protection**: Validates file paths
- **Admin-only access**: REST endpoints require ADMIN/STAFF roles

## Testing

The system includes comprehensive unit tests:
- File validation testing
- Error condition testing
- Configuration testing
- Mock-based testing for service layer

Run tests with:
```bash
mvn test -Dtest=ImageServiceTest
```

## Integration with News System

The image service integrates with the news management system:
1. Upload images during article creation/editing
2. Store image URLs in NewsArticle entity
3. Display thumbnails in article listings
4. Show full images in article detail view
5. Clean up images when articles are deleted (soft delete preserves images)