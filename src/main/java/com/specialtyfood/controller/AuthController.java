package com.specialtyfood.controller;

import com.specialtyfood.dto.*;
import com.specialtyfood.security.JwtTokenProvider;
import com.specialtyfood.service.UserService;
import com.specialtyfood.service.PasswordResetService;
import com.specialtyfood.service.SecurityAuditService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller for login, register, and token refresh
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private SecurityAuditService securityAuditService;
    
    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication.getName());
            
            // Get user details
            UserDto userDto = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                    .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getPhoneNumber(),
                        user.getRole(),
                        user.getIsActive(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                    ))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            JwtAuthenticationResponse jwtResponse = new JwtAuthenticationResponse(accessToken, refreshToken, userDto);
            
            // Store token in cookie for browser requests
            jakarta.servlet.http.Cookie tokenCookie = new jakarta.servlet.http.Cookie("accessToken", accessToken);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setSecure(false); // Set to true in production with HTTPS
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(tokenCookie);
            
            // Store token in session for form-based requests
            request.getSession().setAttribute("accessToken", accessToken);
            request.getSession().setAttribute("user", userDto);
            
            // Log successful authentication
            securityAuditService.logSuccessfulAuthentication(
                loginRequest.getUsernameOrEmail(),
                securityAuditService.getClientIpAddress(request),
                securityAuditService.getUserAgent(request)
            );
            
            logger.info("User {} logged in successfully", loginRequest.getUsernameOrEmail());
            return ResponseEntity.ok(jwtResponse);
            
        } catch (AuthenticationException e) {
            // Log failed authentication
            securityAuditService.logFailedAuthentication(
                loginRequest.getUsernameOrEmail(),
                securityAuditService.getClientIpAddress(request),
                securityAuditService.getUserAgent(request),
                "Invalid credentials"
            );
            
            logger.error("Authentication failed for user: {}", loginRequest.getUsernameOrEmail());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username/email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            logger.error("Login error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "An error occurred during login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if username exists
            if (userService.existsByUsername(registerRequest.getUsername())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Tên đăng nhập đã được sử dụng!");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check if email exists
            if (userService.existsByEmail(registerRequest.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email đã được sử dụng!");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check if phone number exists (if provided)
            if (registerRequest.getPhoneNumber() != null && !registerRequest.getPhoneNumber().trim().isEmpty()) {
                if (userService.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Số điện thoại đã được sử dụng!");
                    return ResponseEntity.badRequest().body(error);
                }
            }
            
            // Register user
            UserDto userDto = userService.registerUser(registerRequest);
            
            // Generate tokens
            String accessToken = tokenProvider.generateTokenFromUsername(userDto.getUsername());
            String refreshToken = tokenProvider.generateRefreshToken(userDto.getUsername());
            
            JwtAuthenticationResponse response = new JwtAuthenticationResponse(accessToken, refreshToken, userDto);
            
            logger.info("User {} registered successfully", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Data integrity violation during registration: ", e);
            Map<String, String> error = new HashMap<>();
            if (e.getMessage().contains("phone_number")) {
                error.put("error", "Số điện thoại đã được sử dụng!");
            } else if (e.getMessage().contains("email")) {
                error.put("error", "Email đã được sử dụng!");
            } else if (e.getMessage().contains("username")) {
                error.put("error", "Tên đăng nhập đã được sử dụng!");
            } else {
                error.put("error", "Thông tin đăng ký không hợp lệ hoặc đã được sử dụng!");
            }
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Registration error: ", e);
            Map<String, String> error = new HashMap<>();
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("already")) {
                error.put("error", errorMessage);
            } else {
                error.put("error", "Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại!");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();
            
            if (tokenProvider.validateToken(refreshToken)) {
                String username = tokenProvider.getUsernameFromToken(refreshToken);
                
                // Generate new access token
                String newAccessToken = tokenProvider.generateTokenFromUsername(username);
                
                // Get user details
                UserDto userDto = userService.findByUsernameOrEmail(username)
                        .map(user -> new UserDto(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getFullName(),
                            user.getPhoneNumber(),
                            user.getRole(),
                            user.getIsActive(),
                            user.getCreatedAt(),
                            user.getUpdatedAt()
                        ))
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                JwtAuthenticationResponse response = new JwtAuthenticationResponse(newAccessToken, refreshToken, userDto);
                
                logger.info("Token refreshed for user: {}", username);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Token refresh error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token refresh failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Logout endpoint (client-side token removal)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        
        // Clear cookie
        jakarta.servlet.http.Cookie tokenCookie = new jakarta.servlet.http.Cookie("accessToken", null);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(false);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);
        
        // Clear session
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "User logged out successfully");
        
        logger.info("User logged out");
        return ResponseEntity.ok(responseBody);
    }
    
    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String username = authentication.getName();
            UserDto userDto = userService.findByUsernameOrEmail(username)
                    .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getPhoneNumber(),
                        user.getRole(),
                        user.getIsActive(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                    ))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return ResponseEntity.ok(userDto);
            
        } catch (Exception e) {
            logger.error("Get current user error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get user information");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {
        try {
            passwordResetService.initiatePasswordReset(request.getEmail());
            
            // Log password reset request
            securityAuditService.logPasswordResetRequest(
                request.getEmail(),
                securityAuditService.getClientIpAddress(httpRequest)
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account with that email exists, a password reset link has been sent.");
            
            logger.info("Password reset requested for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Password reset request error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process password reset request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Validate password reset token
     */
    @GetMapping("/reset-password/validate")
    public ResponseEntity<?> validateResetToken(@RequestParam("token") String token) {
        try {
            boolean isValid = passwordResetService.validateResetToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                response.put("message", "Token is valid");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Token is invalid or expired");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Token validation error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to validate token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Reset password with token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request, HttpServletRequest httpRequest) {
        try {
            // Validate password confirmation
            if (!request.isPasswordMatching()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Passwords do not match");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Get user email for logging before resetting password
            String userEmail = passwordResetService.getUserByResetToken(request.getToken())
                    .map(user -> user.getEmail())
                    .orElse("unknown");
            
            // Reset password
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            
            // Log successful password reset
            securityAuditService.logPasswordResetSuccess(
                userEmail,
                securityAuditService.getClientIpAddress(httpRequest)
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password has been reset successfully");
            
            logger.info("Password reset completed for token: {}", request.getToken());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Password reset error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Password reset error: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}