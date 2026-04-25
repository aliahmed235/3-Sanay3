package com.sany3.graduation_project.util;

import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ApiPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Helper class for building API responses
 * Reduces boilerplate in controllers
 */
public class ApiResponseBuilder {

    /**
     * Success response with data
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Success response with default message
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(data, "Operation successful");
    }

    /**
     * Created response (201)
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }

    /**
     * Created response with default message
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return created(data, "Resource created successfully");
    }

    /**
     * Paginated response
     */
    public static <T> ResponseEntity<ApiPageResponse<T>> page(Page<T> page, String message) {
        return ResponseEntity.ok(ApiPageResponse.of(page, message));
    }

    /**
     * Paginated response with default message
     */
    public static <T> ResponseEntity<ApiPageResponse<T>> page(Page<T> page) {
        return page(page, "Data retrieved successfully");
    }

    /**
     * Error response (400)
     */
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message, String errorCode) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, errorCode));
    }

    /**
     * Error response with default error code
     */
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return badRequest(message, "BAD_REQUEST");
    }

    /**
     * Unauthorized response (401)
     */
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message, "UNAUTHORIZED"));
    }

    /**
     * Forbidden response (403)
     */
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(message, "FORBIDDEN"));
    }

    /**
     * Not found response (404)
     */
    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message, "NOT_FOUND"));
    }

    /**
     * Conflict response (409)
     */
    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message, "CONFLICT"));
    }

    /**
     * Internal server error (500)
     */
    public static <T> ResponseEntity<ApiResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message, "INTERNAL_ERROR"));
    }
}