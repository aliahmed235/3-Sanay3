package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.dto.request.LoginRequest;
import com.sany3.graduation_project.dto.request.RegisterProviderRequest;
import com.sany3.graduation_project.dto.request.RegisterRequest;
import com.sany3.graduation_project.dto.response.LoginResponse;

/**
 * Authentication Service Interface
 * Defines contract for authentication operations
 */
public interface AuthService {

    /**
     * Register a new customer
     * @param request Registration request with customer details
     * @return LoginResponse with JWT tokens
     */
    LoginResponse registerCustomer(RegisterRequest request);

    /**
     * Register a new service provider
     * @param request Registration request with provider details
     * @return LoginResponse with JWT tokens
     */
    LoginResponse registerServiceProvider(RegisterProviderRequest request);

    /**
     * Login user with email and password
     * @param email User email
     * @param password User password
     * @return LoginResponse with JWT tokens
     */
    LoginResponse login(String email, String password);

    /**
     * Refresh access token using refresh token
     * @param refreshToken Valid refresh token
     * @return LoginResponse with new access token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * Logout user and invalidate token
     * @param token Access token to invalidate
     */
    void logout(String token);
}