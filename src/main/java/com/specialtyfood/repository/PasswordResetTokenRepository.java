package com.specialtyfood.repository;

import com.specialtyfood.model.PasswordResetToken;
import com.specialtyfood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for PasswordResetToken entity
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Find password reset token by token string
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Find password reset token by user
     */
    Optional<PasswordResetToken> findByUser(User user);
    
    /**
     * Find valid (unused and not expired) token by token string
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token AND prt.used = false AND prt.expiryDate > :now")
    Optional<PasswordResetToken> findValidTokenByToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Delete all expired tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Delete all tokens for a specific user
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.user = :user")
    void deleteByUser(@Param("user") User user);
    
    /**
     * Mark token as used
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.token = :token")
    void markTokenAsUsed(@Param("token") String token);
}