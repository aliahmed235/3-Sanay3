package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.entites.User;

/**
 * JWT Service Interface
 * Defines contract for JWT token operations
 */
public interface JwtService {

    /**
     * Generate access token for user
     * @param user User to generate token for
     * @return JWT access token string
     */
    String generateAccessToken(User user);

    /**
     * Generate refresh token for user
     * @param user User to generate token for
     * @return JWT refresh token string
     */
    String generateRefreshToken(User user);

    /**
     * Validate JWT token
     * @param token Token to validate
     * @return true if token is valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Extract user ID from JWT token
     * @param token JWT token
     * @return User ID
     */
    Long getUserIdFromToken(String token);

    /**
     * Extract email from JWT token
     * @param token JWT token
     * @return User email
     */
    String getEmailFromToken(String token);

    /**
     * Extract roles from JWT token
     * @param token JWT token
     * @return Comma-separated roles
     */
    String getRolesFromToken(String token);

    /**
     * Blacklist a token (for logout)
     * @param token Token to blacklist
     */
    void blacklistToken(String token);

    /**
     * Check if token is blacklisted
     * @param token Token to check
     * @return true if blacklisted, false otherwise
     */
    boolean isTokenBlacklisted(String token);
}