package com.dacsanviet.service;

import com.dacsanviet.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ImageService
 */
class ImageServiceTest {
    
    private ImageService imageService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        imageService = new ImageServiceImpl();
        // Set the upload path to temp directory for testing
        ReflectionTestUtils.setField(imageService, "newsImageUploadPath", tempDir.toString());
    }
    
    @Test
    void testValidateImageFile_ValidJpegFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );
        
        assertDoesNotThrow(() -> imageService.validateImageFile(file));
    }
    
    @Test
    void testValidateImageFile_ValidPngFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.png", 
            "image/png", 
            "test image content".getBytes()
        );
        
        assertDoesNotThrow(() -> imageService.validateImageFile(file));
    }
    
    @Test
    void testValidateImageFile_ValidWebpFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.webp", 
            "image/webp", 
            "test image content".getBytes()
        );
        
        assertDoesNotThrow(() -> imageService.validateImageFile(file));
    }
    
    @Test
    void testValidateImageFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            new byte[0]
        );
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> imageService.validateImageFile(file)
        );
        
        assertEquals("File không được để trống", exception.getMessage());
    }
    
    @Test
    void testValidateImageFile_UnsupportedFormat() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.gif", 
            "image/gif", 
            "test image content".getBytes()
        );
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> imageService.validateImageFile(file)
        );
        
        assertEquals("Định dạng file không được hỗ trợ. Chỉ chấp nhận: JPG, PNG, WebP", exception.getMessage());
    }
    
    @Test
    void testValidateImageFile_InvalidMimeType() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "text/plain", 
            "test content".getBytes()
        );
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> imageService.validateImageFile(file)
        );
        
        assertEquals("File không phải là hình ảnh hợp lệ", exception.getMessage());
    }
    
    @Test
    void testValidateImageFile_FileTooLarge() {
        // Create a file larger than 5MB
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            largeContent
        );
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> imageService.validateImageFile(file)
        );
        
        assertEquals("Kích thước file không được vượt quá 5MB", exception.getMessage());
    }
    
    @Test
    void testValidateImageFile_NullFilename() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            null, 
            "image/jpeg", 
            "test content".getBytes()
        );
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> imageService.validateImageFile(file)
        );
        
        assertEquals("Tên file không hợp lệ", exception.getMessage());
    }
    
    @Test
    void testGetNewsImageStoragePath() {
        String storagePath = imageService.getNewsImageStoragePath();
        assertNotNull(storagePath);
        assertEquals(tempDir.toString(), storagePath);
    }
    
    @Test
    void testDeleteImage_NullUrl() {
        boolean result = imageService.deleteImage(null);
        assertFalse(result);
    }
    
    @Test
    void testDeleteImage_EmptyUrl() {
        boolean result = imageService.deleteImage("");
        assertFalse(result);
    }
}