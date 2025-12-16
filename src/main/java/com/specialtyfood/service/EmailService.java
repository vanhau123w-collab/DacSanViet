package com.specialtyfood.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending various types of emails
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.mail.from:noreply@specialtyfood.com}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Specialty Food Store");
            
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            String emailBody = buildPasswordResetEmailBody(resetUrl);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    /**
     * Send password reset confirmation email
     */
    public void sendPasswordResetConfirmationEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Successful - Specialty Food Store");
            
            String emailBody = buildPasswordResetConfirmationEmailBody();
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Password reset confirmation email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send password reset confirmation email to: {}", toEmail, e);
            // Don't throw exception here as password reset was successful
        }
    }
    
    /**
     * Send welcome email to new users
     */
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Specialty Food Store!");
            
            String emailBody = buildWelcomeEmailBody(fullName);
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Welcome email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", toEmail, e);
            // Don't throw exception as registration was successful
        }
    }
    
    /**
     * Build password reset email body
     */
    private String buildPasswordResetEmailBody(String resetUrl) {
        return "Dear Customer,\n\n" +
                "You have requested to reset your password for your Specialty Food Store account.\n\n" +
                "Please click the following link to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link will expire in 1 hour for security reasons.\n\n" +
                "If you did not request this password reset, please ignore this email and your password will remain unchanged.\n\n" +
                "Best regards,\n" +
                "Specialty Food Store Team\n\n" +
                "Note: This is an automated email. Please do not reply to this message.";
    }
    
    /**
     * Build password reset confirmation email body
     */
    private String buildPasswordResetConfirmationEmailBody() {
        return "Dear Customer,\n\n" +
                "Your password has been successfully reset for your Specialty Food Store account.\n\n" +
                "If you did not make this change, please contact our support team immediately.\n\n" +
                "For security reasons, we recommend:\n" +
                "- Using a strong, unique password\n" +
                "- Not sharing your password with anyone\n" +
                "- Logging out from shared devices\n\n" +
                "Best regards,\n" +
                "Specialty Food Store Team\n\n" +
                "Note: This is an automated email. Please do not reply to this message.";
    }
    
    /**
     * Build welcome email body
     */
    private String buildWelcomeEmailBody(String fullName) {
        return "Dear " + (fullName != null ? fullName : "Customer") + ",\n\n" +
                "Welcome to Specialty Food Store!\n\n" +
                "Thank you for creating an account with us. You can now:\n" +
                "- Browse our wide selection of specialty foods\n" +
                "- Add items to your cart and place orders\n" +
                "- Track your order history\n" +
                "- Manage your profile and addresses\n\n" +
                "Visit our website: " + frontendUrl + "\n\n" +
                "If you have any questions, please don't hesitate to contact our support team.\n\n" +
                "Happy shopping!\n\n" +
                "Best regards,\n" +
                "Specialty Food Store Team\n\n" +
                "Note: This is an automated email. Please do not reply to this message.";
    }
}