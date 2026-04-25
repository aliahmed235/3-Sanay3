package com.sany3.graduation_project.util;

/**
 * Central location for all application constants
 * Avoids magic numbers and strings scattered throughout code
 */
public class Constants {

    /**
     * JWT Token Configuration
     */
    public static final class JWT {
        public static final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000;      // 15 minutes in milliseconds
        public static final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000;  // 7 days in milliseconds
        public static final String TOKEN_TYPE = "Bearer";
        public static final String AUTHORIZATION_HEADER = "Authorization";
    }

    /**
     * Validation Patterns and Constraints
     */
    public static final class VALIDATION {
        // Phone validation: 10 or more digits
        public static final String PHONE_PATTERN = "^[0-9]{10,}$";

        // Password validation: 8+ chars, must have uppercase and number
        public static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$";

        // Name length constraints
        public static final int MIN_NAME_LENGTH = 3;
        public static final int MAX_NAME_LENGTH = 100;

        // Email length
        public static final int MAX_EMAIL_LENGTH = 100;

        // Bio constraints (for service providers)
        public static final int MIN_BIO_LENGTH = 10;
        public static final int MAX_BIO_LENGTH = 500;

        // Address constraints
        public static final int MAX_ADDRESS_LENGTH = 255;

        // Hourly rate constraints
        public static final double MIN_HOURLY_RATE = 0.01;
        public static final double MAX_HOURLY_RATE = 10000.00;

        // Budget constraints
        public static final double MIN_BUDGET = 0.01;
        public static final double MAX_BUDGET = 1000000.00;
    }

    /**
     * Pagination Configuration
     */
    public static final class PAGINATION {
        public static final int DEFAULT_PAGE_NUMBER = 0;
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int MAX_PAGE_SIZE = 100;
        public static final int MIN_PAGE_SIZE = 1;
        public static final String DEFAULT_SORT_FIELD = "createdAt";
        public static final String DEFAULT_SORT_DIRECTION = "DESC";
    }

    /**
     * Geolocation and Map Search Configuration
     */
    public static final class SEARCH {
        // Default search radius in kilometers
        public static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;

        // Maximum search radius
        public static final double MAX_SEARCH_RADIUS_KM = 100.0;

        // Minimum search radius
        public static final double MIN_SEARCH_RADIUS_KM = 0.5;

        // Earth radius in kilometers (for Haversine formula)
        public static final double EARTH_RADIUS_KM = 6371.0;
    }

    /**
     * Request Configuration
     */
    public static final class REQUEST {
        // Request expires after 24 hours
        public static final long EXPIRY_HOURS = 24;

        // Maximum description length
        public static final int MAX_DESCRIPTION_LENGTH = 1000;
        public static final int MIN_DESCRIPTION_LENGTH = 10;

        // Maximum title length
        public static final int MAX_TITLE_LENGTH = 255;
        public static final int MIN_TITLE_LENGTH = 5;
    }

    /**
     * Offer Configuration
     */
    public static final class OFFER {
        // Maximum estimated time in minutes
        public static final int MAX_ESTIMATED_TIME_MINUTES = 480;  // 8 hours
        public static final int MIN_ESTIMATED_TIME_MINUTES = 1;

        // Maximum offer description length
        public static final int MAX_DESCRIPTION_LENGTH = 500;
    }

    /**
     * Chat Configuration
     */
    public static final class CHAT {
        // Maximum message length
        public static final int MAX_MESSAGE_LENGTH = 1000;
        public static final int MIN_MESSAGE_LENGTH = 1;

        // Chat message types
        public static final String MESSAGE_TYPE_TEXT = "TEXT";
        public static final String MESSAGE_TYPE_LOCATION = "LOCATION";
        public static final String MESSAGE_TYPE_PHOTO = "PHOTO";
    }

    /**
     * Rating Configuration
     */
    public static final class RATING {
        // Star rating constraints
        public static final int MIN_RATING = 1;
        public static final int MAX_RATING = 5;

        // Review length
        public static final int MAX_REVIEW_LENGTH = 500;
        public static final int MIN_REVIEW_LENGTH = 10;
    }

