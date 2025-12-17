package com.specialtyfood.service;

import com.specialtyfood.dto.CreateUserRequest;
import com.specialtyfood.dto.RegisterRequest;
import com.specialtyfood.dto.UpdateUserRequest;
import com.specialtyfood.dao.UserDao;
// Removed Role import - using admin boolean instead
import com.specialtyfood.model.User;
import com.specialtyfood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User service for user management operations
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user
     */
    public UserDao registerUser(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã được sử dụng!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        
        // Check if phone number already exists (if provided)
        if (registerRequest.getPhoneNumber() != null && !registerRequest.getPhoneNumber().trim().isEmpty()) {
            if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber().trim())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng!");
            }
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        
        // Set phone number (trim and handle null/empty)
        String phoneNumber = registerRequest.getPhoneNumber();
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            user.setPhoneNumber(phoneNumber.trim());
        } else {
            user.setPhoneNumber(null);
        }
        
        user.setAdmin(false);
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    /**
     * Find user by username or email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }
    
    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get user DAO by ID
     */
    @Transactional(readOnly = true)
    public UserDao getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }
    
    /**
     * Update user profile
     */
    public UserDao updateProfile(Long userId, RegisterRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Check if new username is taken by another user
        if (!user.getUsername().equals(updateRequest.getUsername()) && 
            userRepository.existsByUsername(updateRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        // Check if new email is taken by another user
        if (!user.getEmail().equals(updateRequest.getEmail()) && 
            userRepository.existsByEmail(updateRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Update user fields
        user.setUsername(updateRequest.getUsername());
        user.setEmail(updateRequest.getEmail());
        user.setFullName(updateRequest.getFullName());
        user.setPhoneNumber(updateRequest.getPhoneNumber());
        
        // Update password if provided
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }
    
    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect!");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * Activate/Deactivate user
     */
    public UserDao toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }
    
    /**
     * Search users (admin function)
     */
    @Transactional(readOnly = true)
    public Page<UserDao> searchUsers(String searchTerm, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(this::convertToDto);
    }
    
    /**
     * Get all users (admin function)
     */
    @Transactional(readOnly = true)
    public Page<UserDao> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }
    
    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Create a new user (admin function)
     */
    public UserDao createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' đã tồn tại!");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAdmin(request.getAdmin() != null ? request.getAdmin() : false);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    /**
     * Update user (admin function)
     */
    public UserDao updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));
        
        // Check if new username is taken by another user
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại!");
        }
        
        // Check if new email is taken by another user
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' đã tồn tại!");
        }
        
        // Update user fields
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAdmin(request.getAdmin() != null ? request.getAdmin() : false);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }
    
    /**
     * Delete user (soft delete by deactivating)
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));
        
        // Soft delete by deactivating
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Check if phone number exists
     */
    @Transactional(readOnly = true)
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByPhoneNumber(phoneNumber.trim());
    }
    
    /**
     * Convert User entity to UserDao
     */
    private UserDao convertToDto(User user) {
        return new UserDao(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getAdmin(),
            user.getIsActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}