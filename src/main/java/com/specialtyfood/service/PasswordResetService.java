package com.specialtyfood.service;

import com.specialtyfood.model.PasswordResetToken;
import com.specialtyfood.model.User;
import com.specialtyfood.repository.PasswordResetTokenRepository;
import com.specialtyfood.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for handling password reset functionality
 */
@Service
@Transactional
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRY_HOURS = 1;
    private static final int TOKEN_LENGTH = 32;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Initiate password reset process
     */
    @Async
    public void initiatePasswordReset(String email) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                // For security reasons, don't reveal if email exists or not
                logger.warn("Password reset requested for non-existent email: {}", email);
                return;
            }
            
            User user = userOptional.get();
            
            if (!user.getIsActive()) {
                logger.warn("Password reset requested for inactive user: {}", email);
                return;
            }
            
            // Delete any existing tokens for this user
            tokenRepository.deleteByUser(user);
            
            // Generate secure reset token
            String resetToken = generateSecureToken();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);
            
            // Save token to database
            PasswordResetToken passwordResetToken = new PasswordResetToken(resetToken, user, expiryDate);
            tokenRepository.save(passwordResetToken);
            
            // Send reset email
            emailService.sendPasswordResetEmail(email, resetToken);
            
            logger.info("Password reset initiated for user: {}", email);
            
        } catch (Exception e) {
            logger.error("Error initiating password reset for email: {}", email, e);
            throw new RuntimeException("Failed to initiate password reset", e);
        }
    }
    
    /**
     * Validate password reset token
     */
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        try {
            Optional<PasswordResetToken> tokenOptional = tokenRepository.findValidTokenByToken(token, LocalDateTime.now());
            return tokenOptional.isPresent();
            
        } catch (Exception e) {
            logger.error("Error validating reset token", e);
            return false;
        }
    }
    
    /**
     * Reset password using token
     */
    public void resetPassword(String token, String newPassword) {
        try {
            Optional<PasswordResetToken> tokenOptional = tokenRepository.findValidTokenByToken(token, LocalDateTime.now());
            
            if (tokenOptional.isEmpty()) {
                throw new RuntimeException("Invalid or expired reset token");
            }
            
            PasswordResetToken resetToken = tokenOptional.get();
            User user = resetToken.getUser();
            
            // Update user password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            // Mark token as used
            tokenRepository.markTokenAsUsed(token);
            
            // Send confirmation email
            emailService.sendPasswordResetConfirmationEmail(user.getEmail());
            
            logger.info("Password reset completed for user: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Error resetting password with token: {}", token, e);
            throw new RuntimeException("Failed to reset password", e);
        }
    }
    
    /**
     * Get user by reset token
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByResetToken(String token) {
        try {
            Optional<PasswordResetToken> tokenOptional = tokenRepository.findValidTokenByToken(token, LocalDateTime.now());
            return tokenOptional.map(PasswordResetToken::getUser);
            
        } catch (Exception e) {
            logger.error("Error getting user by reset token", e);
            return Optional.empty();
        }
    }
    
    /**
     * Generate secure random token
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Scheduled task to clean up expired tokens
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            tokenRepository.deleteExpiredTokens(LocalDateTime.now());
            logger.debug("Expired password reset tokens cleaned up");
            
        } catch (Exception e) {
            logger.error("Error cleaning up expired tokens", e);
        }
    }
}