    /**
     * File Upload Configuration
     */
    public static final class FILE {
        // Maximum file size in MB
        public static final long MAX_FILE_SIZE_MB = 10;

        // Allowed document types
        public static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf", "image/jpeg", "image/png"};
    }

    /**
     * Coordinate Constraints (Latitude/Longitude)
     */
    public static final class COORDINATES {
        public static final double MIN_LATITUDE = -90.0;
        public static final double MAX_LATITUDE = 90.0;
        public static final double MIN_LONGITUDE = -180.0;
        public static final double MAX_LONGITUDE = 180.0;
    }

    /**
     * Service Types
     */
    public static final class SERVICE_TYPE {
        public static final String GAS = "GAS";
        public static final String WATER = "WATER";
        public static final String ELECTRICITY = "ELECTRICITY";
    }

    /**
     * Request Status Values
     */
    public static final class REQUEST_STATUS {
        public static final String OPEN = "OPEN";
        public static final String ACCEPTED = "ACCEPTED";
        public static final String ONGOING = "ONGOING";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";
    }

    /**
     * Offer Status Values
     */
    public static final class OFFER_STATUS {
        public static final String PENDING = "PENDING";
        public static final String ACCEPTED = "ACCEPTED";
        public static final String REJECTED = "REJECTED";
        public static final String WITHDRAWN = "WITHDRAWN";
    }

    /**
     * Verification Status Values
     */
    public static final class VERIFICATION_STATUS {
        public static final String PENDING = "PENDING";
        public static final String APPROVED = "APPROVED";
        public static final String REJECTED = "REJECTED";
    }

    /**
     * Error Codes
     */
    public static final class ERROR_CODE {
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String CONFLICT = "CONFLICT";
        public static final String INVALID_STATE = "INVALID_STATE";
        public static final String PROVIDER_NOT_VERIFIED = "PROVIDER_NOT_VERIFIED";
        public static final String SERVICE_TYPE_MISMATCH = "SERVICE_TYPE_MISMATCH";
        public static final String REQUEST_EXPIRED = "REQUEST_EXPIRED";
        public static final String OFFER_ALREADY_EXISTS = "OFFER_ALREADY_EXISTS";
    }

    /**
     * Success Messages
     */
    public static final class SUCCESS_MESSAGE {
        public static final String OPERATION_SUCCESSFUL = "Operation successful";
        public static final String RESOURCE_CREATED = "Resource created successfully";
        public static final String RESOURCE_UPDATED = "Resource updated successfully";
        public static final String RESOURCE_DELETED = "Resource deleted successfully";
        public static final String RESOURCE_RETRIEVED = "Resource retrieved successfully";
        public static final String LOGIN_SUCCESSFUL = "Login successful";
        public static final String LOGOUT_SUCCESSFUL = "Logout successful";
        public static final String REGISTRATION_SUCCESSFUL = "Registration successful";
        public static final String REQUEST_CREATED = "Request created successfully";
        public static final String OFFER_SUBMITTED = "Offer submitted successfully";
        public static final String OFFER_ACCEPTED = "Offer accepted successfully";
        public static final String RATING_SUBMITTED = "Rating submitted successfully";
    }

    /**
     * Error Messages
     */
    public static final class ERROR_MESSAGE {
        public static final String VALIDATION_FAILED = "Validation failed";
        public static final String INVALID_CREDENTIALS = "Invalid email or password";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String EMAIL_ALREADY_EXISTS = "Email already registered";
        public static final String PHONE_ALREADY_EXISTS = "Phone already registered";
        public static final String PROVIDER_NOT_VERIFIED = "Provider is not verified yet";
        public static final String REQUEST_NOT_FOUND = "Request not found";
        public static final String OFFER_NOT_FOUND = "Offer not found";
        public static final String REQUEST_EXPIRED = "Request has expired";
        public static final String OFFER_ALREADY_EXISTS = "You have already made an offer for this request";
        public static final String SERVICE_TYPE_MISMATCH = "Your service type does not match this request";
        public static final String UNAUTHORIZED_ACCESS = "You are not authorized to access this resource";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    }
}