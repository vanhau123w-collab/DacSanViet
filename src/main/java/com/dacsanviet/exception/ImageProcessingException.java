package com.dacsanviet.exception;

/**
 * Exception thrown when image processing operations fail
 */
public class ImageProcessingException extends RuntimeException {
    
    public ImageProcessingException(String message) {
        super(message);
    }
    
    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ImageProcessingException(Throwable cause) {
        super(cause);
    }
}