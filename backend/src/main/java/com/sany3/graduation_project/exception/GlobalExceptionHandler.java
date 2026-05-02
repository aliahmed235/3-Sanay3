package com.sany3.graduation_project.exception;

import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all controllers
 * Catches exceptions and returns consistent error responses
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     *
     * Example error response:
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "errorCode": "VALIDATION_ERROR",
     *   "data": {
     *     "email": "Email should be valid",
     *     "password": "Password must be 8+ characters"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(Constants.ERROR_MESSAGE.VALIDATION_FAILED)
                        .errorCode(Constants.ERROR_CODE.VALIDATION_ERROR)
                        .data(errors)
                        .build());
    }

    /**
     * Handle resource not found
     *
     * Example: GET /requests/999 (doesn't exist)
     * Response:
     * {
     *   "success": false,
     *   "message": "User not found",
     *   "errorCode": "NOT_FOUND"
     * }
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), Constants.ERROR_CODE.NOT_FOUND));
    }

    /**
     * Handle user already exists
     *
     * Example: Register with email that already exists
     * Response:
     * {
     *   "success": false,
     *   "message": "Email already registered",
     *   "errorCode": "CONFLICT"
     * }
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            WebRequest request) {
        log.warn("User already exists: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), Constants.ERROR_CODE.CONFLICT));
    }

    /**
     * Handle invalid credentials
     *
     * Example: Wrong password
     * Response:
     * {
     *   "success": false,
     *   "message": "Invalid email or password",
     *   "errorCode": "UNAUTHORIZED"
     * }
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
            InvalidCredentialsException ex,
            WebRequest request) {
        log.warn("Invalid credentials: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), Constants.ERROR_CODE.UNAUTHORIZED));
    }

    /**
     * Handle illegal state (business logic violation)
     *
     * Example: Try to accept offer when request is not OPEN
     * Response:
     * {
     *   "success": false,
     *   "message": "Request must be OPEN before accepting offers",
     *   "errorCode": "INVALID_STATE"
     * }
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(
            IllegalStateException ex,
            WebRequest request) {
        log.warn("Illegal state: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), Constants.ERROR_CODE.INVALID_STATE));
    }

    /**
     * Handle illegal arguments
     *
     * Example: Offer doesn't belong to this request
     * Response:
     * {
     *   "success": false,
     *   "message": "Offer does not belong to this request",
     *   "errorCode": "VALIDATION_ERROR"
     * }
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), Constants.ERROR_CODE.VALIDATION_ERROR));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            WebRequest request) {
        log.warn("Upload too large: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("Uploaded file is too large", Constants.ERROR_CODE.VALIDATION_ERROR));
    }

    /**
     * Handle bad credentials (Spring Security)
     *
     * Example: Invalid JWT token
     * Response:
     * {
     *   "success": false,
     *   "message": "Invalid credentials",
     *   "errorCode": "UNAUTHORIZED"
     * }
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid credentials", Constants.ERROR_CODE.UNAUTHORIZED));
    }

    /**
     * Handle all other unexpected exceptions
     *
     * Response:
     * {
     *   "success": false,
     *   "message": "Internal server error",
     *   "errorCode": "ERROR"
     * }
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex,
            WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        Constants.ERROR_MESSAGE.INTERNAL_SERVER_ERROR,
                        "ERROR"
                ));
    }
}
